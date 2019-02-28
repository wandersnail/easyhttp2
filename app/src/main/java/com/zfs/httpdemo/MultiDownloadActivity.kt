package com.zfs.httpdemo

import android.os.Bundle
import android.os.Environment
import com.snail.network.NetworkRequester
import com.snail.network.callback.MultiTaskListener
import com.snail.network.download.DownloadWorker
import kotlinx.android.synthetic.main.activity_multi_download.*
import java.io.File

/**
 *
 *
 * date: 2019/2/24 15:00
 * author: zengfansheng
 */
class MultiDownloadActivity : BaseActivity() {
    private val file1 = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "feiyuon1.apk")
    private val file2 = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "feiyuon2.apk")
    private val infos = arrayListOf(DownInfo("http://app.feiyu-tech.com:80/CM/files/file/FeiyuON3.0.0_2018092601.apk", file1.absolutePath),
            DownInfo("http://app.feiyu-tech.com:80/CM/files/file/FeiyuON-fullv3.0.2_2018093001.apk", file2.absolutePath))
    private var worker: DownloadWorker<DownInfo>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_download)
        btnDownload.setOnClickListener {
            worker = NetworkRequester.download(infos, object : MultiTaskListener<DownInfo> {
                override fun onTotalProgress(successCount: Int, failedCount: Int, total: Int) {
                    tvProgress.text = "成功: $successCount, 失败: $failedCount, 总下载任务: $total, 剩余: ${worker?.remaining() ?: infos.size}"
                }

                override fun onStateChange(info: DownInfo, t: Throwable?) {
                    
                }

                override fun onProgress(info: DownInfo) {
                    val index = infos.indexOf(info)
                    if (index == 0) {
                        progressBar1.progress = (info.completionLength * progressBar1.max / info.contentLength).toInt()
                    } else {
                        progressBar2.progress = (info.completionLength * progressBar2.max / info.contentLength).toInt()
                    }
                }
            })
        }
        btnPause.setOnClickListener { 
            worker?.pause()
        }
        btnResume.setOnClickListener { 
            worker?.resume()
        }
        btnPause1.setOnClickListener { 
            worker?.pause(infos[0])
        }
        btnResume1.setOnClickListener { 
            worker?.resume(infos[0])
        }
        btnPause2.setOnClickListener { 
            worker?.pause(infos[1])
        }
        btnResume2.setOnClickListener { 
            worker?.resume(infos[1])
        }
        btnCancel.setOnClickListener { worker?.cancel() }
    }
    
    override fun onDestroy() {
        worker?.cancel()
        super.onDestroy()
    }
}