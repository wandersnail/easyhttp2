package com.snail.network.upload

import java.util.concurrent.ConcurrentHashMap

/**
 *
 *
 * date: 2019/2/28 18:21
 * author: zengfansheng
 */
class UploadWorker<R, T : UploadInfo<R>> {
    private val taskMap = ConcurrentHashMap<T, UploadObserver<R, T>>()
    private val listener: UploadListener<R, T>?
    private val totalTasks: Int
    private var successCount = 0
    private var failedCount = 0

    internal constructor(info: T, listener: UploadListener<R, T>?) {
        this.listener = listener
        totalTasks = 1
        upload(info)
    }

    internal constructor(infos: List<T>, listener: MultiUploadListener<R, T>?) {
        this.listener = listener
        totalTasks = infos.size
        infos.forEach {
            upload(it)
        }
    }
    
    private fun upload(info: T) {
        
    }
}