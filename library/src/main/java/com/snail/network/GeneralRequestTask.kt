package com.snail.network

import android.os.Handler
import android.os.Looper
import com.snail.network.callback.RequestCallback
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeoutException

/**
 * 一般的请求任务
 *
 * date: 2019/4/1 11:47
 * author: zengfansheng
 */
internal class GeneralRequestTask<T>(private val configuration: Configuration, private val callback: RequestCallback<T>?) : Observer<T> {
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
        }
    }
    
    private inner class TimerRunnable : Runnable {
        override fun run() {
            if (disposable != null && ++secondCount < configuration.callTimeout) {
                handler?.postDelayed(this, 1000)
            } else {
                disposable?.dispose()
                callback?.onError(TimeoutException("Http请求超时！"))
            }
        }
    }
    
    override fun onComplete() {
        disposable = null
        handler?.removeCallbacks(timerRunnable)
    }

    override fun onSubscribe(d: Disposable) {
        disposable = d
        handler?.postDelayed(timerRunnable, 1000)        
    }

    override fun onNext(t: T) {
        callback?.onSuccess(t)
    }

    override fun onError(e: Throwable) {
        disposable = null
        handler?.removeCallbacks(timerRunnable)
        callback?.onError(e)
    }
}