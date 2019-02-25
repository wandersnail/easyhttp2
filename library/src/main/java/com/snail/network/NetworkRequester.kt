package com.snail.network

import com.google.gson.Gson
import com.snail.network.download.DownloadInfo
import com.snail.network.download.DownloadListener
import com.snail.network.download.DownloadTask
import com.snail.network.download.MultiDownloadListener
import com.snail.network.general.HttpService
import com.snail.network.utils.IOUtils
import com.snail.network.utils.SchedulerUtils
import io.reactivex.Observable
import io.reactivex.Observer
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Part
import retrofit2.http.PartMap


/**
 *
 *
 * date: 2019/2/23 16:37
 * author: zengfansheng
 */
object NetworkRequester {

    /**
     * 单个下载
     *
     * @param info 下载信息
     * @param listener 下载监听
     */
    fun <T : DownloadInfo> download(info: T, listener: DownloadListener<T>?): DownloadTask<T> {
        return DownloadTask(info, listener)
    }

    /**
     * 多个同时下载
     *
     * @param infos 下载信息
     * @param listener 下载监听
     */
    fun <T : DownloadInfo> download(infos: List<T>, listener: MultiDownloadListener<T>?): DownloadTask<T> {
        return DownloadTask(infos, listener)
    }

    private fun <T> toJsonModelObservable(cls: Class<T>, observable: Observable<ResponseBody>): Observable<T> {
        return observable.map {
            val jsonStr = IOUtils.toString(it.byteStream(), null)
            Gson().fromJson(jsonStr, cls)
        }
    }
    
    private fun createHttpService(url: String): HttpService {
        return RetrofitBuilder.getDefaultRetrofit(url).create(HttpService::class.java)
    }
        
    private fun <T> subscribe(observable: Observable<T>, observer: Observer<T>) {
        observable.compose(SchedulerUtils.applyObservableSchedulers()).subscribe(observer)
    }
    
    /**
     * 普通GET请求
     */
    fun get(url: String, observer: Observer<ResponseBody>) {
        subscribe(createHttpService(url).get(url), observer)
    }

    /**
     * 普通GET请求，响应数据格式为json
     * 
     * @param cls Json数据模型
     */
    fun <T> get(url: String, cls: Class<T>, observer: Observer<T>) {
        val observable = createHttpService(url).get(url)
        subscribe(toJsonModelObservable(cls, observable), observer)
    }

    /**
     * 普通POST请求
     */
    fun post(url: String, body: RequestBody, observer: Observer<ResponseBody>) {
        subscribe(createHttpService(url).post(url, body), observer)
    }

    /**
     * 普通POST请求，响应数据格式为json
     * 
     * @param cls Json数据模型
     */
    fun <T> post(url: String, body: RequestBody, cls: Class<T>, observer: Observer<T>) {
        val observable = createHttpService(url).post(url, body)
        subscribe(toJsonModelObservable(cls, observable), observer)
    }

    /**
     * POST请求，body是json
     *
     * @param url 请求的url
     */
    fun postJson(url: String, json: String, observer: Observer<ResponseBody>) {
        val requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        subscribe(createHttpService(url).postJson(url, requestBody), observer)
    }

    /**
     * POST请求，body是json，响应数据格式为json
     *
     * @param url 请求的url
     * @param cls Json数据模型
     */
    fun <T> postJson(url: String, json: String, cls: Class<T>, observer: Observer<T>) {
        val requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        val observable = createHttpService(url).postJson(url, requestBody)
        subscribe(toJsonModelObservable(cls, observable), observer)
    }

    /**
     * POST请求，body是字符串
     */
    fun postText(url: String, text: String, observer: Observer<ResponseBody>) {
        val requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text)
        subscribe(createHttpService(url).post(url, requestBody), observer)
    }

    /**
     * POST请求，body是字符串，响应数据格式为json
     * 
     * @param cls Json数据模型
     */
    fun <T> postText(url: String, text: String, cls: Class<T>, observer: Observer<T>) {
        val requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text)
        val observable = createHttpService(url).post(url, requestBody)
        subscribe(toJsonModelObservable(cls, observable), observer)
    }

    /**
     * POST提交表单
     *
     * @param map 参数集合
     */
    fun postForm(url: String, map: Map<String, Any>, observer: Observer<ResponseBody>) {
        subscribe(createHttpService(url).postForm(url, map), observer)
    }

    /**
     * POST提交表单，响应数据格式为json
     *
     * @param map 参数集合
     * @param cls Json数据模型
     */
    fun <T> postForm(url: String, map: Map<String, Any>, cls: Class<T>, observer: Observer<T>) {
        val observable = createHttpService(url).postForm(url, map)
        subscribe(toJsonModelObservable(cls, observable), observer)
    }

    /**
     * 上传文件
     */
    fun upload(url: String, @Part filePart: MultipartBody.Part, observer: Observer<ResponseBody>) {
        subscribe(createHttpService(url).upload(url, filePart), observer)
    }

    /**
     * 上传文件，响应数据格式为json
     * 
     * @param cls Json数据模型
     */
    fun <T> upload(url: String, @Part filePart: MultipartBody.Part, cls: Class<T>, observer: Observer<T>) {
        val observable = createHttpService(url).upload(url, filePart)
        subscribe(toJsonModelObservable(cls, observable), observer)
    }

    /**
     * 上传文件，带参数
     */
    fun upload(url: String, @PartMap args: Map<String, RequestBody>, @Part filePart: MultipartBody.Part, observer: Observer<ResponseBody>) {
        subscribe(createHttpService(url).upload(url, args, filePart), observer)
    }

    /**
     * 上传文件，带参数，响应数据格式为json
     * 
     * @param cls Json数据模型
     */
    fun <T> upload(url: String, @PartMap args: Map<String, RequestBody>, @Part filePart: MultipartBody.Part, cls: Class<T>, observer: Observer<T>) {
        val observable = createHttpService(url).upload(url, args, filePart)
        subscribe(toJsonModelObservable(cls, observable), observer)
    }
}