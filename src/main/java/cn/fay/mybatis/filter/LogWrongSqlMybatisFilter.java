package cn.fay.mybatis.filter;

import cn.fay.mybatis.intercepter.MybatisEnhanceInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author fay  fay9395@gmail.com
 * @date 2018/8/22 下午4:49.
 */
public abstract class LogWrongSqlMybatisFilter implements MybatisFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MybatisEnhanceInterceptor.class);

    protected void logWrongSql(String sql) {
        LOGGER.info("{} doFilter return false with sql: {}", this.getClass().getSimpleName(), sql);
    }
}
