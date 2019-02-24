package com.snail.network.download

/**
 *
 *
 * date: 2019/2/23 23:36
 * author: zengfansheng
 */
interface MultiDownloadListener<T : DownloadInfo> : DownloadListener<T> {
    /**
     * 总体下载任务进度
     * 
     * @param successCount 下载成功个数
     * @param failedCount 下载失败个数，包含主动取消的
     * @param total 总任务数
     */
    fun onTotalProgress(successCount: Int, failedCount: Int, total: Int)
}