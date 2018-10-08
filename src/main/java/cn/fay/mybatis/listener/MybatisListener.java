package cn.fay.mybatis.listener;

import org.apache.ibatis.mapping.BoundSql;

/**
 * @author fay  fay9395@gmail.com
 * @date 2018/8/10 下午12:24.
 */
public interface MybatisListener {

    void onUpdate(BoundSql boundSql, Object result);

    void onQuery(BoundSql boundSql, Object result);
}
