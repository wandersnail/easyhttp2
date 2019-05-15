package com.snail.network.upload

import io.reactivex.Observable
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PartMap
import retrofit2.http.Url

/**
 *
 *
 * date: 2019/2/28 13:48
 * author: zengfansheng
 */
internal interface UploadService {
    @POST
    @Multipart
    fun upload(@Url url: String, @PartMap args: Map<String, @JvmSuppressWildcards RequestBody>): Observable<Response<ResponseBody>>
}