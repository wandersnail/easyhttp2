package cn.wandersnail.http.callback;

import androidx.annotation.NonNull;

/**
 * date: 2021/6/11 11:41
 * author: zengfansheng
 */
public interface JsonParser<T> {
    @NonNull
    T parse(@NonNull String jsonStr);
}
