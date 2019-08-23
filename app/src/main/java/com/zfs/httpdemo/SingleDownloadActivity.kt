package com.zfs.httpdemo

import android.os.Bundle
import android.os.Environment
import cn.wandersnail.commons.util.ToastUtils
import cn.wandersnail.http.EasyHttp
import cn.wandersnail.http.TaskInfo
import cn.wandersnail.http.download.DownloadListener
import cn.wandersnail.http.download.DownloadWorker
import kotlinx.android.synthetic.main.activity_single_download.*
import java.io.File

/**
 *
 *
 * date: 2019/2/24 15:00
 * author: zengfansheng
 */
class SingleDownloadActivity : BaseActivity() {
    private val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "feiyuon.apk")
    private val info = DownInfo("http://app.feiyu-tech.com/CM/files/file/FyBtTool-release-v1.5.1_2018121801.apk", file.absolutePath)
    private var worker: DownloadWorker<DownInfo>? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_download)
        btnDownload.setOnClickListener { 
            worker = EasyHttp.download(info, object : DownloadListener<DownInfo> {
                override fun onStateChange(info: DownInfo, t: Throwable?) {
                    t?.printStackTrace()
                    val log = when (info.state) {
                        TaskInfo.State.IDLE -> "闲置状态"
                        TaskInfo.State.START -> "开始下载"
                        TaskInfo.State.ERROR -> "下载错误, ${t?.message}"
                        TaskInfo.State.COMPLETED -> "下载成功"
                        TaskInfo.State.CANCEL -> "下载取消"
                        TaskInfo.State.PAUSE -> "下载暂停"
                        TaskInfo.State.ONGOING -> "下载中..."
                    }
                    tvState.text = log
                    if (log.isNotEmpty()) {
                        ToastUtils.showShort(log)
                    }
                }

                override fun onProgress(info: DownInfo) {
                    progressBar.progress = (info.completionLength * progressBar.max / info.contentLength).toInt()
                }
            })
        }
        btnPause.setOnClickListener { worker?.pause() }
        btnResume.setOnClickListener { worker?.resume() }
        btnCancel.setOnClickListener { worker?.cancel() }
    }
    
    override fun onDestroy() {
        worker?.cancel()
        super.onDestroy()
    }
}