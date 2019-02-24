package com.snail.network.download

import com.snail.network.exception.RetryWhenException
import com.snail.network.utils.HttpUtils
import com.snail.network.utils.IOUtils
import com.snail.network.utils.SchedulerUtils
import io.reactivex.functions.Function
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 下载任务
 *
 * date: 2019/2/23 18:13
 * author: zengfansheng
 */
class DownloadTask<T : DownloadInfo> {
    private val taskMap = ConcurrentHashMap<T, ProgressObserver<T>>()
    private val listener: DownloadListener<T>?
    private val totalTasks: Int
    private var successCount = 0
    private var failedCount = 0
    
    internal constructor(info: T, listener: DownloadListener<T>?) {
        this.listener = listener  
        totalTasks = 1
        info.reset()
        download(info)
    }
    
    internal constructor(infos: List<T>, listener: MultiDownloadListener<T>?) {
        this.listener = listener
        totalTasks = infos.size
        infos.forEach {
            it.reset()
            download(it)
        }
    }
    
    private fun download(info: T) {
        val progressObserver = ProgressObserver(info, if (listener == null) null else MyDownloadListener())
        taskMap[info] = progressObserver
        val interceptor = ProgressInterceptor(progressObserver)
        val client = HttpUtils.initHttpsClient(true, OkHttpClient.Builder())
                .connectTimeout(info.connectTimeout.toLong(), TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build()
        Retrofit.Builder()
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(info.baseUrl)
                .build()
                .create(DownloadService::class.java)
                //断点续传
                .download("bytes=" + info.readLength + "-", info.url)              
                //失败后的retry配置
                .retryWhen(RetryWhenException())
                //写入文件
                .map(object : Function<ResponseBody, T> {
                    override fun apply(responseBody: ResponseBody): T {
                        writeToDisk(responseBody, File(info.temporaryFilePath), info)
                        return info
                    }
                })
                .compose(SchedulerUtils.applyObservableSchedulers())
                .subscribe(progressObserver)
    }
        
    private inner class MyDownloadListener : DownloadListener<T> {
        override fun onStateChange(info: T, t: Throwable?) {
            listener?.onStateChange(info, t)
            if (totalTasks > 1) {
                if (info.state == DownloadInfo.State.COMPLETED) {
                    taskMap.remove(info)
                    successCount++
                    (listener as? MultiDownloadListener<T>)?.onTotalProgress(successCount, failedCount, totalTasks)
                } else if (info.state == DownloadInfo.State.CANCEL || info.state == DownloadInfo.State.ERROR) {
                    taskMap.remove(info)
                    failedCount++
                    (listener as? MultiDownloadListener<T>)?.onTotalProgress(successCount, failedCount, totalTasks)
                } else if (info.state == DownloadInfo.State.START) {
                    (listener as? MultiDownloadListener<T>)?.onTotalProgress(successCount, failedCount, totalTasks)
                }
            }
        }

        override fun onProgress(info: T) {
            listener?.onProgress(info)
        }
    }

    /**
     * 进行中的下载任务数
     */
    fun remaining(): Int {
        return taskMap.size
    }
    
    /**
     * 取消所有下载
     */
    fun cancel() {
        taskMap.values.forEach { it.dispose(true) }
        taskMap.clear()
    }

    /**
     * 取消单个下载
     */
    fun cancel(info: T) {
        taskMap.remove(info)?.dispose(true)
    }

    /**
     * 暂停所有下载
     */
    fun pause() {
        val iterator = taskMap.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.key.state == DownloadInfo.State.DOWNLOADING || entry.key.state == DownloadInfo.State.START) {
                entry.value.dispose(false)
            }
        }
    }

    /**
     * 暂停单个下载
     */
    fun pause(info: T) {
        if (info.state == DownloadInfo.State.DOWNLOADING || info.state == DownloadInfo.State.START) {
            taskMap[info]?.dispose(false)
        }
    }

    /**
     * 恢复所有下载
     */
    fun resume() {
        taskMap.keys.forEach { 
            if (it.state == DownloadInfo.State.PAUSE) {
                download(it)
            }
        }
    }

    /**
     * 恢复单个下载
     */
    fun resume(info: T) {
        if (info.state == DownloadInfo.State.PAUSE) {
            download(info)
        }
    }

    //写入文件在本地
    private fun writeToDisk(responseBody: ResponseBody?, file: File, info: T) {
        if (responseBody == null) {
            return
        }
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        val inputStream = responseBody.byteStream()
        var randomAccessFile: RandomAccessFile? = null
        var channelOut: FileChannel? = null
        try {
            val allLength: Long = if (info.contentLength == 0L) {
                responseBody.contentLength()
            } else {
                info.contentLength
            }
            randomAccessFile = RandomAccessFile(file, "rwd")
            channelOut = randomAccessFile.channel
            val mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE, info.readLength, allLength - info.readLength)
            val buffer = ByteArray(1024 * 8)
            var len = inputStream.read(buffer)
            while (len != -1) {
                mappedBuffer.put(buffer, 0, len)
                len = inputStream.read(buffer)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            IOUtils.closeQuietly(responseBody.byteStream(), channelOut, randomAccessFile)
        }
    }
}