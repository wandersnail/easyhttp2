package com.snail.network

import android.os.Handler
import android.os.Looper
import com.snail.network.callback.RequestCallback
import com.snail.network.utils.SchedulerUtils
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeoutException

/**
 * 一般的请求任务
 *
 * date: 2019/4/1 11:47
 * author: zengfansheng
 */
internal class GeneralRequestTask<T>(observable: Observable<T>, private val configuration: Configuration, private val callback: RequestCallback<T>?) {
    var disposable: Disposable? = null
        private set
    private var handler: Handler? = null
    private var secondCount = 0
    private var timerRunnable: Runnable? = null
    
    init {
        //只有设置过超时才计
        if (configuration.callTimeout > 0) {
            handler = Handler(Looper.getMainLooper())
            timerRunnable = TimerRunnable()
            handler?.postDelayed(timerRunnable, 1000)
        }
        disposable = observable.compose(SchedulerUtils.applyGeneralObservableSchedulers()).subscribe({
            callback?.onSuccess(it)
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