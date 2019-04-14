package com.snail.network.utils

import java.io.Closeable
import java.io.IOException

internal object IOUtils {
    /**
     * 关闭一个或多个流对象
     * @param closeables 可关闭的流对象列表
     */
    @Throws(IOException::class)
    fun close(vararg closeables: Closeable?) {
        closeables.forEach { it?.close() }
    }

    /**
     * 关闭一个或多个流对象，内部捕获IO异常
     * @param closeables 可关闭的流对象列表
     */
    fun closeQuietly(vararg closeables: Closeable?) {
        try {
            close(*closeables)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}