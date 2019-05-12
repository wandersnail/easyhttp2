package com.snail.network.converter

import com.snail.network.exception.ConvertException
import okhttp3.ResponseBody

/**
 * 响应体的字符串
 *
 * date: 2019/2/27 23:49
 * author: zengfansheng
 */
class StringResponseConverter : ResponseConverter<String> {
    override fun convert(value: ResponseBody?): String {
        try {
            return value!!.string()
        } catch (e: Throwable) {
            throw ConvertException(e.message, e)
        }
    }
}