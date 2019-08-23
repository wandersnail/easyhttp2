package cn.wandersnail.http.download;

import java.io.IOException;

import cn.wandersnail.http.callback.ProgressListener;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * date: 2019/8/23 15:44
 * author: zengfansheng
 */
class ProgressResponseBody extends ResponseBody {
    private final ResponseBody responseBody;
    private final ProgressListener listener;
    private BufferedSource bufferedSource;

    /**
     * @param listener 进度监听
     */
    ProgressResponseBody(ResponseBody responseBody, ProgressListener listener) {
        this.responseBody = responseBody;
        this.listener = listener;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            private long totalReadBytes;
            
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long readBytes = super.read(sink, byteCount);
                totalReadBytes += readBytes != -1L ? readBytes : 0;
                if (totalReadBytes > 0 && contentLength() > 0 && listener != null) {
                    listener.onProgress(totalReadBytes, responseBody.contentLength());
                }
                return readBytes;
            }
        };
    }
}
