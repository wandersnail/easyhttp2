package com.snail.network.download

import com.snail.network.TaskInfo
import com.snail.network.exception.RetryWhenException
import com.snail.network.interceptor.ProgressInterceptor
import com.snail.network.utils.HttpUtils
import com.snail.network.utils.IOUtils
import com.snail.network.utils.SchedulerUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.util.concurrent.ConcurrentHashMap

/**
 * 下载任务
 *
 * date: 2019/2/23 18:13
 * author: zengfansheng
 */
class DownloadWorker<T : DownloadInfo> : Disposable {
    private val taskMap = ConcurrentHashMap<T, DownloadObserver<T>>()
    private val listener: DownloadListener<T>?
    private val totalTasks: Int
    private var successCount = 0
    private var failedCount = 0
    private var isCanceled = false
    
    internal constructor(info: T, listener: DownloadListener<T>?) {
        this.listener = listener
        totalTasks = 1
        info.reset()
        this.execute(info)
    }
    
    internal constructor(infos: List<T>, listener: MultiDownloadListener<T>?) {
        this.listener = listener
        totalTasks = infos.size
        infos.forEach {
            it.reset()
            this.execute(it)
        }
    }

    private fun execute(info: T) {
        //如果listener为空，说明不需要监听，不为空则在本地监听后，再传出去
        val observer = DownloadObserver(info, if (listener == null) null else LocalTaskListener())
        taskMap[info] = observer
        val interceptor = ProgressInterceptor(observer)
        val client = HttpUtils.initHttpsClient(true, OkHttpClient.Builder())
            .addInterceptor(interceptor)
            .build()
        Retrofit.Builder()
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(info.baseUrl)
            .build()
            .create(DownloadService::class.java)
            //断点续传
            .download("bytes=" + info.completionLength + "-", info.url)
            //失败后的retry配置
            .retryWhen(RetryWhenException())
            //写入文件
            .map {
                writeToDisk(it.body(), File(info.temporaryFilePath), info)
                it
            }
            .compose(SchedulerUtils.applyGeneralObservableSchedulers())
            .subscribe(observer)
    }
    
    /**
     * 暂停所有下载
     */
    fun pause() {
        taskMap.values.forEach { 
            it.dispose(false)
        }
    }

    /**
     * 暂停单个下载
     */
    fun pause(info: T) {
        taskMap[info]?.dispose(false)
    }

    /**
     * 恢复所有下载
     */
    fun resume() {
        AndroidSchedulers.mainThread().scheduleDirect {
            taskMap.keys.forEach {
                if (it.state == TaskInfo.State.PAUSE) {
                    execute(it)
                }
            }
        }
    }

    /**
     * 恢复单个下载
     */
    fun resume(info: T) {
        AndroidSchedulers.mainThread().scheduleDirect {
            if (info.state == TaskInfo.State.PAUSE) {
                execute(info)
            }
        }
    }

    private inner class LocalTaskListener : DownloadListener<T> {
        override fun onStateChange(info: T, t: Throwable?) {
            listener?.onStateChange(info, t)
            if (totalTasks > 1) {
                if (info.state == TaskInfo.State.COMPLETED) {
                    taskMap.remove(info)
                    successCount++
                    (listener as? MultiDownloadListener)?.onTotalProgress(successCount, failedCount, totalTasks)
                } else if (info.state == TaskInfo.State.CANCEL || info.state == TaskInfo.State.ERROR) {
                    taskMap.remove(info)
                    failedCount++
                    (listener as? MultiDownloadListener)?.onTotalProgress(successCount, failedCount, totalTasks)
                } else if (info.state == TaskInfo.State.START) {
                    (listener as? MultiDownloadListener)?.onTotalProgress(successCount, failedCount, totalTasks)
                }
            }
        }

        override fun onProgress(info: T) {
            listener?.onProgress(info)
        }
    }

    /**
     * 任务是否正在进行
     */
    fun isOngoing(): Boolean {
        return remaining() > 0
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
    @Synchronized
    fun cancel() {
        taskMap.values.forEach { it.dispose(true) }
        taskMap.clear()
        isCanceled = true
    }

    /**
     * 取消单个下载
     */
    @Synchronized
    fun cancel(info: T) {
        taskMap.remove(info)?.dispose(true)
        if (taskMap.isEmpty()) {
            isCanceled = true
        }
    }

    override fun isDisposed(): Boolean {
        return isCanceled
    }

    override fun dispose() {
        cancel()
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
        val allLength: Long = if (info.contentLength == 0L) {
            responseBody.contentLength()
        } else {
            info.contentLength
        }
        try {
            RandomAccessFile(file, "rwd").channel.use {
                val mappedBuffer = it.map(FileChannel.MapMode.READ_WRITE, info.completionLength, allLength - info.completionLength)
                val buffer = ByteArray(1024 * 80)
                var len = inputStream.read(buffer)
                while (len != -1) {
                    mappedBuffer.put(buffer, 0, len)
                    len = inputStream.read(buffer)
                }
            }
        } catch (e: Exception) {
            IOUtils.closeQuietly(inputStream)
        }
    }
}