package com.snail.network.upload

import com.snail.network.TaskInfo
import com.snail.network.callback.TaskObserver

/**
 *
 *
 * date: 2019/2/28 12:58
 * author: zengfansheng
 * @property R 响应数据类型 
 */
internal class UploadObserver<R, T : UploadInfo<R>> @JvmOverloads constructor(info: T, listener: UploadListener<R, T>? = null) : TaskObserver<T>(info, listener) {
    override fun onCancel() {
    }

    override fun onComplete() {
        info.state = TaskInfo.State.COMPLETED
        listener?.onStateChange(info, null)
    }
}