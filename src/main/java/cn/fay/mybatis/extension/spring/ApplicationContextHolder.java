package cn.fay.mybatis.extension.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * @author fay  fay9395@gmail.com
 * @date 2018/4/26 下午2:30.
 */
public class ApplicationContextHolder implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHolder.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> type) {
        Assert.notNull(applicationContext, "请先把ApplicationContextHolder加入到spring容器中");
        Map<String, T> beanMap = applicationContext.getBeansOfType(type);
        if (beanMap != null && !beanMap.isEmpty()) {
            return beanMap.values().iterator().next();
        }
        return null;
    }

    public static <T> Map<String, T> getBeans(Class<T> type) {
        Assert.notNull(applicationContext, "请先把ApplicationContextHolder加入到spring容器中");
        return applicationContext.getBeansOfType(type);
    }

    public static String getProperty(String key) {
        Assert.notNull(applicationContext, "请先把ApplicationContextHolder加入到spring容器中");
        AbstractApplicationContext aac = (AbstractApplicationContext) applicationContext;
        return aac.getEnvironment().getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        AbstractApplicationContext aac = (AbstractApplicationContext) applicationContext;
        if (aac == null) {
            return defaultValue;
        }
        Assert.notNull(applicationContext, "请先把ApplicationContextHolder加入到spring容器中");
        return aac.getEnvironment().getProperty(key, defaultValue);
    }
}
