# mybatis-enhancer

1. add `<bean class="xxx.ApplicationContextHolder"/>` to declare this bean
2. add ```<plugins>
        <plugin interceptor="xxx.interceptor.MybatisEnhanceInterceptor"/>
    </plugins>``` to mybatis config file to use the plugin.
3. then you can implement `MybatisFilter` or `MybatisListener` and add the bean to spring bean factory.
