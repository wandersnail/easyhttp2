package com.snail.network.upload

import com.snail.network.TaskInfo
import com.snail.network.converter.Converter
import okhttp3.MediaType
import okhttp3.RequestBody
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
        val args: Map<String, RequestBody>? = null,
        internal val cls: Class<R>? = null,
        internal val converter: Converter<ResponseBody, R>? = null,
        tag: String = UUID.randomUUID().toString()) : TaskInfo(url, tag) {

    /**
     * 服务端响应数据
     */
    var response: R? = null
    
    /**
     * @param cls Json数据模型字节码
     * @param R 转到成的Json数据模型类
     */
    @JvmOverloads
    constructor(url: String, file: File, cls: Class<R>, mediaType: MediaType? = null, args: Map<String, RequestBody>? = null, 
                tag: String = UUID.randomUUID().toString()) : this(url, file, mediaType, args, cls, null, tag)

    /**
     * @param converter 响应体转换器
     * @param R 转到成的类
     */
    @JvmOverloads
    constructor(url: String, file: File, converter: Converter<ResponseBody, R>, mediaType: MediaType? = null, args: Map<String, RequestBody>? = null, 
                tag: String = UUID.randomUUID().toString()) : this(url, file, mediaType, args, null, converter, tag)
}