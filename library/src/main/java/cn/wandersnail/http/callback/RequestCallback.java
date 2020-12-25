package cn.wandersnail.http.callback;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 请求结果回调
 * <p>
 * date: 2019/8/23 15:22
 * author: zengfansheng
 */
public interface RequestCallback<T> {
    /**
     * 请求成功。已废弃！请使用{@link #onSuccess(retrofit2.Response, Object)}
     *
     * @param response          原始响应
     * @param convertedResponse 经过设置的转换器转换后的结果
     */
    @Deprecated
    default void onSuccess(@NonNull Response response, @Nullable T convertedResponse) {}

    void onError(@NonNull Throwable t);

    /**
     * 请求成功
     *
     * @param response          原始响应
     * @param convertedResponse 经过设置的转换器转换后的结果
     */
    void onSuccess(@NonNull retrofit2.Response<ResponseBody> response, @Nullable T convertedResponse);
    
    default void onConvertError(@NonNull Throwable t) {}
}
