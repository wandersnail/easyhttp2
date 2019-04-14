package com.snail.network.converter

import com.alibaba.fastjson.JSON
import okhttp3.ResponseBody
import okio.Okio
import retrofit2.Converter
import java.lang.reflect.Type



/**
 * 使用阿里巴巴的FastJson转换响应体
 *
 * date: 2019/4/14 17:39
 * author: zengfansheng
 */
class FastJsonResponseBodyConverter(private val type: Type) : Converter<ResponseBody, Any> {
    override fun convert(value: ResponseBody): Any? {
        val bufferedSource = Okio.buffer(value.source())
        val tempStr = bufferedSource.readUtf8()
        bufferedSource.close()
        return JSON.parseObject(tempStr, type)
    }
}