package com.snail.network.download

import com.snail.network.callback.ProgressListener
import okhttp3.Interceptor
import okhttp3.Response

import java.io.IOException

/**
 * 时间: 2017/7/6 14:47
 * 作者: zengfansheng
 * 邮箱: 43068145@qq.com
 * 功能: 拦截器，加入进度监听
 * 
 * @param listener 进度
 */

internal class ProgressInterceptor(private val listener: ProgressListener) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        val body = originalResponse.body()
        return originalResponse.newBuilder().body(if (body == null) null else ProgressResponseBody(body, listener)).build()
    }
}
