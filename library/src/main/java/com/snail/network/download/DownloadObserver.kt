package com.snail.network.download


import android.annotation.SuppressLint
import com.snail.network.TaskInfo
import com.snail.network.callback.ProgressListener
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File

/**
 * 下载任务观察者
 * 
 * 时间: 2017/7/8 02:33
 * 作者: zengfansheng
 */
internal class DownloadObserver<T : DownloadInfo> @JvmOverloads constructor(private val info: T, private val listener: DownloadListener<T>? = null) : 
    Observer<Response<ResponseBody>>, ProgressListener {
    private var disposable: Disposable? = null
    private var lastUpdateTime: Long = 0//上次进度更新时间

    override fun onSubscribe(d: Disposable) {
        disposable = d
        info.state = TaskInfo.State.START
        listener?.onStateChange(info, null)
    }

    override fun onError(e: Throwable) {
        disposable = null
        info.state = TaskInfo.State.ERROR
        listener?.onStateChange(info, e)
    }

    override fun onProgress(progress: Long, max: Long) {
        AndroidSchedulers.mainThread().scheduleDirect {
            var completionLength = progress
            if (info.contentLength > max) {
                completionLength += info.contentLength - max
            } else {
                info.contentLength = max
            }
            info.completionLength = completionLength
            if (System.currentTimeMillis() - lastUpdateTime >= UPDATE_LIMIT_DURATION && (info.state == TaskInfo.State.IDLE ||
                            info.state == TaskInfo.State.START || info.state == TaskInfo.State.ONGOING)) {
                if (info.state != TaskInfo.State.ONGOING) {
                    info.state = TaskInfo.State.ONGOING
                    listener?.onStateChange(info, null)
                }
                updateProgress()
                lastUpdateTime = System.currentTimeMillis()
            }
        }
    }

    private fun updateProgress() {
        if (info.completionLength > 0 && info.contentLength > 0) {
            listener?.onProgress(info)
        }
    }

    fun dispose(cancel: Boolean) {
        AndroidSchedulers.mainThread().scheduleDirect {
            if (disposable != null && !disposable!!.isDisposed) {
                disposable?.dispose()
            }
            if (info.state == TaskInfo.State.ONGOING || info.state == TaskInfo.State.START) {
                if (cancel) {
                    info.state = TaskInfo.State.CANCEL
                    File(info.temporaryFilePath).delete()
                } else {
                    info.state = TaskInfo.State.PAUSE
                }
                listener?.onStateChange(info, null)
            }
        }
    }
    
    override fun onNext(t: Response<ResponseBody>) {
        
    }

    @SuppressLint("CheckResult")
    override fun onComplete() {
        disposable = null
        Schedulers.io().scheduleDirect {
            //将临时文件重命名为目标路径
            val destFile = File(info.savePath)
            destFile.delete()//如果目标有文件，删除
            val tempFile = File(info.temporaryFilePath)
            val success = tempFile.renameTo(destFile)
            if (!success) {
                tempFile.delete()
            }
            AndroidSchedulers.mainThread().scheduleDirect {
                if (success) {
                    //更新进度
                    info.completionLength = info.contentLength
                    updateProgress()
                    info.state = TaskInfo.State.COMPLETED
                    listener?.onStateChange(info, null)
                } else {
                    info.state = TaskInfo.State.ERROR
                    listener?.onStateChange(info, Throwable("Renaming to target file failed"))
                }
            }
        }
    }

    companion object {
        private const val UPDATE_LIMIT_DURATION = 500//限制进度更新频率，毫秒
    }
}
