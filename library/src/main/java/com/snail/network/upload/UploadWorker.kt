package com.snail.network.upload

import com.snail.network.utils.HttpUtils
import com.snail.network.utils.SchedulerUtils
import io.reactivex.disposables.Disposable
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.net.URLEncoder

/**
 * 上传执行
 *
 * date: 2019/2/28 18:21
 * author: zengfansheng
 */
class UploadWorker<T> @JvmOverloads internal constructor(info: UploadInfo<T>, listener: UploadListener<T>? = null) : Disposable {
    private val observer = UploadObserver(info, listener)
    
    init {
        val service = Retrofit.Builder()
                .client(info.client ?: HttpUtils.initHttpsClient(true, OkHttpClient.Builder()).build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(info.baseUrl)
                .build()
                .create(UploadService::class.java)
        val builder = MultipartBody.Builder()
        info.paramParts.entries.forEach { 
            builder.addFormDataPart(it.key, it.value)
        }
        info.fileParts.entries.forEach {
            val part = MultipartBody.Part.createFormData(it.key, URLEncoder.encode(it.value.name, "utf-8"),
                    ProgressRequestBody(MediaType.parse("multipart/form-data"), it.key, it.value, observer))
            builder.addPart(part)
        }        
        service.upload(info.url, builder.build()).compose(SchedulerUtils.applyGeneralObservableSchedulers()).subscribe(observer)
    }

    override fun dispose() {
        observer.dispose()
    }

    override fun isDisposed(): Boolean {
        return observer.isDisposed
    }
    
    fun cancel() {
        dispose()
    }
}