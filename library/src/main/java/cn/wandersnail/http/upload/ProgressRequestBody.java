package cn.wandersnail.http.upload;

import androidx.annotation.NonNull;

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
    private final FileInfo fileInfo;
    private final InternalUploadListener listener;

    ProgressRequestBody(MediaType contentType, FileInfo fileInfo, @NonNull InternalUploadListener listener) {
        this.contentType = contentType;
        this.fileInfo = fileInfo;
        this.listener = listener;
    }

    @Override
    public MediaType contentType() {
        return contentType;
    }

    @Override
    public long contentLength() throws IOException {
        return fileInfo.getFile() == null ? fileInfo.getInputStream().available() : fileInfo.getFile().length();
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        long uploadCount = 0;
        InputStream inputStream = null;
        try {
            byte[] buffer = new byte[10240];
            int len;
            if (fileInfo.getFile() != null) {
                inputStream = new FileInputStream(fileInfo.getFile());
            } else {
                inputStream = fileInfo.getInputStream();
            }
            while ((len = inputStream.read(buffer)) != -1) {
                sink.write(buffer, 0, len);
                uploadCount += len;
                listener.onProgress(fileInfo, uploadCount, contentLength());
            }
            listener.onComplete(fileInfo);
        } finally {
            HttpUtils.closeQuietly(inputStream);
        }
    }
}
