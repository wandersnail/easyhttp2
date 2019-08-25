package com.zfs.httpdemo

import android.content.Intent
import android.os.Bundle
import cn.wandersnail.fileselector.FileSelector
import cn.wandersnail.http.EasyHttp
import cn.wandersnail.http.TaskInfo
import cn.wandersnail.http.converter.JsonResponseConverter
import cn.wandersnail.http.upload.UploadInfo
import cn.wandersnail.http.upload.UploadListener
import cn.wandersnail.http.upload.UploadWorker
import kotlinx.android.synthetic.main.activity_single_upload.*
import okhttp3.Response
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
        fileSelector.setOnFileSelectListener { paths ->
            path = paths[0]
            tvPath.text = path
        }
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
                val info = UploadInfo<BaseResp>(url, files)
                info.setParamParts(args)
                info.setConverter(JsonResponseConverter(BaseResp::class.java))
                worker = EasyHttp.enqueueUpload(info, object : UploadListener<BaseResp> {
                    override fun onResponseBodyParse(response: Response, convertedBody: BaseResp?) {
                        tvResponse.text = response.toString()
                    }

                    override fun onStateChange(state: TaskInfo.State, t: Throwable?) {
                        t?.printStackTrace()
                        val log = when (state) {
                            TaskInfo.State.IDLE -> "闲置状态"
                            TaskInfo.State.START -> "开始上传"
                            TaskInfo.State.ERROR -> "上传错误, ${t?.message}"
                            TaskInfo.State.COMPLETED -> {
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