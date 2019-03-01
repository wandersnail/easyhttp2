package com.snail.network.converter

import okhttp3.ResponseBody

/**
 *
 *
 * date: 2019/3/1 09:07
 * author: zengfansheng
 */
interface ResponseConverter<T> : Converter<ResponseBody, T>