package cn.wandersnail.http.upload;

import java.io.IOException;
import java.io.InputStream;

import cn.wandersnail.http.util.HttpUtils;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * date: 2019/8/23 18:17
 * author: zengfansheng
 */
class ProgressRequestBody extends RequestBody {
    private final MediaType contentType;
    private final String filename;
    private final InputStream inputStream;
    private final UploadProgressListener listener;

    ProgressRequestBody(MediaType contentType, String filename, InputStream inputStream, UploadProgressListener listener) {
        this.contentType = contentType;
        this.filename = filename;
        this.inputStream = inputStream;
        this.listener = listener;
    }

    @Override
    public MediaType contentType() {
        return contentType;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long uploadCount = 0;
        try {
            byte[] buffer = new byte[10240];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                sink.write(buffer, 0, len);
                uploadCount += len;
                if (listener != null) {
                    listener.onProgress(filename, uploadCount, contentLength());
                }
            }
        } finally {
            HttpUtils.closeQuietly(inputStream);
        }
    }
}
