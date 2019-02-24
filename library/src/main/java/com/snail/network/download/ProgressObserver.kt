package com.snail.network.download


import android.annotation.SuppressLint
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File

/**
 * 时间: 2017/7/8 02:33
 * 作者: zengfansheng
 * 邮箱: 43068145@qq.com
 * 功能: 各种状态监听
 */

internal class ProgressObserver<T : DownloadInfo> @JvmOverloads constructor(private val info: T,
                       private val listener: DownloadListener<T>? = null) : 
        ProgressListener, Observer<DownloadInfo> {
    private var disposable: Disposable? = null
    private var lastUpdateTime: Long = 0//上次进度更新时间

    override fun onSubscribe(d: Disposable) {
        disposable = d
        info.state = DownloadInfo.State.START        
        listener?.onStateChange(info, null)
    }

    override fun onNext(next: DownloadInfo) {
        if (info.readLength > 0) {
            listener?.onProgress(info)
        }
    }

    override fun onError(t: Throwable) {
        info.state = DownloadInfo.State.ERROR
        listener?.onStateChange(info, t)        
    }

    @SuppressLint("CheckResult")
    override fun onComplete() {
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
                    info.state = DownloadInfo.State.COMPLETED
                    //更新进度
                    listener?.onProgress(info)
                    listener?.onStateChange(info, null)
                } else {
                    info.state = DownloadInfo.State.ERROR
                    listener?.onStateChange(info, Throwable("Renaming to target file failed"))
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    override fun onProgress(readLength: Long, contentLength: Long) {
        AndroidSchedulers.mainThread().scheduleDirect {
            var readLen = readLength
            if (info.contentLength > contentLength) {
                readLen += info.contentLength - contentLength
            } else {
                info.contentLength = contentLength
            }
            info.readLength = readLen
            if (System.currentTimeMillis() - lastUpdateTime >= UPDATE_LIMIT_DURATION && (info.state == DownloadInfo.State.IDLE ||
                            info.state == DownloadInfo.State.START || info.state == DownloadInfo.State.DOWNLOADING)) {
                info.state = DownloadInfo.State.DOWNLOADING
                listener?.onProgress(info)
                lastUpdateTime = System.currentTimeMillis()
            }
        }
    }
    
    @SuppressLint("CheckResult")
    fun dispose(cancel: Boolean) {
        AndroidSchedulers.mainThread().scheduleDirect {
            disposable?.dispose()
            if (info.state == DownloadInfo.State.DOWNLOADING || info.state == DownloadInfo.State.START) {
                if (cancel) {
                    info.state = DownloadInfo.State.CANCEL
                    File(info.temporaryFilePath).delete()
                } else {
                    info.state = DownloadInfo.State.PAUSE
                }
                listener?.onStateChange(info, null)
            }
        }
    }
    
    companion object {
        private const val UPDATE_LIMIT_DURATION = 500//限制进度更新频率，毫秒
    }
}
