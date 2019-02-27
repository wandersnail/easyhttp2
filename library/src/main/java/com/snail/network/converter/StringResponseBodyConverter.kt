package com.snail.network.converter

import okhttp3.ResponseBody

/**
 *
 *
 * date: 2019/2/27 23:49
 * author: zengfansheng
 */
class StringResponseBodyConverter : Converter<ResponseBody, String> {
    override fun convert(value: ResponseBody): String {
        return value.string()
    }
}