package com.zfs.httpdemo

import com.snail.network.general.ResponseBodyConverter
import okhttp3.ResponseBody

/**
 *
 *
 * date: 2019/2/26 16:52
 * author: zengfansheng
 */
class StringRespConverter : ResponseBodyConverter<String> {
    override fun convert(value: ResponseBody): String {
        return value.string()
    }
}