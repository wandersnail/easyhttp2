package com.snail.network

import com.google.gson.Gson
import com.snail.network.converter.Converter
import com.snail.network.download.DownloadInfo
import com.snail.network.download.DownloadListener
import com.snail.network.download.DownloadTask
import com.snail.network.download.MultiDownloadListener
import com.snail.network.utils.HttpUtils
import com.snail.network.utils.IOUtils
import com.snail.network.utils.SchedulerUtils
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.PartMap
import java.io.File
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
     * 设置自定义的Retrofit，如果不设置，请求使用默认的。对下载无效
     * 
     * @param baseUrl 每一个baseUrl对应一个Retrofit
     */
    fun setRetrofitBuilder(baseUrl: String, builder: RetrofitBuilder) {
        val url = HttpUtils.getBaseUrl(baseUrl)
        customRetrofitBuilderMap[url] = builder
        //替换缓存里已有的
        if (retrofitCache[url] != null) {
            retrofitCache[url] = RetrofitHolder(builder.build(), System.currentTimeMillis())
        }
    }
    
    //清理不活动的
    private fun cleanExpired() {
        //30s清理一次
        if (System.currentTimeMillis() - lastCheckTime >= 30000) {
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
    
    fun getRetrofit(baseUrl: String): Retrofit {
        val url = HttpUtils.getBaseUrl(baseUrl)
        var holder = retrofitCache[url]
        //如果缓存里没有，通过设置的RetrofitBuilder创建，如果没有设置，使用默认
        if (holder == null) {
            val builder = customRetrofitBuilderMap[url]
            holder = if (builder != null) {
                RetrofitHolder(builder.build(), System.currentTimeMillis())
            } else {
                RetrofitHolder(RetrofitBuilder.getDefaultRetrofit(url), System.currentTimeMillis())
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

    private fun <T> toJsonBeanObservable(cls: Class<T>, observable: Observable<ResponseBody>): Observable<T> {
        return observable.map {
            val jsonStr = IOUtils.toString(it.byteStream(), null)
            Gson().fromJson(jsonStr, cls)
        }
    }
    
    private fun <T> convertObservable(observable: Observable<ResponseBody>, converter: Converter<ResponseBody, T>): Observable<T> {
        return observable.map { converter.convert(it) }
    }
    
    private fun createHttpService(url: String): HttpService {
        return getRetrofit(url).create(HttpService::class.java)
    }
        
    private fun <T> subscribe(observable: Observable<T>, observer: Observer<T>? = null) {
        observable.compose(SchedulerUtils.applyObservableSchedulers()).subscribe(observer ?: object : Observer<T> {
            override fun onComplete() {
            }

            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(t: T) {
            }

            override fun onError(e: Throwable) {
            }
        })
    }
    
    /**
     * 普通GET请求
     */
    @JvmOverloads fun get(url: String, observer: Observer<ResponseBody>? = null) {
        subscribe(createHttpService(url).get(url), observer)
    }

    /**
     * 普通GET请求，响应数据格式为json
     * 
     * @param cls Json数据模型
     */
    @JvmOverloads fun <T> get(url: String, cls: Class<T>, observer: Observer<T>? = null) {
        val observable = createHttpService(url).get(url)
        subscribe(toJsonBeanObservable(cls, observable), observer)
    }

    /**
     * 普通GET请求
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmOverloads fun <T> get(url: String, converter: Converter<ResponseBody, T>, observer: Observer<T>? = null) {
        val observable = createHttpService(url).get(url)
        subscribe(convertObservable(observable, converter), observer)
    }

    /**
     * 普通POST请求
     */
    @JvmOverloads fun post(url: String, body: RequestBody, observer: Observer<ResponseBody>? = null) {
        subscribe(createHttpService(url).post(url, body), observer)
    }

    /**
     * 普通POST请求，响应数据格式为json
     * 
     * @param cls Json数据模型
     */
    @JvmOverloads fun <T> post(url: String, body: RequestBody, cls: Class<T>, observer: Observer<T>? = null) {
        val observable = createHttpService(url).post(url, body)
        subscribe(toJsonBeanObservable(cls, observable), observer)
    }

    /**
     * 普通POST请求
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmOverloads fun <T> post(url: String, body: RequestBody, converter: Converter<ResponseBody, T>, observer: Observer<T>? = null) {
        val observable = createHttpService(url).post(url, body)
        subscribe(convertObservable(observable, converter), observer)
    }

    /**
     * POST请求，body是json
     *
     * @param url 请求的url
     */
    @JvmOverloads fun postJson(url: String, json: String, observer: Observer<ResponseBody>? = null) {
        val requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        subscribe(createHttpService(url).postJson(url, requestBody), observer)
    }

    /**
     * POST请求，body是json，响应数据格式为json
     *
     * @param url 请求的url
     * @param cls Json数据模型
     */
    @JvmOverloads fun <T> postJson(url: String, json: String, cls: Class<T>, observer: Observer<T>? = null) {
        val requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        val observable = createHttpService(url).postJson(url, requestBody)
        subscribe(toJsonBeanObservable(cls, observable), observer)
    }

    /**
     * POST请求，body是json
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmOverloads fun <T> postJson(url: String, json: String, converter: Converter<ResponseBody, T>, observer: Observer<T>? = null) {
        val requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        val observable = createHttpService(url).postJson(url, requestBody)
        subscribe(convertObservable(observable, converter), observer)
    }

    /**
     * POST请求，body是字符串
     */
    @JvmOverloads fun postText(url: String, text: String, observer: Observer<ResponseBody>? = null) {
        val requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text)
        subscribe(createHttpService(url).post(url, requestBody), observer)
    }

    /**
     * POST请求，body是字符串，响应数据格式为json
     * 
     * @param cls Json数据模型
     */
    @JvmOverloads fun <T> postText(url: String, text: String, cls: Class<T>, observer: Observer<T>? = null) {
        val requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text)
        val observable = createHttpService(url).post(url, requestBody)
        subscribe(toJsonBeanObservable(cls, observable), observer)
    }

    /**
     * POST请求，body是字符串
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmOverloads fun <T> postText(url: String, text: String, converter: Converter<ResponseBody, T>, observer: Observer<T>? = null) {
        val requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text)
        val observable = createHttpService(url).post(url, requestBody)
        subscribe(convertObservable(observable, converter), observer)
    }

    /**
     * POST提交表单
     *
     * @param map 参数集合
     */
    @JvmOverloads fun postForm(url: String, map: Map<String, Any>, observer: Observer<ResponseBody>? = null) {
        subscribe(createHttpService(url).postForm(url, map), observer)
    }

    /**
     * POST提交表单，响应数据格式为json
     *
     * @param map 参数集合
     * @param cls Json数据模型
     */
    @JvmOverloads fun <T> postForm(url: String, map: Map<String, Any>, cls: Class<T>, observer: Observer<T>? = null) {
        val observable = createHttpService(url).postForm(url, map)
        subscribe(toJsonBeanObservable(cls, observable), observer)
    }

    /**
     * POST提交表单
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmOverloads fun <T> postForm(url: String, map: Map<String, Any>, converter: Converter<ResponseBody, T>, observer: Observer<T>? = null) {
        val observable = createHttpService(url).postForm(url, map)
        subscribe(convertObservable(observable, converter), observer)
    }

    private fun createFilePart(mediaType: MediaType?, file: File): MultipartBody.Part {
        return MultipartBody.Part.createFormData("file", file.name, RequestBody.create(mediaType, file))
    }

    /**
     * 上传文件
     */
    @JvmOverloads fun upload(url: String, file: File, observer: Observer<ResponseBody>? = null) {
        subscribe(createHttpService(url).upload(url, createFilePart(MediaType.parse("multipart/form-data"), file)), observer)
    }
    
    /**
     * 上传文件
     */
    @JvmOverloads fun upload(url: String, mediaType: MediaType?, file: File, observer: Observer<ResponseBody>? = null) {
        subscribe(createHttpService(url).upload(url, createFilePart(mediaType, file)), observer)
    }

    /**
     * 上传文件，响应数据格式为json
     * 
     * @param cls Json数据模型
     */
    @JvmOverloads fun <T> upload(url: String, mediaType: MediaType?, file: File, cls: Class<T>, observer: Observer<T>? = null) {
        val observable = createHttpService(url).upload(url, createFilePart(mediaType, file))
        subscribe(toJsonBeanObservable(cls, observable), observer)
    }

    /**
     * 上传文件，响应数据格式为json
     *
     * @param cls Json数据模型
     */
    @JvmOverloads fun <T> upload(url: String, file: File, cls: Class<T>, observer: Observer<T>? = null) {
        val observable = createHttpService(url).upload(url, createFilePart(MediaType.parse("multipart/form-data"), file))
        subscribe(toJsonBeanObservable(cls, observable), observer)
    }

    /**
     * 上传文件
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmOverloads fun <T> upload(url: String, mediaType: MediaType?, file: File, converter: Converter<ResponseBody, T>, observer: Observer<T>? = null) {
        val observable = createHttpService(url).upload(url, createFilePart(mediaType, file))
        subscribe(convertObservable(observable, converter), observer)
    }

    /**
     * 上传文件
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmOverloads fun <T> upload(url: String, file: File, converter: Converter<ResponseBody, T>, observer: Observer<T>? = null) {
        val observable = createHttpService(url).upload(url, createFilePart(MediaType.parse("multipart/form-data"), file))
        subscribe(convertObservable(observable, converter), observer)
    }

    /**
     * 上传文件，带参数
     */
    @JvmOverloads fun upload(url: String, @PartMap args: Map<String, RequestBody>, mediaType: MediaType?, file: File, observer: Observer<ResponseBody>? = null) {
        subscribe(createHttpService(url).upload(url, args, createFilePart(mediaType, file)), observer)
    }

    /**
     * 上传文件，带参数
     */
    @JvmOverloads fun upload(url: String, @PartMap args: Map<String, RequestBody>, file: File, observer: Observer<ResponseBody>? = null) {
        subscribe(createHttpService(url).upload(url, args, createFilePart(MediaType.parse("multipart/form-data"), file)), observer)
    }

    /**
     * 上传文件，带参数，响应数据格式为json
     * 
     * @param cls Json数据模型
     */
    @JvmOverloads fun <T> upload(url: String, @PartMap args: Map<String, RequestBody>, mediaType: MediaType?, file: File, cls: Class<T>, observer: Observer<T>? = null) {
        val observable = createHttpService(url).upload(url, args, createFilePart(mediaType, file))
        subscribe(toJsonBeanObservable(cls, observable), observer)
    }

    /**
     * 上传文件，带参数，响应数据格式为json
     *
     * @param cls Json数据模型
     */
    @JvmOverloads fun <T> upload(url: String, @PartMap args: Map<String, RequestBody>, file: File, cls: Class<T>, observer: Observer<T>? = null) {
        val observable = createHttpService(url).upload(url, args, createFilePart(MediaType.parse("multipart/form-data"), file))
        subscribe(toJsonBeanObservable(cls, observable), observer)
    }

    /**
     * 上传文件，带参数
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmOverloads fun <T> upload(url: String, @PartMap args: Map<String, RequestBody>, mediaType: MediaType?, file: File, converter: Converter<ResponseBody, T>, observer: Observer<T>? = null) {
        val observable = createHttpService(url).upload(url, args, createFilePart(mediaType, file))
        subscribe(convertObservable(observable, converter), observer)
    }

    /**
     * 上传文件，带参数
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmOverloads fun <T> upload(url: String, @PartMap args: Map<String, RequestBody>, file: File, converter: Converter<ResponseBody, T>, observer: Observer<T>? = null) {
        val observable = createHttpService(url).upload(url, args, createFilePart(MediaType.parse("multipart/form-data"), file))
        subscribe(convertObservable(observable, converter), observer)
    }
}