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
    default void onSuccess(@NonNull Response response, @Nullable T convertedResponse) {
        //do nothing
    }

    /**
     * 请求成功
     *
     * @param response          原始响应
     * @param convertedResponse 经过设置的转换器转换后的结果
     */
    @Deprecated
    default void onSuccess(@NonNull retrofit2.Response<ResponseBody> response, @Nullable T convertedResponse) {
        //do nothing
    }

    /**
     * 出现异常
     */
    void onError(@NonNull Throwable t);

    /**
     * 响应回调
     *
     * @param response    原始响应
     * @param successBody 请求成功时，经过设置的转换器转换后的结果
     * @param errorBody   错误响应的body
     */
    void onResponse(@NonNull retrofit2.Response<ResponseBody> response, @Nullable T successBody, @Nullable ResponseBody errorBody);
}
