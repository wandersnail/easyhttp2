package com.snail.network.upload

import com.snail.network.callback.ProgressListener
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.internal.Util
import okio.BufferedSink
import okio.Okio
import okio.Source
import java.io.File
import java.io.IOException

/**
 *
 *
 * date: 2019/2/28 12:25
 * author: zengfansheng
 */
internal class ProgressRequestBody(private val contentType: MediaType?, private val file: File, private val listener: ProgressListener?) : RequestBody() {
    override fun contentType(): MediaType? {
        return contentType
    }

    override fun contentLength(): Long {
        return file.length()
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        var source: Source? = null
        var uploaded = 0L
        val uploadCount = 50000L//一次上传多少
        try {
            source = Okio.source(file)
            source.read(sink.buffer(), uploadCount)
            uploaded += uploadCount
            if (contentLength() > 0 && uploaded > 0) {
                listener?.onProgress(uploaded, contentLength())
            }
        } finally {
            Util.closeQuietly(source)
        }
    }
}