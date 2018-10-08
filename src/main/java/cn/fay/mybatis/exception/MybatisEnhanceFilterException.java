package cn.fay.mybatis.exception;

/**
 * @author fay  fay9395@gmail.com
 * @date 2018/8/21 下午4:20.
 */
public class MybatisEnhanceFilterException extends RuntimeException {
    public MybatisEnhanceFilterException() {
        super();
    }

    public MybatisEnhanceFilterException(String message) {
        super(message);
    }

    public MybatisEnhanceFilterException(String message, Throwable cause) {
        super(message, cause);
    }

    public MybatisEnhanceFilterException(Throwable cause) {
        super(cause);
    }

    protected MybatisEnhanceFilterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
