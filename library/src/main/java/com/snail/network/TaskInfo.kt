package com.snail.network

import com.snail.network.utils.HttpUtils
import java.util.*

/**
 *
 *
 * date: 2019/2/28 14:46
 * author: zengfansheng
 */
abstract class TaskInfo(
        /** 请求地址 */
        val url: String,
        /** 唯一标识 */
        val tag: String = UUID.randomUUID().toString()) {

    /** 任务状态 */
    var state = State.IDLE
        internal set
    /** 基础url */
    val baseUrl: String = HttpUtils.getBaseUrl(url)    
    internal open fun reset() {}
    
    enum class State {
        IDLE, START, ONGOING, COMPLETED, CANCEL, ERROR, PAUSE
    }
}