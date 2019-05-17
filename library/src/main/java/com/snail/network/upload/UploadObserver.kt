package com.snail.network.upload

import com.snail.network.TaskInfo
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import okhttp3.ResponseBody
import retrofit2.Response

/**
 * 上传任务观察者
 *
 * date: 2019/2/28 12:58
 * author: zengfansheng
 * @property T 响应数据类型 
 */
internal class UploadObserver<T> 
@JvmOverloads constructor(private val info: UploadInfo<T>, private val listener: UploadListener<T>? = null) : 
    Observer<Response<ResponseBody>>, Disposable, UploadProgressListener {
    private var disposable: Disposable? = null
    private var lastUpdateTime: Long = 0//上次进度更新时间
    private val contentLengthMap = HashMap<String, Long>()
        
    override fun onSubscribe(d: Disposable) {
        disposable = d
        info.state = TaskInfo.State.START
        listener?.onStateChange(info.state, null)
    }

    override fun onError(e: Throwable) {
        info.state = TaskInfo.State.ERROR
        listener?.onStateChange(info.state, e)
    }

    override fun onNext(t: Response<ResponseBody>) {
        try {
            val convertedBody = info.converter.convert(t.body())
            listener?.onResponseBodyParse(t.raw(), convertedBody)
        } catch (e: Exception) {
            onError(e)
        }
    }

    override fun onProgress(name: String, progress: Long, max: Long) {
        AndroidSchedulers.mainThread().scheduleDirect {
            var completionLength = progress
            var contentLen = contentLengthMap[name]
            if (contentLen != null && contentLen > max) {
                completionLength += contentLen - max
            } else {
                contentLengthMap[name] = max
                contentLen = max
            }
            if (System.currentTimeMillis() - lastUpdateTime >= UPDATE_LIMIT_DURATION && (info.state == TaskInfo.State.IDLE ||
                            info.state == TaskInfo.State.START || info.state == TaskInfo.State.ONGOING)) {
                if (info.state != TaskInfo.State.ONGOING) {
                    info.state = TaskInfo.State.ONGOING
                    listener?.onStateChange(info.state, null)
                }
                listener?.onProgress(name, completionLength, contentLen)
                lastUpdateTime = System.currentTimeMillis()
            }
        }
    }
    
    override fun onComplete() {
        contentLengthMap.entries.forEach { 
            listener?.onProgress(it.key, it.value, it.value)
        }
        info.state = TaskInfo.State.COMPLETED
        listener?.onStateChange(info.state, null)
    }

    override fun isDisposed(): Boolean {
        return disposable == null || disposable!!.isDisposed
    }

    override fun dispose() {
        AndroidSchedulers.mainThread().scheduleDirect {
            disposable?.dispose()
            if (info.state == TaskInfo.State.ONGOING || info.state == TaskInfo.State.START) {
                info.state = TaskInfo.State.CANCEL
                listener?.onStateChange(info.state, null)
            }
        }
    }

    companion object {
        private const val UPDATE_LIMIT_DURATION = 500//限制进度更新频率，毫秒
    }
}