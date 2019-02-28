package com.snail.network.upload

import com.snail.network.TaskInfo
import com.snail.network.converter.Converter
import okhttp3.MediaType
import okhttp3.ResponseBody
import java.io.File
import java.util.*

/**
 *
 *
 * date: 2019/2/28 13:00
 * author: zengfansheng
 */
open class UploadInfo<R> private constructor(
        url: String,
        val file: File,
        val mediaType: MediaType? = null,
        val cls: Class<R>? = null,
        val converter: Converter<ResponseBody, R>? = null,
        tag: String = UUID.randomUUID().toString()) : TaskInfo(url, tag) {

    @JvmOverloads
    constructor(url: String, file: File, cls: Class<R>, mediaType: MediaType? = null, tag: String = UUID.randomUUID().toString()) : this(url, file, mediaType, cls, null, tag)
    
    @JvmOverloads
    constructor(url: String, file: File, converter: Converter<ResponseBody, R>, mediaType: MediaType? = null, tag: String = UUID.randomUUID().toString()) : this(url, file, mediaType, null, converter, tag)
}