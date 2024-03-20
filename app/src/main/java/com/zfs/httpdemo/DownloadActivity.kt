package com.zfs.httpdemo

import android.os.Bundle
import cn.wandersnail.http.EasyHttp
import cn.wandersnail.http.TaskInfo
import cn.wandersnail.http.download.DownloadInfo
import cn.wandersnail.http.download.DownloadListener
import cn.wandersnail.http.download.DownloadWorker
import com.zfs.httpdemo.databinding.DownloadActivityBinding
import java.io.File

/**
 * Created by 曾繁盛 on 2024/3/20 17:25
 */
class DownloadActivity : ViewBindingActivity<DownloadActivityBinding>() {
    override fun getViewBindingClass(): Class<DownloadActivityBinding> {
        return DownloadActivityBinding::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fileName = "7940HS驱动.zip"
        val url = "http://192.168.0.107:9991/file/download/$fileName"
        var worker: DownloadWorker<DownloadInfo>? = null
        val savePath = "/sdcard/$fileName"
        binding.btnStart.setOnClickListener {
            worker = EasyHttp.singleDownloadWorkerBuilder()
                .setFileInfo(url, savePath)
                .setListener(object : DownloadListener<DownloadInfo> {
                    override fun onStateChange(info: DownloadInfo, t: Throwable?) {
                        binding.tvState.text = getStateString(info.state, t)
                    }

                    override fun onProgress(info: DownloadInfo) {
                        binding.progressBar.progress =
                            (info.completionLength * 100 / info.contentLength).toInt()
                    }
                })
                .buildAndDownload()
        }
        binding.btnCancel.setOnClickListener {
            worker?.cancel()
        }
    }

    private fun getStateString(state: TaskInfo.State, t: Throwable?): String {
        return when (state) {
            TaskInfo.State.IDLE -> "未开始"
            TaskInfo.State.START -> "下载开始"
            TaskInfo.State.ONGOING -> "下载中"
            TaskInfo.State.COMPLETED -> "下载完成"
            TaskInfo.State.CANCEL -> "下载取消"
            TaskInfo.State.ERROR -> "下载错误：${t?.message}"
            TaskInfo.State.PAUSE -> "下载暂停"
        }
    }
}