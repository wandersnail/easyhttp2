package com.snail.network.upload

import com.snail.network.ConvertedResponse
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.net.URLEncoder
import java.util.concurrent.Executors

/**
 * 同步的上传执行者
 *
 * date: 2019/5/17 11:49
 * author: zengfansheng
 */
internal class SyncUploadWorker<T> @JvmOverloads constructor(info: UploadInfo<T>, listener: UploadProgressListener? = null) {
    val convertedResponse: ConvertedResponse<T>
    
    private val localListener = object : UploadProgressListener {
        private val executor = Executors.newSingleThreadExecutor()
        
        override fun onProgress(name: String, progress: Long, max: Long) {
            executor.execute { listener?.onProgress(name, progress, max) }
        }
    }
    
    init {
        val builder = Retrofit.Builder()
        if (info.client != null) {
            builder.client(info.client)
        }
        val service = builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(info.baseUrl)
            .build()
            .create(UploadService::class.java)
        val parts = HashMap<String, @JvmSuppressWildcards RequestBody>()
        info.paramParts.entries.forEach { parts[it.key] = RequestBody.create(null, it.value) }
        info.fileParts.entries.forEach {
            MultipartBody.Part.createFormData(it.key, URLEncoder.encode(it.value.name, "utf-8"),
                ProgressRequestBody(MediaType.parse("multipart/form-data"), it.key, it.value, localListener)
            )
        }
        val call = service.uploadSync(info.url, parts)
        convertedResponse = ConvertedResponse(call)
        try {
            val response = call.execute()
            convertedResponse.raw = response.raw()
            if (response.isSuccessful) {
                try {
                    convertedResponse.convertedBody = info.converter.convert(response.body())
                } catch (t: Throwable) {
                    convertedResponse.convertRrror = t
                }
            }
        } catch (e: Exception) {
            //取消任务会抛异常
        }
    }
}