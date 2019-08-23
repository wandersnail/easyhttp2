package cn.wandersnail.http.exception;

/**
 * date: 2019/8/23 14:43
 * author: zengfansheng
 */
public class ConvertException extends RuntimeException {
    private static final long serialVersionUID = -4501070002355219441L;

    public ConvertException(String message) {
        super(message);
    }

    public ConvertException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConvertException(Throwable cause) {
        super(cause);
    }
}
