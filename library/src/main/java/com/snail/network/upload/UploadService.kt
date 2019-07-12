package com.snail.network.upload

import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

/**
 *
 *
 * date: 2019/2/28 13:48
 * author: zengfansheng
 */
internal interface UploadService {
    @POST
    fun upload(@Url url: String, @Body body: MultipartBody): Observable<Response<ResponseBody>>

    @POST
    fun uploadSync(@Url url: String, @Body body: MultipartBody): Call<ResponseBody>
}