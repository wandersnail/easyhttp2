package com.zfs.httpdemo

import android.os.Bundle
import android.os.Environment
import com.snail.commons.utils.ToastUtils
import com.snail.network.NetworkManager
import com.snail.network.download.DownloadInfo
import com.snail.network.download.DownloadListener
import com.snail.network.download.DownloadTask
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
    private val info = DownInfo("http://app.feiyu-tech.com:80/CM/files/file/FeiyuON-v3.1.1_2019022201.apk", file.absolutePath)
    private var task: DownloadTask<DownInfo>? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_download)
        btnDownload.setOnClickListener { 
            task = NetworkManager.instance.download(info, object : DownloadListener<DownInfo> {
                override fun onStateChange(info: DownInfo, t: Throwable?) {
                    t?.printStackTrace()
                    val log = when (info.state) {
                        DownloadInfo.State.IDLE -> "闲置状态"
                        DownloadInfo.State.START -> "开始下载"
                        DownloadInfo.State.ERROR -> "下载错误, ${t?.message}"
                        DownloadInfo.State.COMPLETED -> "下载成功"
                        DownloadInfo.State.CANCEL -> "下载取消"
                        DownloadInfo.State.PAUSE -> "下载暂停"
                        DownloadInfo.State.DOWNLOADING -> "下载中..."
                    }
                    tvState.text = log
                    if (log.isNotEmpty()) {
                        ToastUtils.showShort(log)
                    }
                }

                override fun onProgress(info: DownInfo) {
                    progressBar.progress = (info.readLength * progressBar.max / info.contentLength).toInt()
                }
            })
        }
        btnPause.setOnClickListener { task?.pause() }
        btnResume.setOnClickListener { task?.resume() }
        btnCancel.setOnClickListener { task?.cancel() }
    }
    
    override fun onDestroy() {
        task?.cancel()
        super.onDestroy()
    }
}