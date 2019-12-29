package cn.wandersnail.http.upload;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import cn.wandersnail.http.TaskInfo;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * date: 2019/8/23 17:46
 * author: zengfansheng
 */
public class UploadInfo<T> extends TaskInfo {
    Converter<ResponseBody, T> converter;
    Map<String, String> paramParts;
    final Map<String, File> fileParts;
    OkHttpClient client;
    Map<String, String> headers;

    public UploadInfo(@NonNull String url, @NonNull Map<String, File> fileParts) {
        this(UUID.randomUUID().toString(), url, fileParts);
    }

    public UploadInfo(String tag, @NonNull String url, Map<String, File> fileParts) {
        super(tag, url);
        this.fileParts = fileParts;        
    }

    public UploadInfo(@NonNull String url, @NonNull Map<String, File> fileParts, @NonNull Map<String, String> headers) {
        this(UUID.randomUUID().toString(), url, fileParts);
        this.headers = headers;
    }

    public UploadInfo(String tag, @NonNull String url, Map<String, File> fileParts, @NonNull Map<String, String> headers) {
        super(tag, url);
        this.fileParts = fileParts;
        this.headers = headers;
    }

    /**
     * 设置响应体转换器
     */
    public void setConverter(@NonNull Converter<ResponseBody, T> converter) {
        this.converter = converter;
    }

    /**
     * 设置携带的参数
     */
    public void setParamParts(@NonNull Map<String, String> paramParts) {
        this.paramParts = paramParts;
    }

    /**
     * 设置自定义的OkHttpClient
     */
    public void setClient(@NonNull OkHttpClient client) {
        this.client = client;
    }
}
