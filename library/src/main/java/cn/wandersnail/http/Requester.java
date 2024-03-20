package cn.wandersnail.http;

import java.util.concurrent.TimeUnit;

import cn.wandersnail.http.callback.Cancelable;
import cn.wandersnail.http.callback.RequestCallback;
import cn.wandersnail.http.util.HttpUtils;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * date: 2021/4/8 15:42
 * author: zengfansheng
 */
abstract class Requester<T> {
    protected Configuration configuration;
    protected String url;
    protected Converter<ResponseBody, T> converter;

    /**
     * 同步请求
     */
    public abstract ConvertedResponse<T> execute();

    /**
     * 异步请求
     */
    public abstract Cancelable enqueue(RequestCallback<T> callback);
    
    protected void handleConfiguration(String url, Configuration configuration) {
        String baseUrl = HttpUtils.getBaseUrl(url);
        Configuration config = configuration;
        if (config == null) {
            GlobalConfiguration globalConfig = EasyHttp.getInstance().getGlobalConfiguration();
            config = new Configuration();
            config.bypassAuth = globalConfig.bypassAuth;
            config.callTimeout = globalConfig.callTimeout;
        }
        if (config.retrofit == null) {
            int timeout = config.callTimeout > 0 ? config.callTimeout : 5;
            OkHttpClient client = config.client;
            if (client == null) {
                client = HttpUtils.initHttpsClient(config.bypassAuth, new OkHttpClient.Builder())
                        .readTimeout(timeout, TimeUnit.SECONDS)
                        .connectTimeout(timeout, TimeUnit.SECONDS)
                        .build();
            }
            config.retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .build();
        }
        config.service = config.retrofit.create(HttpService.class);
        this.configuration = config;
    }
    
    protected ConvertedResponse<T> execute(Call<ResponseBody> call) {
        return new SyncGeneralRequestTask<>(call, converter, configuration).convertedResp;
    }

    protected Cancelable enqueue(Call<ResponseBody> call, RequestCallback<T> callback) {
        return new GeneralRequestTask<>(call, converter, configuration, callback);
    }
}
