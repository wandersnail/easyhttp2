package cn.wandersnail.http;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import cn.wandersnail.http.callback.RequestCallback;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * date: 2021/4/8 16:39
 * author: zengfansheng
 */
public class PostRequester<T> extends Requester<T> {
    private RequestBody body;
    private Map<String, Object> params;
    private boolean isJsonBody;

    /**
     * 自定义配置
     */
    public PostRequester<T> setConfiguration(@NonNull Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * 请求路径
     */
    public PostRequester<T> setUrl(@NonNull String url) {
        this.url = url;
        return this;
    }

    /**
     * 响应体转换器
     */
    public PostRequester<T> setConverter(@NonNull Converter<ResponseBody, T> converter) {
        this.converter = converter;
        return this;
    }
    
    /**
     * 文本请求体
     */
    public PostRequester<T> setTextBody(@NonNull String text) {
        body = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text);
        return this;
    }

    /**
     * json请求体
     */
    public PostRequester<T> setJsonBody(@NonNull String json) {
        body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json);
        isJsonBody = true;
        return this;
    }

    /**
     * 自定义请求体
     */
    public PostRequester<T> setBody(@NonNull RequestBody body) {
        this.body = body;
        return this;
    }

    /**
     * 请求参数
     */
    public PostRequester<T> setParams(@NonNull Map<String, Object> params) {
        this.params = params;
        return this;
    }

    @Override
    public ConvertedResponse<T> execute() {
        handleConfiguration(url, configuration);
        if (isJsonBody) {
            if (configuration.headers == null) {
                configuration.headers = new HashMap<>();
            }
            configuration.headers.put("Content-Type", "application/json;charset=utf-8");
            configuration.headers.put("Accept", "application/json;");
        }
        if (configuration.headers != null && !configuration.headers.isEmpty()) {
            if (params != null) {
                if (body != null) {
                    return execute(configuration.service.postSync(url, configuration.headers, params, body));
                } else {
                    return execute(configuration.service.postFormSync(url, configuration.headers, params));
                }
            } else if (body != null) {
                return execute(configuration.service.postSync(url, configuration.headers, body));
            } else {
                return execute(configuration.service.postSync(url, configuration.headers));
            }
        } else {
            if (params != null) {
                if (body != null) {
                    return execute(configuration.service.postParamsAndBodySync(url, params, body));
                } else {
                    return execute(configuration.service.postFormSync(url, params));
                }
            } else if (body != null) {
                return execute(configuration.service.postSync(url, body));
            } else {
                return execute(configuration.service.postSync(url));
            }
        }
    }

    @Override
    public Disposable enqueue(RequestCallback<T> callback) {
        handleConfiguration(url, configuration);
        if (isJsonBody) {
            if (configuration.headers == null) {
                configuration.headers = new HashMap<>();
            }
            configuration.headers.put("Content-Type", "application/json;charset=utf-8");
            configuration.headers.put("Accept", "application/json;");
        }
        if (configuration.headers != null && !configuration.headers.isEmpty()) {
            if (params != null) {
                if (body != null) {
                    return enqueue(configuration.service.post(url, configuration.headers, params, body), callback);
                } else {
                    return enqueue(configuration.service.postForm(url, configuration.headers, params), callback);
                }
            } else if (body != null) {
                return enqueue(configuration.service.post(url, configuration.headers, body), callback);
            } else {
                return enqueue(configuration.service.post(url, configuration.headers), callback);
            }
        } else {
            if (params != null) {
                if (body != null) {
                    return enqueue(configuration.service.postParamsAndBody(url, params, body), callback);
                } else {
                    return enqueue(configuration.service.postForm(url, params), callback);
                }
            } else if (body != null) {
                return enqueue(configuration.service.post(url, body), callback);
            } else {
                return enqueue(configuration.service.post(url), callback);
            }
        }
    }
}
