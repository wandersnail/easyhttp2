package com.snail.network.upload

import com.snail.network.TaskInfo
import okhttp3.Response

/**
 * 上传监听
 *
 * date: 2019/5/15 11:01
 * author: zengfansheng
 */
interface UploadListener<T> : UploadProgressListener {
    /**
     * 任务上传状态改变
     */
    fun onStateChange(state: TaskInfo.State, t: Throwable?)

    /**
     * 响应结果解析完成
     *
     * @param response 服务端响应数据
     * @param convertedBody 根据转换器转换过的body
     */
    fun onResponseBodyParse(response: Response, convertedBody: T?)
}