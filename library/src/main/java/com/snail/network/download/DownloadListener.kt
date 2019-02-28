package com.snail.network.download

import com.snail.network.callback.TaskStateListener

/**
 *  下载监听
 *
 * date: 2019/2/23 23:21
 * author: zengfansheng
 */
interface DownloadListener<T : DownloadInfo> : TaskStateListener<T>