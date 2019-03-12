package com.snail.network.converter

import com.google.gson.Gson
import com.snail.network.exception.ConvertException
import okhttp3.ResponseBody

/**
 *
 *
 * date: 2019/3/1 09:10
 * author: zengfansheng
 */
class JsonResponseConverter<T>(private val cls: Class<T>) : ResponseConverter<T> {
    override fun convert(value: ResponseBody): T {
        try {
            return Gson().fromJson(value.string(), cls)
        } catch (e: Throwable) {
            throw ConvertException(e.message, e)
        }
    }
}