package cn.wandersnail.http.exception;

/**
 * date: 2019/8/23 14:43
 * author: zengfansheng
 */
public class MissingArgsException extends RuntimeException {

    public MissingArgsException(String message) {
        super(message);
    }

    public MissingArgsException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingArgsException(Throwable cause) {
        super(cause);
    }
}
