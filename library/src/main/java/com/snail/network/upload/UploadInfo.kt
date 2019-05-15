package com.snail.network.upload

import com.snail.network.TaskInfo
import com.snail.network.converter.ResponseConverter
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.File
import java.util.*

/**
 * 上传任务信息
 *
 * date: 2019/2/28 13:00
 * author: zengfansheng
 *
 * @param converter 响应体转换器
 * @param paramParts 携带的参数
 * @param fileParts 多文件，key：为form-data; name=key
 * @param client 使用自定义的OkHttpClient
 */
open class UploadInfo<R> 
@JvmOverloads constructor(url: String, internal val converter: ResponseConverter<R>, val paramParts: Map<String, String>, val fileParts: Map<String, File>,
                          internal val client: OkHttpClient? = null, tag: String = UUID.randomUUID().toString()) : TaskInfo(url, tag) {

    /**
     * 服务端响应数据
     */
    var response: Response? = null

    /**
     * 转换过的body
     */
    var convertedBody: R? = null
}