package com.snail.network.general

import com.snail.network.exception.ConvertException
import okhttp3.ResponseBody

/**
 *
 *
 * date: 2019/2/26 12:26
 * author: zengfansheng
 */
interface ResponseBodyConverter<T> {
    @Throws(ConvertException::class)
    fun convert(value: ResponseBody): T
}