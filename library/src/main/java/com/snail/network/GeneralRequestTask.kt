package com.snail.network

import android.os.Handler
import android.os.Looper
import com.snail.network.callback.RequestCallback
import com.snail.network.converter.ResponseConverter
import com.snail.network.utils.SchedulerUtils
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import okhttp3.ResponseBody
import retrofit2.Response
import java.util.concurrent.TimeoutException

/**
 * 一般的请求任务
 *
 * date: 2019/4/1 11:47
 * author: zengfansheng
 */
internal class GeneralRequestTask<T>(observable: Observable<Response<ResponseBody>>, converter: ResponseConverter<T>, private val configuration: Configuration, private val callback: RequestCallback<T>?) {
    var disposable: Disposable?
        private set
    private var handler: Handler? = null    
    private var timerRunnable: Runnable? = null
    
    init {
        //只有设置过超时才计
        if (configuration.callTimeout > 0) {
            handler = Handler(Looper.getMainLooper())
            timerRunnable = TimerRunnable()
            handler?.postDelayed(timerRunnable, 1000)
        }
        disposable = observable.compose(SchedulerUtils.applyGeneralObservableSchedulers()).subscribe({
            try {
                callback?.onSuccess(it.raw(), converter.convert(it.body()))
            } catch (t: Throwable) {
                callback?.onError(t)
            }            
        }, {
            disposable = null
            handler?.removeCallbacks(timerRunnable)
            callback?.onError(it)
        }, {
            disposable = null
            handler?.removeCallbacks(timerRunnable)
        })        
    }
    
    private inner class TimerRunnable : Runnable {
        private var secondCount = 0
        override fun run() {
            if (disposable != null && ++secondCount < configuration.callTimeout) {
                handler?.postDelayed(this, 1000)
            } else {
                disposable?.dispose()
                disposable = null
                callback?.onError(TimeoutException("Http请求超时！"))
            }
        }
    }
}