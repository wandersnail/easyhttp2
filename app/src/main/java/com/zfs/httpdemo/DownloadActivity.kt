package com.zfs.httpdemo

import android.os.Bundle
import android.util.Log
import cn.wandersnail.http.EasyHttp
import cn.wandersnail.http.TaskInfo
import cn.wandersnail.http.download.DownloadInfo
import cn.wandersnail.http.download.DownloadListener
import cn.wandersnail.http.download.DownloadWorker
import cn.wandersnail.http.download.RangeDownloadWorker
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.zfs.httpdemo.databinding.DownloadActivityBinding

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

                    override fun onProgress(info: DownloadInfo, progress: Int) {
                        binding.progressBar.progress = progress
                    }
                })
                .buildAndDownload()
        }
        binding.btnCancel.setOnClickListener {
            worker?.cancel()
        }
        val gson: Gson = GsonBuilder()
            .disableHtmlEscaping()
            .create()
        var blockWorker: RangeDownloadWorker<DownloadInfo>? = null
        binding.btnStartBlock.setOnClickListener {
            blockWorker = EasyHttp.rangeDownloadWorkerBuilder()
                .setFileInfo(url, savePath)
                .setListener(object : DownloadListener<DownloadInfo> {
                    override fun onStateChange(info: DownloadInfo, t: Throwable?) {
                        binding.tvStateBlock.text = getStateString(info.state, t)
                    }

                    override fun onProgress(info: DownloadInfo, progress: Int) {
                        binding.progressBarBlock.progress = progress
                        Log.d("DownloadActivity", "readBytes = ${info.completionLength}，contentLength = ${info.contentLength}")
                    }
                })
                .setRangeHeaderName("Snail-Range")
                .download()
        }
        binding.btnCancelBlock.setOnClickListener {
            blockWorker?.cancel()
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