package com.snail.network

import com.snail.network.callback.RequestCallback
import com.snail.network.converter.OriginalResponseConverter
import com.snail.network.converter.ResponseConverter
import com.snail.network.download.DownloadInfo
import com.snail.network.download.DownloadListener
import com.snail.network.download.DownloadWorker
import com.snail.network.download.MultiDownloadListener
import com.snail.network.upload.*
import com.snail.network.utils.HttpUtils
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.functions.Consumer
import io.reactivex.plugins.RxJavaPlugins
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


/**
 * http网络请求，包含普通的get和post、上传、下载
 *
 * date: 2019/2/23 16:37
 * author: zengfansheng
 */
object NetworkRequester {
    val executor: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    
    init {
        RxJavaPlugins.setErrorHandler(object : Consumer<Throwable> {
            override fun accept(e: Throwable) {
                var t = e
                if (t is UndeliverableException) {
                    t = t.cause ?: Throwable(e)
                }
                if (t is IOException) {
                    // fine, irrelevant network problem or API that throws on cancellation
                    return
                }
                if (t is InterruptedException) {
                    // fine, some blocking code was interrupted by a dispose call
                    return
                }
                if (t is NullPointerException || t is IllegalArgumentException) {
                    // that's likely a bug in the application
                    Thread.currentThread().uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), t)
                    return
                }
                if (t is IllegalStateException) {
                    // that's a bug in RxJava or in a custom operator
                    Thread.currentThread().uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), t)
                    return
                }
            }
        })
    }
    
    private fun getConfiguration(baseUrl: String, configuration: Configuration?): Configuration {
        val url = HttpUtils.getBaseUrl(baseUrl)
        val config = configuration ?: Configuration()
        if (config.retrofit == null) {
            val client = HttpUtils.initHttpsClient(config.bypassAuth, OkHttpClient().newBuilder())
                .readTimeout(5, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS)
                .build()
            config.retrofit = Retrofit.Builder().baseUrl(url)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build()
        }
        config.service = config.retrofit!!.create(HttpService::class.java)
        return config
    }
    
    /**
     * 单个下载
     *
     * @param info 下载信息
     * @param listener 下载监听
     */
    @JvmStatic
    fun <T : DownloadInfo> download(info: T, listener: DownloadListener<T>?): DownloadWorker<T> {
        return DownloadWorker(info, listener)
    }

    /**
     * 多个同时下载
     *
     * @param infos 下载信息
     * @param listener 下载监听
     */
    @JvmStatic
    fun <T : DownloadInfo> download(infos: List<T>, listener: MultiDownloadListener<T>?): DownloadWorker<T> {
        return DownloadWorker(infos, listener)
    }

    /**
     * 上传。异步的
     */
    @JvmStatic
    @JvmOverloads
    fun <T> upload(info: UploadInfo<T>, listener: UploadListener<T>? = null): UploadWorker<T> {
        return UploadWorker(info, listener)
    }

    /**
     * 上传。同步的
     */
    @JvmStatic
    @JvmOverloads
    fun <T> uploadSync(info: UploadInfo<T>, listener: UploadProgressListener? = null): ConvertedResponse<T> {
        return SyncUploadWorker(info, listener).convertedResponse
    }

    private fun <T> subscribe(observable: Observable<Response<ResponseBody>>, converter: ResponseConverter<T>, configuration: Configuration, callback: RequestCallback<T>?): Disposable {
        return GeneralRequestTask(observable, converter, configuration, callback).disposable!!
    }
    
    private fun <T> handleSyncResponse(call: Call<ResponseBody>, converter: ResponseConverter<T>, configuration: Configuration): ConvertedResponse<T> {
        return SyncGeneralRequestTask(call, converter, configuration).convertedResponse
    }

    /**
     * 普通GET请求。异步的
     */
    @JvmStatic
    fun get(url: String, callback: RequestCallback<ResponseBody>?): Disposable {
        val config = getConfiguration(url, null)
        return subscribe(config.service!!.get(url), OriginalResponseConverter(), config, callback)
    }

    /**
     * 普通GET请求。同步的
     */
    @JvmStatic
    fun get(url: String): ConvertedResponse<ResponseBody> {
        val config = getConfiguration(url, null)
        return handleSyncResponse(config.service!!.getSync(url), OriginalResponseConverter(), config)
    }

    /**
     * 普通GET请求。异步的
     */
    @JvmStatic
    fun get(configuration: Configuration, url: String, callback: RequestCallback<ResponseBody>?): Disposable {
        val config = getConfiguration(url, configuration)
        return subscribe(config.service!!.get(url), OriginalResponseConverter(), config, callback)
    }

    /**
     * 普通GET请求。同步的
     */
    @JvmStatic
    fun get(configuration: Configuration, url: String): ConvertedResponse<ResponseBody> {
        val config = getConfiguration(url, configuration)
        return handleSyncResponse(config.service!!.getSync(url), OriginalResponseConverter(), config)
    }

    /**
     * 普通GET请求。异步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> get(url: String, converter: ResponseConverter<T>, callback: RequestCallback<T>?): Disposable {
        val config = getConfiguration(url, null)
        return subscribe(config.service!!.get(url), converter, config, callback)
    }

    /**
     * 普通GET请求。同步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> get(url: String, converter: ResponseConverter<T>): ConvertedResponse<T> {
        val config = getConfiguration(url, null)
        return handleSyncResponse(config.service!!.getSync(url), converter, config)
    }

    /**
     * 普通GET请求。异步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> get(configuration: Configuration, url: String, converter: ResponseConverter<T>, callback: RequestCallback<T>?): Disposable {
        val config = getConfiguration(url, configuration)
        return subscribe(config.service!!.get(url), converter, config, callback)
    }

    /**
     * 普通GET请求。同步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> get(configuration: Configuration, url: String, converter: ResponseConverter<T>): ConvertedResponse<T> {
        val config = getConfiguration(url, configuration)
        return handleSyncResponse(config.service!!.getSync(url), converter, config)
    }

    /**
     * POST请求，body是json。异步的
     *
     * @param url 请求的url
     */
    @JvmStatic
    fun postJson(url: String, json: String, callback: RequestCallback<ResponseBody>?): Disposable {
        val requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        val config = getConfiguration(url, null)
        return subscribe(config.service!!.postJson(url, requestBody), OriginalResponseConverter(), config, callback)
    }

    /**
     * POST请求，body是json。同步的
     *
     * @param url 请求的url
     */
    @JvmStatic
    fun postJson(url: String, json: String): ConvertedResponse<ResponseBody> {
        val requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        val config = getConfiguration(url, null)
        return handleSyncResponse(config.service!!.postJsonSync(url, requestBody), OriginalResponseConverter(), config)
    }

    /**
     * POST请求，body是json。异步的
     *
     * @param url 请求的url
     */
    @JvmStatic 
    fun postJson(configuration: Configuration, url: String, json: String, callback: RequestCallback<ResponseBody>?): Disposable {
        val requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        val config = getConfiguration(url, configuration)
        return subscribe(config.service!!.postJson(url, requestBody), OriginalResponseConverter(), config, callback)
    }

    /**
     * POST请求，body是json。同步的
     *
     * @param url 请求的url
     */
    @JvmStatic
    fun postJson(configuration: Configuration, url: String, json: String): ConvertedResponse<ResponseBody> {
        val requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        val config = getConfiguration(url, configuration)
        return handleSyncResponse(config.service!!.postJsonSync(url, requestBody), OriginalResponseConverter(), config)
    }

    /**
     * POST请求，body是json。异步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postJson(url: String, json: String, converter: ResponseConverter<T>, callback: RequestCallback<T>?): Disposable {
        val requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        val config = getConfiguration(url, null)
        return subscribe(config.service!!.postJson(url, requestBody), converter, config, callback)
    }

    /**
     * POST请求，body是json。同步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postJson(url: String, json: String, converter: ResponseConverter<T>): ConvertedResponse<T> {
        val requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        val config = getConfiguration(url, null)
        return handleSyncResponse(config.service!!.postJsonSync(url, requestBody), converter, config)
    }

    /**
     * POST请求，body是json。异步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postJson(configuration: Configuration, url: String, json: String, converter: ResponseConverter<T>, callback: RequestCallback<T>?): Disposable {
        val requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        val config = getConfiguration(url, configuration)
        return subscribe(config.service!!.postJson(url, requestBody), converter, config, callback)
    }

    /**
     * POST请求，body是json。同步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postJson(configuration: Configuration, url: String, json: String, converter: ResponseConverter<T>): ConvertedResponse<T> {
        val requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        val config = getConfiguration(url, configuration)
        return handleSyncResponse(config.service!!.postJsonSync(url, requestBody), converter, config)
    }

    /**
     * POST请求，body是字符串。异步的
     */
    @JvmStatic
    fun postText(url: String, text: String, callback: RequestCallback<ResponseBody>?): Disposable {
        val requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text)
        val config = getConfiguration(url, null)
        return subscribe(config.service!!.post(url, requestBody), OriginalResponseConverter(), config, callback)
    }

    /**
     * POST请求，body是字符串。同步的
     */
    @JvmStatic
    fun postText(url: String, text: String): ConvertedResponse<ResponseBody> {
        val requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text)
        val config = getConfiguration(url, null)
        return handleSyncResponse(config.service!!.postSync(url, requestBody), OriginalResponseConverter(), config)
    }

    /**
     * POST请求，body是字符串。异步的
     */
    @JvmStatic
    fun postText(configuration: Configuration, url: String, text: String, callback: RequestCallback<ResponseBody>?): Disposable {
        val requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text)
        val config = getConfiguration(url, configuration)
        return subscribe(config.service!!.post(url, requestBody), OriginalResponseConverter(), config, callback)
    }

    /**
     * POST请求，body是字符串。同步的
     */
    @JvmStatic
    fun postText(configuration: Configuration, url: String, text: String): ConvertedResponse<ResponseBody> {
        val requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text)
        val config = getConfiguration(url, configuration)
        return handleSyncResponse(config.service!!.postSync(url, requestBody), OriginalResponseConverter(), config)
    }

    /**
     * POST请求，body是字符串。异步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postText(url: String, text: String, converter: ResponseConverter<T>, callback: RequestCallback<T>?): Disposable {
        val requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text)
        val config = getConfiguration(url, null)
        return subscribe(config.service!!.post(url, requestBody), converter, config, callback)
    }

    /**
     * POST请求，body是字符串。同步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postText(url: String, text: String, converter: ResponseConverter<T>): ConvertedResponse<T> {
        val requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text)
        val config = getConfiguration(url, null)
        return handleSyncResponse(config.service!!.postSync(url, requestBody), converter, config)
    }

    /**
     * POST请求，body是字符串。异步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postText(configuration: Configuration, url: String, text: String, converter: ResponseConverter<T>, callback: RequestCallback<T>?): Disposable {
        val requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text)
        val config = getConfiguration(url, configuration)
        return subscribe(config.service!!.post(url, requestBody), converter, config, callback)
    }

    /**
     * POST请求，body是字符串。同步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postText(configuration: Configuration, url: String, text: String, converter: ResponseConverter<T>): ConvertedResponse<T> {
        val requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text)
        val config = getConfiguration(url, configuration)
        return handleSyncResponse(config.service!!.postSync(url, requestBody), converter, config)
    }

    /**
     * POST提交表单。异步的
     *
     * @param map 参数集合
     */
    @JvmStatic
    fun postForm(url: String, map: Map<String, Any>, callback: RequestCallback<ResponseBody>?): Disposable {
        val config = getConfiguration(url, null)
        return subscribe(config.service!!.postForm(url, map), OriginalResponseConverter(), config, callback)
    }

    /**
     * POST提交表单。同步的
     *
     * @param map 参数集合
     */
    @JvmStatic
    fun postForm(url: String, map: Map<String, Any>): ConvertedResponse<ResponseBody> {
        val config = getConfiguration(url, null)
        return handleSyncResponse(config.service!!.postFormSync(url, map), OriginalResponseConverter(), config)
    }

    /**
     * POST提交表单。异步的
     *
     * @param map 参数集合
     */
    @JvmStatic
    fun postForm(configuration: Configuration, url: String, map: Map<String, Any>, callback: RequestCallback<ResponseBody>?): Disposable {
        val config = getConfiguration(url, configuration)
        return subscribe(config.service!!.postForm(url, map), OriginalResponseConverter(), config, callback)
    }

    /**
     * POST提交表单。同步的
     *
     * @param map 参数集合
     */
    @JvmStatic
    fun postForm(configuration: Configuration, url: String, map: Map<String, Any>): ConvertedResponse<ResponseBody> {
        val config = getConfiguration(url, configuration)
        return handleSyncResponse(config.service!!.postFormSync(url, map), OriginalResponseConverter(), config)
    }

    /**
     * POST提交表单。异步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postForm(url: String, map: Map<String, Any>, converter: ResponseConverter<T>, callback: RequestCallback<T>?): Disposable {
        val config = getConfiguration(url, null)
        return subscribe(config.service!!.postForm(url, map), converter, config, callback)
    }

    /**
     * POST提交表单。同步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postForm(url: String, map: Map<String, Any>, converter: ResponseConverter<T>): ConvertedResponse<T> {
        val config = getConfiguration(url, null)
        return handleSyncResponse(config.service!!.postFormSync(url, map), converter, config)
    }

    /**
     * POST提交表单。异步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postForm(configuration: Configuration, url: String, map: Map<String, Any>, converter: ResponseConverter<T>, callback: RequestCallback<T>?): Disposable {
        val config = getConfiguration(url, configuration)
        return subscribe(config.service!!.postForm(url, map), converter, config, callback)
    }

    /**
     * POST提交表单。同步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postForm(configuration: Configuration, url: String, map: Map<String, Any>, converter: ResponseConverter<T>): ConvertedResponse<T> {
        val config = getConfiguration(url, configuration)
        return handleSyncResponse(config.service!!.postFormSync(url, map), converter, config)
    }
}