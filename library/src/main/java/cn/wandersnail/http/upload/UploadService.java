package cn.wandersnail.http.upload;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * date: 2019/8/23 18:09
 * author: zengfansheng
 */
interface UploadService {
    @POST
    Observable<Response<ResponseBody>> upload(@Url String url, @Body MultipartBody body);

    @POST
    Call<ResponseBody> uploadSync(@Url String url, @Body MultipartBody body);
}
