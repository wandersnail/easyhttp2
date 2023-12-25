package cn.wandersnail.http;

import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * 配置
 * <p>
 * date: 2019/8/23 21:01
 * author: zengfansheng
 */
public class Configuration extends BaseConfiguration {    
    /**
     * 请求头
     */
    public Map<String, String> headers;
    /**
     * 使用自定义的Retrofit
     */
    public Retrofit retrofit;
    public OkHttpClient client;
    HttpService service;
}
