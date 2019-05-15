package com.zfs.httpdemo

import android.content.Intent
import android.os.Bundle
import com.snail.fileselector.FileSelector
import com.snail.fileselector.OnFileSelectListener
import com.snail.network.NetworkRequester
import com.snail.network.TaskInfo
import com.snail.network.converter.JsonResponseConverter
import com.snail.network.upload.UploadInfo
import com.snail.network.upload.UploadListener
import com.snail.network.upload.UploadWorker
import kotlinx.android.synthetic.main.activity_single_upload.*
import java.io.File

/**
 *
 *
 * date: 2019/3/1 10:14
 * author: zengfansheng
 */
class SingleUploadActivity : BaseActivity() {
    private val fileSelector = FileSelector()
    private var path = ""
    private var worker: UploadWorker<BaseResp>? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_upload)
        fileSelector.setSelectionMode(FileSelector.FILES_ONLY)
        fileSelector.setOnFileSelectListener(object : OnFileSelectListener {
            override fun onFileSelect(paths: List<String>) {
                path = paths[0]
                tvPath.text = path
            }
        })
        btnSelectFile.setOnClickListener { 
            fileSelector.select(this)
        }
        btnUpload.setOnClickListener { 
            if (path.isNotEmpty()) {
                worker?.cancel()
                val args = HashMap<String, String>()
                args["upload"] = "Hello"
                val files = HashMap<String, File>()
                files["file"] = File(path)
                val url = "https://www.blindx.cn/smart/customer/log"
                val converter = JsonResponseConverter(BaseResp::class.java)
                val info = UploadInfo(url, converter, args, files)
                worker = NetworkRequester.upload(info, object : UploadListener {
                    override fun onStateChange(state: TaskInfo.State, t: Throwable?) {
                        t?.printStackTrace()
                        val log = when (state) {
                            TaskInfo.State.IDLE -> "闲置状态"
                            TaskInfo.State.START -> "开始上传"
                            TaskInfo.State.ERROR -> "上传错误, ${t?.message}"
                            TaskInfo.State.COMPLETED -> {
                                tvResponse.text = info.response?.toString()
                                "上传成功"
                            }
                            TaskInfo.State.CANCEL -> "上传取消"
                            TaskInfo.State.PAUSE -> "上传暂停"
                            TaskInfo.State.ONGOING -> "上传中..."
                        }
                        tvState.text = log
                    }

                    override fun onProgress(name: String, progress: Long, max: Long) {
                        progressBar.progress = (progress * progressBar.max / max).toInt()
                    }
                })
            }
        }
        btnCancel.setOnClickListener { 
            worker?.cancel()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fileSelector.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        worker?.cancel()
    }
}