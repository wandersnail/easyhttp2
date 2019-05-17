package com.snail.network

import io.reactivex.Observable
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

/**
 * 
 *
 * date: 2019/2/25 10:03
 * author: zengfansheng
 */
internal interface HttpService {
    @GET
    fun get(@Url url: String): Observable<Response<ResponseBody>>

    @POST
    @FormUrlEncoded
    fun postForm(@Url url: String, @FieldMap map: Map<String, @JvmSuppressWildcards Any>): Observable<Response<ResponseBody>>

    @POST
    @Headers("Content-Type:application/json;charset=utf-8", "Accept:application/json;")
    fun postJson(@Url url: String, @Body body: RequestBody): Observable<Response<ResponseBody>>

    @POST
    fun post(@Url url: String, @Body body: RequestBody): Observable<Response<ResponseBody>>

    @GET
    fun getSync(@Url url: String): Call<ResponseBody>

    @POST
    @FormUrlEncoded
    fun postFormSync(@Url url: String, @FieldMap map: Map<String, @JvmSuppressWildcards Any>): Call<ResponseBody>

    @POST
    @Headers("Content-Type:application/json;charset=utf-8", "Accept:application/json;")
    fun postJsonSync(@Url url: String, @Body body: RequestBody): Call<ResponseBody>

    @POST
    fun postSync(@Url url: String, @Body body: RequestBody): Call<ResponseBody>
}