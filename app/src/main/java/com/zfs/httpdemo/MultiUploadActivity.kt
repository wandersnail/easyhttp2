package com.zfs.httpdemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.snail.commons.utils.FileUtils
import com.snail.fileselector.FileSelector
import com.snail.fileselector.OnFileSelectListener
import com.snail.network.NetworkRequester
import com.snail.network.TaskInfo
import com.snail.network.callback.MultiTaskListener
import com.snail.network.converter.JsonResponseConverter
import com.snail.network.upload.UploadInfo
import com.snail.network.upload.UploadWorker
import kotlinx.android.synthetic.main.activity_multi_upload.*
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.File

/**
 *
 *
 * date: 2019/3/1 10:14
 * author: zengfansheng
 */
class MultiUploadActivity : BaseActivity() {
    private val fileSelector = FileSelector()
    private var worker: UploadWorker<BaseResp, UploadInfo<BaseResp>>? = null
    private var paths = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_upload)
        fileSelector.setMultiSelectionEnabled(true)
        fileSelector.setSelectionMode(FileSelector.FILES_ONLY)
        btnCancel.setOnClickListener {
            worker?.cancel()
        }
        fileSelector.setOnFileSelectListener(object : OnFileSelectListener {
            override fun onFileSelect(paths: List<String>) {
                this@MultiUploadActivity.paths.removeAll(paths)
                this@MultiUploadActivity.paths.addAll(paths)
                this@MultiUploadActivity.paths.forEach {
                    tvPath.append(it)
                    tvPath.append("\n")
                }
            }
        })
        btnSelectFile.setOnClickListener {
            fileSelector.select(this)
        }
        btnUpload.setOnClickListener {
            if (paths.isNotEmpty()) {
                worker?.cancel()
                val infos = ArrayList<UploadInfo<BaseResp>>()
                paths.forEach {
                    val args = HashMap<String, RequestBody>()
                    args["upload"] = RequestBody.create(MediaType.parse("text/plain"), FileUtils.getFileName(it))
                    val file = File(it)
                    val url = "http://192.168.137.1:8080/testupload"
                    val converter = JsonResponseConverter(BaseResp::class.java)
                    infos.add(UploadInfo(url, file, converter, null, args))
                }
                worker = NetworkRequester.upload(infos, object : MultiTaskListener<UploadInfo<BaseResp>> {
                    override fun onStateChange(info: UploadInfo<BaseResp>, t: Throwable?) {
                        t?.printStackTrace()
                        val log = when (info.state) {
                            TaskInfo.State.IDLE -> "闲置状态"
                            TaskInfo.State.START -> "开始上传"
                            TaskInfo.State.ERROR -> "上传错误, ${t?.message}"
                            TaskInfo.State.COMPLETED -> {
                                tvResponse.append(info.response?.toString())
                                tvResponse.append("\n")
                                "上传成功"
                            }
                            TaskInfo.State.CANCEL -> "上传取消"
                            TaskInfo.State.PAUSE -> "上传暂停"
                            TaskInfo.State.ONGOING -> "上传中..."
                        }
                        Log.e("MultiUpload", log)
                    }

                    override fun onProgress(info: UploadInfo<BaseResp>) {
                        
                    }

                    override fun onTotalProgress(successCount: Int, failedCount: Int, total: Int) {
                        tvProgress.text = "成功: $successCount, 失败: $failedCount, 总上传任务: $total, 剩余: ${worker?.remaining() ?: infos.size}"
                    }
                })
            }
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