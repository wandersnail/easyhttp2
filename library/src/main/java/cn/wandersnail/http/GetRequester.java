package cn.wandersnail.http;

import androidx.annotation.NonNull;

import cn.wandersnail.http.callback.Cancelable;
import cn.wandersnail.http.callback.RequestCallback;
import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * date: 2021/4/8 16:12
 * author: zengfansheng
 */
public class GetRequester<T> extends Requester<T> {
    /**
     * 自定义配置
     */
    public GetRequester<T> setConfiguration(@NonNull Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * 请求路径
     */
    public GetRequester<T> setUrl(@NonNull String url) {
        this.url = url;
        return this;
    }

    /**
     * 响应体转换器
     */
    public GetRequester<T> setConverter(@NonNull Converter<ResponseBody, T> converter) {
        this.converter = converter;
        return this;
    }
    
    @Override
    public ConvertedResponse<T> execute() {
        handleConfiguration(url, configuration);
        if (configuration.headers != null && !configuration.headers.isEmpty()) {
            return execute(configuration.service.get(url, configuration.headers));
        } else {
            return execute(configuration.service.get(url));
        }
    }

    @Override
    public Cancelable enqueue(RequestCallback<T> callback) {
        handleConfiguration(url, configuration);
        if (configuration.headers != null && !configuration.headers.isEmpty()) {
            return enqueue(configuration.service.get(url, configuration.headers), callback);
        } else {
            return enqueue(configuration.service.get(url), callback);
        }
    }
}
