package cn.wandersnail.http.callback;

/**
 * Created by 曾繁盛 on 2024/3/20 10:46
 */
public interface Cancelable {
    void cancel();

    boolean isCanceled();
}
