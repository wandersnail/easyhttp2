package com.snail.network

import com.snail.network.converter.ResponseConverter
import okhttp3.ResponseBody
import retrofit2.Call

/**
 * 同步的一般的请求任务
 *
 * date: 2019/5/16 20:49
 * author: zengfansheng
 */
internal class SyncGeneralRequestTask<T>(private val call: Call<ResponseBody>, converter: ResponseConverter<T>, configuration: Configuration) {
    val convertedResponse = ConvertedResponse<T>(call)
    
    init {
        var completed = false
        var secondCount = 0
        if (configuration.callTimeout > 0) {
            //计时
            NetworkRequester.executor.execute {
                while (secondCount++ < configuration.callTimeout) {
                    Thread.sleep(1000)
                    if (completed) {
                        return@execute
                    }
                }
                //如果到了这里，说明超时了，取消请求
                call.cancel()
            }
        }
        try {
            val response = call.execute()
            completed = true
            convertedResponse.raw = response.raw()
            if (response.isSuccessful) {
                try {
                    convertedResponse.convertedBody = converter.convert(response.body())
                } catch (t: Throwable) {
                    convertedResponse.convertRrror = t
                }
            }
        } catch (e: Exception) {
            //取消任务会抛异常            
        }        
    }
}