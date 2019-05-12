package com.snail.network.callback

import okhttp3.Response


/**
 * 请求结果回调
 *
 * date: 2019/5/1 10:12
 * author: zengfansheng
 */
interface RequestCallback<T> {
    fun onSuccess(response: Response, convertedBody: T?)
    
    fun onError(t: Throwable)
}