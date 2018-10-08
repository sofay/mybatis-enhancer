package cn.fay.mybatis.intercepter;


import cn.fay.mybatis.exception.MybatisEnhanceFilterException;
import cn.fay.mybatis.extension.spring.ApplicationContextHolder;
import cn.fay.mybatis.filter.MybatisFilter;
import cn.fay.mybatis.listener.MybatisListener;
import cn.fay.mybatis.listener.Sync;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.lang.reflect.Method;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author fay  fay9395@gmail.com
 * @date 2018/8/10 上午11:24.
 */
@Intercepts(
        {
                @Signature(type = StatementHandler.class, method = "update", args = Statement.class),
                @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class})
        }
)
public class MybatisEnhanceInterceptor implements Interceptor {
    public static final ConcurrentHashMap<BoundSql, StackTraceElement[]> cache = new ConcurrentHashMap<>();
    public static final String USED_TIME_KEY = "usedTime";
    private static final Logger LOGGER = LoggerFactory.getLogger(MybatisEnhanceInterceptor.class);
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(1);
    private static Boolean canUse;
    private static MybatisFilter mybatisFilter;
    private static MybatisListener mybatisListener;
    private BoundSql boundSql;
    private static MybatisFilter failedFilter; // 可能会出现线程问题，这里只记录最新出现问题的filter，不保证线程安全

    /**
     * 供mybatis初始化时反射用
     */
    public MybatisEnhanceInterceptor() {
    }

    public MybatisEnhanceInterceptor(BoundSql boundSql) {
        this.boundSql = boundSql;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        if (!getMybatisFilter().doFilter(boundSql)) {
            throw new MybatisEnhanceFilterException(String.format("没有通过检验 %s", failedFilter != null ? failedFilter.getClass().getSimpleName() : ""));
        }
        long start = System.currentTimeMillis();
        Object result = invocation.proceed();
        boundSql.setAdditionalParameter(USED_TIME_KEY, System.currentTimeMillis() - start);
        switch (method.getName()) {
            case "update":
                getMybatisListener().onUpdate(boundSql, result);
                break;
            case "query":
                getMybatisListener().onQuery(boundSql, result);
                break;
            default:
        }
        return result;
    }

    @Override
    public Object plugin(Object target) {
        if (canUse == null) {
            try {
                canUse = (!ApplicationContextHolder.getBeans(MybatisFilter.class).isEmpty()) || (!ApplicationContextHolder.getBeans(MybatisListener.class).isEmpty());
            } catch (Exception ignore) {
                ignore.printStackTrace();
                canUse = false;
            }
        }
        if (target instanceof StatementHandler && canUse) {
            return Plugin.wrap(target, new MybatisEnhanceInterceptor(((StatementHandler) target).getBoundSql())); // 每次都新建，保证boundSql的线程安全
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {

    }

    public MybatisFilter getMybatisFilter() {
        if (mybatisFilter == null) {
            synchronized (MybatisEnhanceInterceptor.class) {
                if (mybatisFilter != null) {
                    return mybatisFilter;
                }
                final List<? extends MybatisFilter> mybatisFilters = new ArrayList<>(ApplicationContextHolder.getBeans(MybatisFilter.class).values());
                mybatisFilter = new MybatisFilter() {
                    @Override
                    public boolean doFilter(BoundSql boundSql) {
                        if ((mybatisFilters == null || mybatisFilters.isEmpty())) {
                            return true;
                        }
                        AnnotationAwareOrderComparator.sort(mybatisFilters);
                        for (MybatisFilter filter : mybatisFilters) {
                            try {
                                if (!filter.doFilter(boundSql)) {
                                    MybatisEnhanceInterceptor.failedFilter = filter;
                                    return false;
                                }
                            } catch (Exception e) { // 抛异常的filter跳过，不能因为人为写了错误的filter去影响正常的业务
                                LOGGER.info("[MYBATIS]执行过滤时因为异常:{} 跳过:{}", e.getMessage(), filter.getClass());
                            }
                        }
                        return true;
                    }
                };
            }
        }
        return mybatisFilter;
    }

    public MybatisListener getMybatisListener() {
        if (mybatisListener == null) {
            synchronized (MybatisEnhanceInterceptor.class) {
                if (mybatisListener != null) {
                    return mybatisListener;
                }
                final List<MybatisListener> mybatisListeners = new ArrayList<>(ApplicationContextHolder.getBeans(MybatisListener.class).values());
                final List<MybatisListener> syncListeners = new ArrayList<>();
                final List<MybatisListener> asyncListeners = new ArrayList<>();
                if (!mybatisListeners.isEmpty()) {
                    for (MybatisListener listener : mybatisListeners) {
                        if (listener.getClass().isAnnotationPresent(Sync.class)) {
                            syncListeners.add(listener);
                        } else {
                            asyncListeners.add(listener);
                        }
                    }
                }
                mybatisListener = new MybatisListener() {
                    private Object helpful(final BoundSql boundSql, final Object result, final RunAdapter runAdapter) {
                        if (!mybatisListeners.isEmpty()) {
                            cache.put(boundSql, Thread.currentThread().getStackTrace());
                            for (MybatisListener listener : syncListeners) {
                                try {
                                    runAdapter.run(new Object[]{listener, boundSql, result});
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    LOGGER.info("[MYBATIS]执行监听:{} 出现异常:{}", listener.getClass(), e.getMessage());
                                }
                            }
                            if (!mybatisListeners.isEmpty()) {
                                EXECUTOR_SERVICE.submit(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (MybatisListener listener : syncListeners) {
                                            try {
                                                runAdapter.run(new Object[]{listener, boundSql, result});
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                LOGGER.info("[MYBATIS]执行异步监听:{} 出现异常:{}", listener.getClass(), e.getMessage());
                                            }
                                        }
                                        cache.remove(boundSql);
                                    }
                                });
                            }
                            if (asyncListeners.isEmpty()) {
                                cache.remove(boundSql);
                            }
                        }
                        return null;
                    }

                    @Override
                    public void onUpdate(final BoundSql boundSql, final Object result) {
                        helpful(boundSql, result, new RunAdapter() {
                            @Override
                            public Object run(Object[] args) {
                                MybatisListener mybatisListener = (MybatisListener) args[0];
                                mybatisListener.onUpdate((BoundSql) args[1], args[2]);
                                return null;
                            }
                        });
                    }

                    @Override
                    public void onQuery(final BoundSql boundSql, final Object result) {
                        helpful(boundSql, result, new RunAdapter() {
                            @Override
                            public Object run(Object[] args) {
                                MybatisListener mybatisListener = (MybatisListener) args[0];
                                mybatisListener.onQuery((BoundSql) args[1], args[2]);
                                return null;
                            }
                        });
                    }
                };
            }
        }
        return mybatisListener;
    }

    interface RunAdapter {
        Object run(Object[] args);
    }
}
