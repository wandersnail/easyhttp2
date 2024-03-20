package cn.wandersnail.http.upload;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * date: 2019/8/23 18:09
 * author: zengfansheng
 */
interface UploadService {
    @POST
    Call<ResponseBody> upload(@Url String url, @Body MultipartBody body);

    @POST
    Call<ResponseBody> upload(@Url String url, @Body MultipartBody body, @HeaderMap Map<String, String> headers);
}
