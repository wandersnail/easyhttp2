package com.snail.network.download

import com.snail.network.callback.MultiProgressListener

/**
 * 多个任务下载监听
 *
 * date: 2019/2/23 23:36
 * author: zengfansheng
 */
interface MultiDownloadListener<T : DownloadInfo> : DownloadListener<T>, MultiProgressListener