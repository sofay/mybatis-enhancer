package cn.fay.mybatis.listener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识作用
 * 将用线程池来执行监听{@link MybatisListener}，默认加上该注解
 *
 * @author fay  fay9395@gmail.com
 * @date 2018/8/27 上午10:56.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Async {
}
