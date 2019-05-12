package com.snail.network.converter

import com.snail.network.exception.ConvertException
import okhttp3.ResponseBody

/**
 * 不进行转换的，直接返回原始数据
 *
 * date: 2019/2/28 22:19
 * author: zengfansheng
 */
class OriginalResponseConverter : ResponseConverter<ResponseBody> {
    override fun convert(value: ResponseBody?): ResponseBody {
        try {
            return value!!
        } catch (e: Throwable) {
            throw ConvertException(e.message, e)
        }
    }
}