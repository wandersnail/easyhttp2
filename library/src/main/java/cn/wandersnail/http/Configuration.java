package cn.wandersnail.http;

import retrofit2.Retrofit;

/**
 * 配置
 * <p>
 * date: 2019/8/23 21:01
 * author: zengfansheng
 */
public class Configuration {
    /**
     * 是否绕过认证，也就是无条件信任所有HTTPS网站
     */
    public boolean bypassAuth;
    /**
     * 整个请求的超时时长，单位：秒，有效值：大于0
     */
    public int callTimeout;
    /**
     * 使用自定义的Retrofit
     */
    public Retrofit retrofit;
    HttpService service;
}
