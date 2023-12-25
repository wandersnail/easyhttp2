package com.zfs.httpdemo

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by 曾繁盛 on 2023/12/23 17:25
 */
class MyHttpInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return chain.proceed(request)
    }
}