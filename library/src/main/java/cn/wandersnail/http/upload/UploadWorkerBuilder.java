package cn.wandersnail.http.upload;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * date: 2021/4/8 14:40
 * author: zengfansheng
 */
public class UploadWorkerBuilder<T> {
    private Converter<ResponseBody, T> converter;
    private Map<String, String> paramParts;
    private OkHttpClient client;
    private Map<String, String> headers;
    private List<FileInfo> fileInfos;
    private String tag;
    private String url;
    private UploadListener<T> listener;

    public UploadWorkerBuilder<T> setTag(@NonNull String tag) {
        this.tag = tag;
        return this;
    }

    public UploadWorkerBuilder<T> setUrl(@NonNull String url) {
        this.url = url;
        return this;
    }

    public UploadWorkerBuilder<T> setListener(UploadListener<T> listener) {
        this.listener = listener;
        return this;
    }

    public UploadWorkerBuilder<T> setFileParts(List<FileInfo> fileInfos) {
        this.fileInfos = fileInfos;
        return this;
    }

    public UploadWorkerBuilder<T> setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * 设置响应体转换器
     */
    public UploadWorkerBuilder<T> setConverter(@NonNull Converter<ResponseBody, T> converter) {
        this.converter = converter;
        return this;
    }

    /**
     * 设置携带的参数
     */
    public UploadWorkerBuilder<T> setParamParts(@NonNull Map<String, String> paramParts) {
        this.paramParts = paramParts;
        return this;
    }

    /**
     * 设置自定义的OkHttpClient
     */
    public UploadWorkerBuilder<T> setClient(@NonNull OkHttpClient client) {
        this.client = client;
        return this;
    }

    public UploadWorker<T> build() {
        UploadInfo<T> info;
        if (tag == null || tag.length() == 0) {
            info = new UploadInfo<>(url);
        } else {
            info = new UploadInfo<>(tag, url);
        }
        info.setClient(client)
                .setConverter(converter)
                .setFileParts(fileInfos)
                .setHeaders(headers)
                .setParamParts(paramParts);
        return new UploadWorker<>(info, listener);
    }
}
