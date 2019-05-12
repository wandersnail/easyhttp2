package com.snail.network.upload

import com.snail.network.callback.TaskListener
import com.snail.network.callback.TaskObserver
import okhttp3.ResponseBody
import retrofit2.Response

/**
 * 上传任务观察者
 *
 * date: 2019/2/28 12:58
 * author: zengfansheng
 * @property R 响应数据类型 
 */
internal class UploadObserver<R, T : UploadInfo<R>> @JvmOverloads constructor(info: T, listener: TaskListener<T>? = null) : TaskObserver<T>(info, listener) {
    override fun onCancel() {
    }

    override fun onNext(t: Response<ResponseBody>) {
        info.response = t.raw()
        try {
            info.convertedBody = info.converter.convert(t.body())
        } catch (e: Exception) {
            onError(e)
        }
    }
    
    override fun onComplete() {
        handleSuccess()
    }
}