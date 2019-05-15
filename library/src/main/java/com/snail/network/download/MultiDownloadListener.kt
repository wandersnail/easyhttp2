package com.snail.network.download

import com.snail.network.callback.MultiProgressListener

/**
 * 多下载任务监听
 *
 * date: 2019/2/28 20:26
 * author: zengfansheng
 */
interface MultiDownloadListener<T : DownloadInfo> : DownloadListener<T>, MultiProgressListener