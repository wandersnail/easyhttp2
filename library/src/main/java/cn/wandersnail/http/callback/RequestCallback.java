package cn.wandersnail.http.callback;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import okhttp3.Response;

/**
 * 请求结果回调
 * <p>
 * date: 2019/8/23 15:22
 * author: zengfansheng
 */
public interface RequestCallback<T> {
    /**
     * 请求成功
     *
     * @param response          原始响应
     * @param convertedResponse 经过设置的转换器转换后的结果
     */
    void onSuccess(@NonNull Response response, @Nullable T convertedResponse);

    void onError(@NonNull Throwable t);
    
    default void onConvertError(@NonNull Throwable t) {}
}
