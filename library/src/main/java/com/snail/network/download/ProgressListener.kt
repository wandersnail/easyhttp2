package com.snail.network.download

/**
 * 时间: 2017/7/8 20:23
 * 作者: zengfansheng
 * 邮箱: 43068145@qq.com
 * 功能: 下载进度监听
 */

internal interface ProgressListener {
    /**
     * 下载进度
     * @param readLength    已下载长度
     * @param contentLength 总长度
     */
    fun onProgress(readLength: Long, contentLength: Long)
}
