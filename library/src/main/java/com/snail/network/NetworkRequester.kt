package com.snail.network

import com.snail.network.callback.MultiTaskListener
import com.snail.network.callback.TaskListener
import com.snail.network.converter.ResponseConverter
import com.snail.network.download.DownloadInfo
import com.snail.network.download.DownloadWorker
import com.snail.network.upload.UploadInfo
import com.snail.network.upload.UploadWorker
import com.snail.network.utils.HttpUtils
import io.reactivex.Observer
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import java.util.concurrent.ConcurrentHashMap


/**
 *
 *
 * date: 2019/2/23 16:37
 * author: zengfansheng
 */
object NetworkRequester {
    /**
     * @param activeTime 上次使用时间
     */
    private data class RetrofitHolder(val retrofit: Retrofit, var activeTime: Long)
    
    //缓存Retrofit
    private val retrofitCache = ConcurrentHashMap<String, RetrofitHolder>()
    
    //自定义的Retrofit
    private val customRetrofitBuilderMap = ConcurrentHashMap<String, RetrofitBuilder>()
    private var lastCheckTime = System.currentTimeMillis()

    /**
     * 设置自定义的Retrofit，如果不设置，请求使用默认的。对下载和上传无效
     * 
     * @param baseUrl 每一个baseUrl对应一个Retrofit
     */
    fun setRetrofitBuilder(baseUrl: String, builder: RetrofitBuilder) {
        val url = HttpUtils.getBaseUrl(baseUrl)
        customRetrofitBuilderMap[url] = builder
        //替换缓存里已有的
        if (retrofitCache[url] != null) {
            retrofitCache[url] = RetrofitHolder(builder.baseUrl(baseUrl).build(), System.currentTimeMillis())
        }
    }
    
    //清理不活动的
    private fun cleanExpired() {
        //30s清理一次
        if (System.currentTimeMillis() - lastCheckTime >= 30000) {
            lastCheckTime = System.currentTimeMillis()
            val iterator = retrofitCache.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                //1分钟不活动则清理掉
                if (System.currentTimeMillis() - entry.value.activeTime >= 60000) {
                    iterator.remove()
                }
            }
        }        
    }
    
    private fun getRetrofit(baseUrl: String): Retrofit {
        val url = HttpUtils.getBaseUrl(baseUrl)
        var holder = retrofitCache[url]
        //如果缓存里没有，通过设置的RetrofitBuilder创建，如果没有设置，使用默认
        if (holder == null) {
            val builder = customRetrofitBuilderMap[url]
            holder = if (builder != null) {
                RetrofitHolder(builder.build(), System.currentTimeMillis())
            } else {
                RetrofitHolder(RetrofitBuilder.getDefaultRetrofitBuilder(url).build(), System.currentTimeMillis())
            }
        } else {
            holder.activeTime = System.currentTimeMillis()
        }
        cleanExpired()
        return holder.retrofit
    }
    
    /**
     * 单个下载
     *
     * @param info 下载信息
     * @param listener 下载监听
     */
    fun <T : DownloadInfo> download(info: T, listener: TaskListener<T>?): DownloadWorker<T> {
        return DownloadWorker(info, listener)
    }

    /**
     * 多个同时下载
     *
     * @param infos 下载信息
     * @param listener 下载监听
     */
    fun <T : DownloadInfo> download(infos: List<T>, listener: MultiTaskListener<T>?): DownloadWorker<T> {
        return DownloadWorker(infos, listener)
    }

    private fun createHttpService(url: String): HttpService {
        return getRetrofit(url).create(HttpService::class.java)
    }
    
    /**
     * 普通GET请求
     */
    @JvmOverloads fun get(url: String, observer: Observer<ResponseBody>? = null) {
        HttpUtils.subscribe(createHttpService(url).get(url), observer)
    }

    /**
     * 普通GET请求
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    fun <T> get(url: String, converter: ResponseConverter<T>, observer: Observer<T>) {
        val observable = createHttpService(url).get(url)
        HttpUtils.subscribe(HttpUtils.convertObservable(observable, converter), observer)
    }

    /**
     * POST请求，body是json
     *
     * @param url 请求的url
     */
    @JvmOverloads fun postJson(url: String, json: String, observer: Observer<ResponseBody>? = null) {
        val requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        HttpUtils.subscribe(createHttpService(url).postJson(url, requestBody), observer)
    }

    /**
     * POST请求，body是json
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    fun <T> postJson(url: String, json: String, converter: ResponseConverter<T>, observer: Observer<T>) {
        val requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        val observable = createHttpService(url).postJson(url, requestBody)
        HttpUtils.subscribe(HttpUtils.convertObservable(observable, converter), observer)
    }

    /**
     * POST请求，body是字符串
     */
    @JvmOverloads fun postText(url: String, text: String, observer: Observer<ResponseBody>? = null) {
        val requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text)
        HttpUtils.subscribe(createHttpService(url).post(url, requestBody), observer)
    }

    /**
     * POST请求，body是字符串
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    fun <T> postText(url: String, text: String, converter: ResponseConverter<T>, observer: Observer<T>) {
        val requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text)
        val observable = createHttpService(url).post(url, requestBody)
        HttpUtils.subscribe(HttpUtils.convertObservable(observable, converter), observer)
    }

    /**
     * POST提交表单
     *
     * @param map 参数集合
     */
    @JvmOverloads fun postForm(url: String, map: Map<String, Any>, observer: Observer<ResponseBody>? = null) {
        HttpUtils.subscribe(createHttpService(url).postForm(url, map), observer)
    }

    /**
     * POST提交表单
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    fun <T> postForm(url: String, map: Map<String, Any>, converter: ResponseConverter<T>, observer: Observer<T>) {
        val observable = createHttpService(url).postForm(url, map)
        HttpUtils.subscribe(HttpUtils.convertObservable(observable, converter), observer)
    }

    /**
     * 上传单个文件
     */
    fun <R, T : UploadInfo<R>> upload(info: T, listener: TaskListener<T>?): UploadWorker<R, T> {
        return UploadWorker(info, listener)
    }

    /**
     * 批量上传
     */
    fun <R, T : UploadInfo<R>> upload(infos: List<T>, listener: MultiTaskListener<T>?): UploadWorker<R, T> {
        return UploadWorker(infos, listener)
    }    
}