package cn.wandersnail.http.upload;

import java.io.File;
import java.io.FileInputStream;
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
    private final String name;
    private final File file;
    private final UploadProgressListener listener;

    ProgressRequestBody(MediaType contentType, String name, File file, UploadProgressListener listener) {
        this.contentType = contentType;
        this.name = name;
        this.file = file;
        this.listener = listener;
    }

    @Override
    public MediaType contentType() {
        return contentType;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        InputStream input = null;
        long uploadCount = 0;
        try {
            byte[] buffer = new byte[10240];
            input = new FileInputStream(file);
            int len;
            while ((len = input.read(buffer)) != -1) {
                sink.write(buffer, 0, len);
                uploadCount += len;
                if (listener != null) {
                    listener.onProgress(name, uploadCount, contentLength());
                }
            }
        } finally {
            HttpUtils.closeQuietly(input);
        }
    }
}
