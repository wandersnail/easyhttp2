package com.snail.network.download


import android.annotation.SuppressLint
import com.snail.network.TaskInfo
import com.snail.network.callback.TaskObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File

/**
 * 时间: 2017/7/8 02:33
 * 作者: zengfansheng
 * 邮箱: 43068145@qq.com
 * 功能: 各种状态监听
 */

internal class DownloadObserver<T : DownloadInfo> @JvmOverloads constructor(info: T, listener: DownloadListener<T>? = null) : TaskObserver<T>(info, listener) {
    override fun onCancel() {
        File(info.temporaryFilePath).delete()
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
                    info.state = TaskInfo.State.COMPLETED
                    //更新进度
                    listener?.onProgress(info)
                    listener?.onStateChange(info, null)
                } else {
                    info.state = TaskInfo.State.ERROR
                    listener?.onStateChange(info, Throwable("Renaming to target file failed"))
                }
            }
        }
    }
}
