package com.zfs.httpdemo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContracts
import cn.wandersnail.commons.util.ToastUtils
import cn.wandersnail.http.EasyHttp
import cn.wandersnail.http.TaskInfo
import cn.wandersnail.http.converter.StringResponseConverter
import cn.wandersnail.http.upload.FileInfo
import cn.wandersnail.http.upload.UploadListener
import cn.wandersnail.http.upload.UploadWorker
import com.zfs.httpdemo.databinding.UploadActivityBinding
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import java.util.concurrent.TimeUnit


/**
 * Created by 曾繁盛 on 2023/12/23 14:17
 */
class UploadActivity : ViewBindingActivity<UploadActivityBinding>() {
    override fun getViewBindingClass(): Class<UploadActivityBinding> {
        return UploadActivityBinding::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var uri: Uri? = null
        var worker: UploadWorker<String>? = null
        val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                if (result?.data?.data != null) {
                    uri = result.data!!.data!!
                    contentResolver.query(uri!!,null,null,null,null)?.use { cursor ->
                        cursor.moveToFirst()
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        binding.tvPath.text = cursor.getString(index)
                    }
                }
            }
        }
        binding.btnSelectFile.setOnClickListener {
            binding.progressBar.progress = 0
            binding.tvPath.text = ""
            selectImageLauncher.launch(Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            })
        }
        binding.btnCancel.setOnClickListener {
            worker?.cancel()
        }
        binding.btnUpload.setOnClickListener {
            try {
                val uri1 = uri ?: return@setOnClickListener
                val inputStream = contentResolver.openInputStream(uri1)!!
                val filename = binding.tvPath.text.toString()
                worker = EasyHttp.uploadWorkerBuilder(String::class.java)
                    .setUrl("http://192.168.0.107:9991/file/upload")
                    .setClient(getClient())
                    .setConverter(StringResponseConverter())
                    .setFileParts(listOf(FileInfo.Builder()
                        .setFilename(filename)
                        .setMediaType(MediaType.parse(getMimeType(filename)))
                        .setInputStream(inputStream).build()))
                    .setListener(object : UploadListener<String> {
                        override fun onProgress(fileInfo: FileInfo, progress: Long, max: Long) {
                            if (binding.progressBar.max != max.toInt()) {
                                binding.progressBar.max = max.toInt()
                            }
                            binding.progressBar.progress = progress.toInt()
                        }

                        override fun onStateChange(state: TaskInfo.State, t: Throwable?) {
                            if (state == TaskInfo.State.ERROR || state == TaskInfo.State.CANCEL) {
                                ToastUtils.showShort("上传失败")
                            } else if (state == TaskInfo.State.COMPLETED) {
                                ToastUtils.showShort("上传成功")
                            }
                        }

                        override fun onResponseBodyParse(
                            response: Response<ResponseBody>,
                            convertedResponse: String?
                        ) {
                            binding.tvResponse.text = if (convertedResponse?.isNotBlank() == true) convertedResponse else {
                                response.errorBody()?.string()
                            }
                        }
                    })
                    .buildAndUpload()
            } catch (e: Exception) {
                ToastUtils.showShort("文件打开失败")
            }
        }
    }

    fun getMimeType(filename: String): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(filename) ?: return "application/octet-stream"
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
    }

    private fun getClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(5, TimeUnit.SECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            .addNetworkInterceptor(MyHttpInterceptor())
            .build()
    }
}