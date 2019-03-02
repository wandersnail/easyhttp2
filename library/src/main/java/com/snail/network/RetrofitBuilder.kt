package com.snail.network

import com.snail.network.utils.HttpUtils
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

/**
 *
 *
 * date: 2019/2/25 09:32
 * author: zengfansheng
 */
class RetrofitBuilder {
    private var callFactory: okhttp3.Call.Factory? = null
    private var callbackExecutor: Executor? = null
    private var client: OkHttpClient? = null
    private val converterFactories = ArrayList<Converter.Factory>()
    private val callAdapterFactories = ArrayList<CallAdapter.Factory>()
    private var validateEagerly: Boolean = false
    private var bypassAuth: Boolean = false
    private var baseUrl: String? = null

    /**
     * The HTTP client used for requests.
     * 
     * This is a convenience method for calling [callFactory].
     */
    fun client(client: OkHttpClient): RetrofitBuilder {
        this.client = client
        return this
    }

    /**
     * Specify a custom call factory for creating [retrofit2.Call] instances.
     * 
     * Note: Calling [client] automatically sets this value.
     */
    fun callFactory(callFactory: okhttp3.Call.Factory): RetrofitBuilder {
        this.callFactory = callFactory
        return this
    }

    /**
     * The executor on which [retrofit2.Callback] methods are invoked when returning [retrofit2.Call] from
     * your service method.
     * 
     * Note: executor is not used for [addCallAdapterFactory] custom method return types.
     */
    fun setCallbackExecutor(callbackExecutor: Executor): RetrofitBuilder {
        this.callbackExecutor = callbackExecutor
        return this
    }

    /**
     * Add converter factory for serialization and deserialization of objects.
     */
    fun addConverterFactory(factory: Converter.Factory): RetrofitBuilder {
        converterFactories.add(factory)
        return this
    }

    /**
     * Add a call adapter factory for supporting service method return types other than [retrofit2.Call].
     */
    fun addCallAdapterFactory(factory: CallAdapter.Factory): RetrofitBuilder {
        callAdapterFactories.add(factory)
        return this
    }

    /**
     * When calling [Retrofit.create] on the resulting [Retrofit] instance, eagerly validate
     * the configuration of all methods in the supplied interface.
     */
    fun validateEagerly(validateEagerly: Boolean): RetrofitBuilder {
        this.validateEagerly = validateEagerly
        return this
    }

    /**
     * Set the API base URL.
     */
    fun baseUrl(baseUrl: String): RetrofitBuilder {
        this.baseUrl = HttpUtils.getBaseUrl(baseUrl)
        return this
    }

    /**
     * 是否绕过认证，也就是无条件信任所有HTTPS网站
     */
    fun bypassAuth(bypassAuth: Boolean): RetrofitBuilder {
        this.bypassAuth = bypassAuth
        return this
    }

    /**
     * Create the [Retrofit] instance using the configured values.
     * 
     * Note: If neither [client] nor [callFactory] is called a default [OkHttpClient] will be created and used.
     */
    fun build(): Retrofit {
        val builder = Retrofit.Builder()
        if (baseUrl != null) {
            builder.baseUrl(HttpUtils.getBaseUrl(baseUrl!!))
        }
        builder.client(if (client == null) getDefaultClientBuilder(bypassAuth).build() else client!!)
        builder.validateEagerly(validateEagerly)
        if (callFactory != null) {
            builder.callFactory(callFactory!!)
        }
        if (callbackExecutor != null) {
            builder.callbackExecutor(callbackExecutor!!)
        }
        callAdapterFactories.forEach {
            builder.addCallAdapterFactory(it)
        }
        converterFactories.forEach {
            builder.addConverterFactory(it)
        }
        return builder.build()
    }
    
    companion object {
        @JvmOverloads 
        fun getDefaultRetrofitBuilder(baseUrl: String, bypassAuth: Boolean = false): RetrofitBuilder {
            return RetrofitBuilder().baseUrl(baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(getDefaultClientBuilder(bypassAuth).build())
                .addConverterFactory(GsonConverterFactory.create())
        }

        fun getDefaultClientBuilder(bypassAuth: Boolean): OkHttpClient.Builder {
            return HttpUtils.initHttpsClient(bypassAuth, OkHttpClient().newBuilder())
                .readTimeout(5, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS)
        }
    }
}