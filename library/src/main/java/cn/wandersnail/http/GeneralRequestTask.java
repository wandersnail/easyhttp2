package cn.wandersnail.http;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeoutException;

import cn.wandersnail.http.callback.Cancelable;
import cn.wandersnail.http.callback.RequestCallback;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;

/**
 * 一般的请求任务
 * <p>
 * date: 2019/8/23 21:19
 * author: zengfansheng
 */
class GeneralRequestTask<T> implements Cancelable {
    private final Configuration configuration;
    private final RequestCallback<T> callback;
    Call<ResponseBody> call;
    private Handler handler;

    GeneralRequestTask(@NonNull Call<ResponseBody> call, Converter<ResponseBody, T> converter,
                       Configuration configuration, RequestCallback<T> callback) {
        this.call = call;
        this.configuration = configuration;
        this.callback = callback;
        doRequest(call, converter, configuration, callback);
    }
    
    private void doRequest(Call<ResponseBody> call, Converter<ResponseBody, T> converter, Configuration configuration, RequestCallback<T> callback) {
        Runnable timerRunnable = new TimerRunnable();
        //只有设置过超时才计
        if (configuration.callTimeout > 0) {
            handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(timerRunnable, 1000);
        }
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                handleRequestOver(timerRunnable);
                if (callback != null) {
                    T successBody = null;
                    T errorBody = null;
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        try {
                            successBody = body == null ? null : (converter == null ? (T) body : converter.convert(body));
                        } catch (Exception e) {
                            callback.onError(e);
                            return;
                        }
                    } else {
                        ResponseBody body = response.errorBody();
                        try {
                            errorBody = body == null ? null : (converter == null ? (T) body : converter.convert(body));
                        } catch (Exception ignore) {
                        }
                    }
                    callback.onResponse(response, successBody, errorBody);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                handleRequestOver(timerRunnable);
                if (callback != null) {
                    callback.onError(t);
                }
            }
        });
    }
    
    //处理请求结束
    private void handleRequestOver(Runnable timerRunnable) {
        call = null;
        if (handler != null) {
            handler.removeCallbacks(timerRunnable);
        }
    }

    @Override
    public boolean isCanceled() {
        return call == null || call.isCanceled();
    }

    @Override
    public void cancel() {
        if (call != null) {
            call.cancel();
        }
    }

    private class TimerRunnable implements Runnable {
        private int secondCount;

        @Override
        public void run() {
            if (call != null && ++secondCount < configuration.callTimeout) {
                handler.postDelayed(this, 1000);
            } else {
                if (call != null && !call.isCanceled()) {
                    call.cancel();
                }
                call = null;
                if (callback != null) {
                    callback.onError(new TimeoutException("Http request timeout!"));
                }
            }
        }
    }
}
