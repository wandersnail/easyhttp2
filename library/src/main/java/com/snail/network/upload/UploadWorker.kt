package com.snail.network.upload

import com.snail.network.TaskWorker
import com.snail.network.callback.MultiTaskListener
import com.snail.network.callback.TaskListener
import com.snail.network.utils.HttpUtils
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 *
 *
 * date: 2019/2/28 18:21
 * author: zengfansheng
 */
class UploadWorker<R, T : UploadInfo<R>> : TaskWorker<R, T> {
    internal constructor(info: T, listener: TaskListener<T>?) : super(info, listener)

    internal constructor(infos: List<T>, listener: MultiTaskListener<T>?) : super(infos, listener)

    override fun execute(info: T) {
        //如果listener为空，说明不需要监听，不为空则在本地监听后，再传出去
        val observer = UploadObserver(info, if (listener == null) null else LocalTaskListener())
        taskMap[info] = observer
        val client = HttpUtils.initHttpsClient(true, OkHttpClient.Builder())
            .readTimeout(5, TimeUnit.SECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            .build()
        val service = Retrofit.Builder()
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(info.baseUrl)
            .build()
            .create(UploadService::class.java)
        val observable = if (info.args == null) {//不带参数
            service.upload(info.url, createFilePart(info.mediaType, info.file))
        } else {//带参数
            service.upload(info.url, info.args, createFilePart(info.mediaType, info.file))
        }
        when {
            info.cls != null -> {//响应数据格式为json
                HttpUtils.subscribe(HttpUtils.toJsonBeanObservable(info.cls, observable), observer)
            }
            info.converter != null -> {//响应体转换器
                HttpUtils.subscribe(HttpUtils.convertObservable(observable, info.converter), observer)
            }
        }
    }
    
    private fun createFilePart(mediaType: MediaType?, file: File): MultipartBody.Part {
        return MultipartBody.Part.createFormData("file", file.name,
                RequestBody.create(mediaType ?: MediaType.parse("multipart/form-data"), file))
    }
}