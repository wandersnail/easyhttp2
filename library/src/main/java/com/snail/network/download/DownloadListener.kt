package com.snail.network.download

/**
 *
 *
 * date: 2019/2/23 23:21
 * author: zengfansheng
 */
interface DownloadListener<T : DownloadInfo> {
    /**
     * 下载状态改变
     */
    fun onStateChange(info: T, t: Throwable?)

    /**
     * 下载进度变化
     * 
     * @param info 当前下载信息
     */
    fun onProgress(info: T)
}