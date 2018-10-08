package cn.fay.mybatis.filter;

import org.apache.ibatis.mapping.BoundSql;

/**
 * @author fay  fay9395@gmail.com
 * @date 2018/8/10 下午12:23.
 */
public interface MybatisFilter {
    /**
     * @param boundSql
     * @return true 正常往下执行  false 终止执行
     */
    boolean doFilter(BoundSql boundSql);
}
