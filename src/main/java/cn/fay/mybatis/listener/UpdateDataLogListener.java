package cn.fay.mybatis.listener;

import cn.fay.mybatis.intercepter.MybatisEnhanceInterceptor;
import cn.fay.mybatis.utils.SqlSimpleParseUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fay  fay9395@gmail.com
 * @date 2018/8/10 下午2:21.
 */
@Component
@Sync
public class UpdateDataLogListener extends MybatisListenerAdapter {
    @Override
    public void onUpdate(BoundSql boundSql, Object result) {
        Map<String, Object> map = new HashMap<>();
        map.put("date", System.currentTimeMillis() / 1000);
        map.put("sql", SqlSimpleParseUtils.simpleSql(boundSql.getSql()));
        map.put("args", boundSql.getParameterObject());
        map.put("usedTime", boundSql.getAdditionalParameter(MybatisEnhanceInterceptor.USED_TIME_KEY));
        map.put("stackTraceInfo", MybatisEnhanceInterceptor.cache.get(boundSql));
        System.out.println(map);
    }

    private String mergeStackTrace(StackTraceElement[] stackTraceElements) {
        if (stackTraceElements == null || stackTraceElements.length <= 0)
            return "";
        StringBuilder info = new StringBuilder();
        for (StackTraceElement element : stackTraceElements) {
            if (element.getClassName().startsWith("org.springframework.aop.")
                    || element.getClassName().startsWith("java.lang.reflect.Method")
                    || element.getFileName() == null
                    || element.getFileName().equals("<generated>")
                    || element.getClassName().startsWith("sun.reflect.")
                    || element.getClassName().startsWith("org.springframework.web.servlet.mvc.method.")
                    || element.getClassName().startsWith("org.springframework.web.method.support.InvocableHandlerMethod")
                    || element.getClassName().startsWith("org.apache.ibatis.")
                    || element.getClassName().startsWith("com.sun.proxy.$")
                    || (element.getClassName().startsWith("java.lang.Thread") && element.getMethodName().startsWith("getStackTrace"))
                    || element.getClassName().startsWith("org.mybatis.spring.SqlSessionTemplate")
                    || element.getClassName().contains("MybatisEnhanceInterceptor")
                    || element.getClassName().startsWith("org.springframework.cglib.proxy.MethodProxy")
                    || element.getClassName().startsWith("org.springframework.transaction.interceptor.")) {
                continue;
            }
            if (element.getClassName().equals("org.springframework.web.servlet.DispatcherServlet") && element.getMethodName().equals("doDispatch")) {
                break;
            }
            info.append(element.toString()).append("\r\n");
        }
        return info.toString().substring(0, info.length() - 2);
    }
}
