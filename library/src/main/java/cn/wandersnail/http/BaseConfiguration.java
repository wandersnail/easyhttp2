package cn.wandersnail.http;

/**
 * date: 2021/10/29 13:12
 * author: zengfansheng
 */
public abstract class BaseConfiguration {
    /**
     * 是否绕过认证，也就是无条件信任所有HTTPS网站
     */
    public boolean bypassAuth;
    /**
     * 整个请求的超时时长，单位：秒，有效值：大于0
     */
    public int callTimeout;      
}
