package com.snail.network

import com.snail.network.download.DownloadInfo
import com.snail.network.download.DownloadListener
import com.snail.network.download.DownloadTask
import com.snail.network.download.MultiDownloadListener


/**
 *
 *
 * date: 2019/2/23 16:37
 * author: zengfansheng
 */
class NetworkManager private constructor() {

    /**
     * 单个下载
     * 
     * @param info 下载信息
     * @param listener 下载监听
     */
    fun <T : DownloadInfo> download(info: T, listener: DownloadListener<T>?): DownloadTask<T> {
        return DownloadTask(info, listener)
    }

    /**
     * 多个同时下载
     * 
     * @param infos 下载信息
     * @param listener 下载监听
     */
    fun <T : DownloadInfo> download(infos: List<T>, listener: MultiDownloadListener<T>?): DownloadTask<T> {
        return DownloadTask(infos, listener)
    }

    internal object Holder {
        internal val requester = NetworkManager()
    }

    companion object {
        val instance: NetworkManager
            get() = Holder.requester
    }
}