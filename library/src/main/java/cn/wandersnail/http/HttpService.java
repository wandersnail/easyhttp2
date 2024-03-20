package cn.wandersnail.http;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * date: 2019/8/23 20:56
 * author: zengfansheng
 */
public interface HttpService {

    @GET
    Call<ResponseBody> get(@Url String url);

    @GET
    Call<ResponseBody> get(@Url String url, @HeaderMap Map<String, String> headers);

    @POST
    Call<ResponseBody> post(@Url String url);

    @POST
    Call<ResponseBody> post(@Url String url, @HeaderMap Map<String, String> headers);

    @POST
    @FormUrlEncoded
    Call<ResponseBody> postForm(@Url String url, @FieldMap Map<String, Object> params);

    @POST
    Call<ResponseBody> post(@Url String url, @Body RequestBody body);
    
    @POST
    Call<ResponseBody> postParamsAndBody(@Url String url, @FieldMap Map<String, Object> params, @Body RequestBody body);

    @POST
    @FormUrlEncoded
    Call<ResponseBody> postForm(@Url String url, @HeaderMap Map<String, String> headers, @FieldMap Map<String, Object> params);

    @POST
    Call<ResponseBody> post(@Url String url, @HeaderMap Map<String, String> headers, @Body RequestBody body);

    @POST
    Call<ResponseBody> post(@Url String url, @HeaderMap Map<String, String> headers, @FieldMap Map<String, Object> params, @Body RequestBody body);

    @DELETE
    Call<ResponseBody> delete(@Url String url);

    @DELETE
    Call<ResponseBody> deleteParams(@Url String url, @QueryMap Map<String, Object> params);

    @HTTP(method = "DELETE", hasBody = true)
    Call<ResponseBody> delete(@Url String url, @Body RequestBody body);

    @HTTP(method = "DELETE", hasBody = true)
    Call<ResponseBody> deleteParamsAndBody(@Url String url, @QueryMap Map<String, Object> params, @Body RequestBody body);

    @DELETE
    Call<ResponseBody> delete(@Url String url, @HeaderMap Map<String, String> headers);

    @DELETE
    Call<ResponseBody> deleteParams(@Url String url, @HeaderMap Map<String, String> headers,
                                        @QueryMap Map<String, Object> params);

    @HTTP(method = "DELETE", hasBody = true)
    Call<ResponseBody> delete(@Url String url, @HeaderMap Map<String, String> headers, @Body RequestBody body);

    @HTTP(method = "DELETE", hasBody = true)
    Call<ResponseBody> deleteParamsAndBody(@Url String url, @HeaderMap Map<String, String> headers,
                                                           @QueryMap Map<String, Object> params, @Body RequestBody body);
}
