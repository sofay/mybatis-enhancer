package cn.fay.mybatis.utils;

import java.util.*;

/**
 * sql简单解析工具包
 *
 * @author fay  fay9395@gmail.com
 * @date 2018/8/22 下午4:34.
 */
public class SqlSimpleParseUtils {
    private static final int CACHE_SIZE = 256;
    private static final LinkedHashMap<String, String> cache = new LinkedHashMap<String, String>(CACHE_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return CACHE_SIZE < size();
        }
    };

    /**
     * 获取update语句中的表名，如果有表连接情况将返回""
     */
    public static String getUpdateTableName(String sql) {
        sql = simpleSql(sql);
        String prefix = "update ";
        String suffix = " set ";
        int startIndex = sql.indexOf(prefix) + prefix.length();
        int endIndex = sql.indexOf(suffix);
        if (startIndex < prefix.length() || endIndex <= startIndex) {
            return "";
        }
        String tableName = sql.substring(startIndex, endIndex);
        if (tableName.contains(" join ")) {
            return "";
        }
        return tableName.trim();
    }

    /**
     * 获取delete语句中的表名，如果有表连接情况将返回""
     */
    public static String getDeleteTableName(String sql) {
        sql = simpleSql(sql);
        String prefix = "delete ";
        String suffix = " from ";
        int startIndex = sql.indexOf(prefix) + prefix.length();
        int endIndex = sql.indexOf(suffix);
        if (startIndex < prefix.length() || endIndex <= startIndex) {
            return "";
        }
        String tableName = sql.substring(startIndex, endIndex);
        if (tableName.contains(" join ")) {
            return "";
        }
        return tableName.trim();
    }

    /**
     * 获取sql中where子句的列属性
     */
    public static List<String> getWhereColumn(String sql) {
        sql = simpleSql(sql);
        String prefix = " where ";
        int startIndex = sql.indexOf(prefix) + prefix.length();
        if (startIndex < prefix.length()) {
            return Collections.emptyList();
        }
        String whereExp = sql.substring(startIndex);
        String andSplit = " and | or ";
        String[] whereArr = whereExp.split(andSplit);
        List<String> columns = new ArrayList<>(whereArr.length);
        for (String exp : whereArr) {
            columns.add(exp.split("=| like | in ")[0].trim());
        }
        return columns;
    }

    /**
     * >. \t 替换成空格
     * >.去除SQL首尾空格
     * >.去除SQL中的换行符
     * >.去除SQL中的"`"
     * >.将SQL中连续出现的多个空格精简为一个空格
     * >.将SQL中出现的引号内容替换成"?"
     */
    public static String simpleSql(String originSql) {
        String sql = cache.get(originSql);
        if (sql == null || "".equals(sql)) {
            sql = originSql.toLowerCase().trim().replaceAll("[\t\r\n`]", " ").replaceAll("[ ]{2,}", " ");
            int startIndex = sql.indexOf("'");
            while (startIndex != -1) {
                int nextIndex = sql.indexOf("'", startIndex + 1);
                if (nextIndex == -1) {
                    break;
                }
                sql = sql.replace(sql.substring(startIndex, nextIndex + 1), "?");
                startIndex = sql.indexOf("'", nextIndex + 1);
            }
            startIndex = sql.indexOf("\"");
            while (startIndex != -1) {
                int nextIndex = sql.indexOf("\"", startIndex + 1);
                if (nextIndex == -1) {
                    break;
                }
                sql = sql.replace(sql.substring(startIndex, nextIndex + 1), "?");
                startIndex = sql.indexOf("\"", nextIndex + 1);
            }
            cache.put(originSql, sql);
            cache.put(sql, sql); // warn 将简化之后的sql也放入缓存
        }
        return sql;
    }
}
