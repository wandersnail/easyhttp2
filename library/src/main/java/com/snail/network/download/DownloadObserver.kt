package com.snail.network.download


import android.annotation.SuppressLint
import com.snail.network.TaskInfo
import com.snail.network.callback.TaskListener
import com.snail.network.callback.TaskObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File

/**
 * 下载任务观察者
 * 
 * 时间: 2017/7/8 02:33
 * 作者: zengfansheng
 * 邮箱: 43068145@qq.com
 */

internal class DownloadObserver<T : DownloadInfo> @JvmOverloads constructor(info: T, listener: TaskListener<T>? = null) : TaskObserver<T, T>(info, listener) {
    override fun onCancel() {
        File(info.temporaryFilePath).delete()
    }

    override fun onNext(t: T) {
        
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
                    handleSuccess()
                } else {
                    info.state = TaskInfo.State.ERROR
                    listener?.onStateChange(info, Throwable("Renaming to target file failed"))
                }
            }
        }
    }
}
