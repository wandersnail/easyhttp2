package com.snail.network.download

import com.snail.network.utils.HttpUtils
import java.io.File
import java.util.*

/**
 * 时间: 2017/7/8 20:13
 * 作者: zengfansheng
 * 邮箱: 43068145@qq.com
 * 功能: 下载信息类，包含下载状态及进度监听
 */

open class DownloadInfo @JvmOverloads constructor(
    /** 下载url */
    val url: String,
    /** 下载路径 */
    val savePath: String,
    /** 唯一标识 */
    val tag: String = UUID.randomUUID().toString(),
    /** 超时设置，单位秒 */
    val connectTimeout: Int = 5) {

    /** 下载状态 */
    var state = State.IDLE
        internal set
    /** 基础url */
    val baseUrl: String = HttpUtils.getBaseUrl(url)
    /** 文件总长度 */
    var contentLength: Long = 0
        internal set
    /** 已下载长度 */
    var readLength: Long = 0
        internal set
    /**
     * 获取下载的临时文件，下载完成后再重命名
     */
    internal val temporaryFilePath: String
        get() = "$savePath.temp"

    internal fun reset() {
        readLength = 0
        contentLength = 0
        File(temporaryFilePath).delete()
    }
    
    override fun equals(other: Any?): Boolean {
        return this === other || other is DownloadInfo && url == other.url
    }

    override fun hashCode(): Int {
        return url.hashCode()
    }

    enum class State {
        IDLE, START, DOWNLOADING, COMPLETED, CANCEL, ERROR, PAUSE
    }
}
