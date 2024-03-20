package cn.wandersnail.http.download;


import androidx.annotation.NonNull;

import java.io.IOException;

import cn.wandersnail.http.callback.ProgressListener;
import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 拦截器，加入进度监听
 * <p>
 * date: 2019/8/23 16:31
 * author: zengfansheng
 */
class ProgressInterceptor implements Interceptor {
    private final ProgressListener listener;

    ProgressInterceptor(ProgressListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        ResponseBody body = originalResponse.body();
        ResponseBody progressBody = body == null ? null : new ProgressResponseBody(body, listener);
        return originalResponse.newBuilder().body(progressBody).build();
    }
}
