package com.snail.network.upload

import com.snail.network.TaskInfo
import com.snail.network.converter.ResponseConverter
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.Response
import java.io.File
import java.util.*

/**
 *
 *
 * date: 2019/2/28 13:00
 * author: zengfansheng
 */
open class UploadInfo<R> @JvmOverloads constructor(
        url: String,
        /** 待上传文件 */
        val file: File,
        /** 响应体转换器 */
        internal val converter: ResponseConverter<R>,
        /** 文件类型 */
        val mediaType: MediaType? = null,
        /** 请求参数 */
        val args: Map<String, @JvmSuppressWildcards RequestBody>? = null,
        tag: String = UUID.randomUUID().toString()) : TaskInfo(url, tag) {

    /**
     * 服务端响应数据
     */
    var response: Response? = null

    /**
     * 转换过的body
     */
    var convertedBody: R? = null
}