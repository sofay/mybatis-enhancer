package cn.fay.mybatis.listener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识作用
 * 在SQL操作的同一个线程中执行{@link MybatisListener} 优先于{@link Async} 执行
 *
 * @author fay  fay9395@gmail.com
 * @date 2018/8/27 上午10:56.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sync {
}
