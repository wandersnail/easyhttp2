package cn.wandersnail.http;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeoutException;

import cn.wandersnail.http.callback.RequestCallback;
import cn.wandersnail.http.util.SchedulerUtils;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;

/**
 * 一般的请求任务
 * <p>
 * date: 2019/8/23 21:19
 * author: zengfansheng
 */
class GeneralRequestTask<T> {
    private final Configuration configuration;
    private final RequestCallback<T> callback;
    Disposable disposable;
    private Handler handler;

    GeneralRequestTask(Observable<Response<ResponseBody>> observable, @NonNull Converter<ResponseBody, T> converter,
                              Configuration configuration, RequestCallback<T> callback) {
        this.configuration = configuration;
        this.callback = callback;
        Runnable timerRunnable = new TimerRunnable();
        //只有设置过超时才计
        if (configuration.callTimeout > 0) {
            handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(timerRunnable, 1000);
        }
        disposable = observable.compose(SchedulerUtils.applyGeneralObservableSchedulers())
                .subscribe(response -> {
                    if (callback != null) {
                        try {
                            callback.onSuccess(response.raw(), converter.convert(response.body()));
                        } catch (Throwable t) {
                            callback.onError(t);
                        }
                    }
                }, throwable -> {
                    disposable = null;
                    handler.removeCallbacks(timerRunnable);
                    if (callback != null) {
                        callback.onError(throwable);
                    }
                }, () -> {
                    disposable = null;
                    handler.removeCallbacks(timerRunnable);
                });
    }

    private class TimerRunnable implements Runnable {
        private int secondCount;

        @Override
        public void run() {
            if (disposable != null && ++secondCount < configuration.callTimeout) {
                handler.postDelayed(this, 1000);
            } else {
                if (disposable != null && !disposable.isDisposed()) {
                    disposable.dispose();
                }
                disposable = null;
                if (callback != null) {
                    callback.onError(new TimeoutException("Http request timeout!"));
                }
            }
        }
    }
}
