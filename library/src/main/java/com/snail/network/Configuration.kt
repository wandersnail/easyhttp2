package com.snail.network

import retrofit2.Retrofit

/**
 * 配置
 *
 * date: 2019/4/1 11:17
 * author: zengfansheng
 */
class Configuration {
    /** 是否绕过认证，也就是无条件信任所有HTTPS网站 */
    var bypassAuth = false
    /** 整个请求的超时时长，单位：秒，有值为大于0 */
    var callTimeout = 0
    var retrofit: Retrofit? = null
    internal var service: HttpService? = null
}