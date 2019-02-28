package com.snail.network.callback

/**
 *
 *
 * date: 2019/2/23 23:21
 * author: zengfansheng
 */
interface TaskListener<T> {
    /**
     * 任务上传状态改变
     */
    fun onStateChange(info: T, t: Throwable?)

    /**
     * 上传进度变化
     * 
     * @param info 当前上传信息
     */
    fun onProgress(info: T)
}