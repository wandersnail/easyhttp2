package com.snail.network.upload

/**
 *
 *
 * date: 2019/5/15 11:16
 * author: zengfansheng
 */
interface UploadProgressListener {
    /**
     * 进度更新
     *
     * @param name 文件参数名
     * @param progress 已完成的大小
     * @param max 总大小
     */
    fun onProgress(name: String, progress: Long, max: Long)
}