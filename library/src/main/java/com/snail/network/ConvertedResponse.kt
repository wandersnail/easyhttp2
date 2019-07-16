package com.snail.network

import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Call

/**
 * 转换过的响应数据
 *
 * date: 2019/5/16 20:08
 * author: zengfansheng
 */
class ConvertedResponse<T> internal constructor(private val call: Call<ResponseBody>) {
    var convertedBody: T? = null
    /**
     * 转换异常
     */
    var convertRrror: Throwable? = null
    /**
     * 原始响应
     */
    var raw: Response? = null
    /**
     * 是否请求超时了
     */
    var callTimeout = false
    /**
     * 是否被取消了
     */
    val isCanceled
        get() = call.isCanceled

    /**
     * 取消
     */
    fun cancel() {
        if (call.isExecuted && !call.isCanceled) {
            call.cancel()
        }
    }
}