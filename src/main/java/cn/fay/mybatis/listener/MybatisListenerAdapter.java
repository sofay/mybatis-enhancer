package cn.fay.mybatis.listener;

import org.apache.ibatis.mapping.BoundSql;

/**
 * @author fay  fay9395@gmail.com
 * @date 2018/8/10 下午3:26.
 */
public abstract class MybatisListenerAdapter implements MybatisListener {
    @Override
    public void onUpdate(BoundSql boundSql, Object result) {
    }

    @Override
    public void onQuery(BoundSql boundSql, Object result) {
    }
}
