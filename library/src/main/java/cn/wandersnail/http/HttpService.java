package cn.wandersnail.http;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
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
    Observable<Response<ResponseBody>> get(@Url String url);

    @GET
    Call<ResponseBody> getSync(@Url String url);

    @GET
    Observable<Response<ResponseBody>> get(@Url String url, @HeaderMap Map<String, String> headers);

    @GET
    Call<ResponseBody> getSync(@Url String url, @HeaderMap Map<String, String> headers);

    @POST
    @FormUrlEncoded
    Observable<Response<ResponseBody>> postForm(@Url String url, @FieldMap Map<String, Object> map);

    @POST
    Observable<Response<ResponseBody>> post(@Url String url, @Body RequestBody body);

    @POST
    Observable<Response<ResponseBody>> post(@Url String url, @FieldMap Map<String, Object> map, @Body RequestBody body);

    @POST
    @FormUrlEncoded
    Call<ResponseBody> postFormSync(@Url String url, @FieldMap Map<String, Object> map);

    @POST
    Call<ResponseBody> postSync(@Url String url, @Body RequestBody body);
    
    @POST
    Call<ResponseBody> postSync(@Url String url, @FieldMap Map<String, Object> map, @Body RequestBody body);

    @POST
    @FormUrlEncoded
    Observable<Response<ResponseBody>> postForm(@Url String url, @FieldMap Map<String, Object> map, @HeaderMap Map<String, String> headers);

    @POST
    Observable<Response<ResponseBody>> post(@Url String url, @Body RequestBody body, @HeaderMap Map<String, String> headers);

    @POST
    Observable<Response<ResponseBody>> post(@Url String url, @FieldMap Map<String, Object> map, @Body RequestBody body, @HeaderMap Map<String, String> headers);

    @POST
    @FormUrlEncoded
    Call<ResponseBody> postFormSync(@Url String url, @FieldMap Map<String, Object> map, @HeaderMap Map<String, String> headers);

    @POST
    Call<ResponseBody> postSync(@Url String url, @Body RequestBody body, @HeaderMap Map<String, String> headers);

    @POST
    Call<ResponseBody> postSync(@Url String url, @FieldMap Map<String, Object> map, @Body RequestBody body, @HeaderMap Map<String, String> headers);
    
    @DELETE
    Observable<Response<ResponseBody>> delete(@Url String url);

    @DELETE
    Observable<Response<ResponseBody>> deleteParams(@Url String url, @QueryMap Map<String, Object> params);

    @HTTP(method = "DELETE", hasBody = true)
    Observable<Response<ResponseBody>> delete(@Url String url, @Body RequestBody body);

    @HTTP(method = "DELETE", hasBody = true)
    Observable<Response<ResponseBody>> deleteParamsAndBody(@Url String url, @QueryMap Map<String, Object> params, @Body RequestBody body);
    
    @DELETE
    Call<ResponseBody> deleteSync(@Url String url);

    @DELETE
    Call<ResponseBody> deleteParamsSync(@Url String url, @QueryMap Map<String, Object> params);

    @HTTP(method = "DELETE", hasBody = true)
    Call<ResponseBody> deleteSync(@Url String url, @Body RequestBody body);

    @HTTP(method = "DELETE", hasBody = true)
    Call<ResponseBody> deleteParamsAndBodySync(@Url String url, @QueryMap Map<String, Object> params, @Body RequestBody body);

    @DELETE
    Observable<Response<ResponseBody>> delete(@Url String url, @HeaderMap Map<String, String> headers);

    @DELETE
    Observable<Response<ResponseBody>> deleteParams(@Url String url, @HeaderMap Map<String, String> headers,
                                                    @QueryMap Map<String, Object> params);

    @HTTP(method = "DELETE", hasBody = true)
    Observable<Response<ResponseBody>> delete(@Url String url, @Body RequestBody body, @HeaderMap Map<String, String> headers);

    @HTTP(method = "DELETE", hasBody = true)
    Observable<Response<ResponseBody>> delete(@Url String url, @HeaderMap Map<String, String> headers,
                                              @QueryMap Map<String, Object> params, @Body RequestBody body);

    @DELETE
    Call<ResponseBody> deleteSync(@Url String url, @HeaderMap Map<String, String> headers);

    @DELETE
    Call<ResponseBody> deleteParamsSync(@Url String url, @HeaderMap Map<String, String> headers,
                                        @QueryMap Map<String, Object> params);

    @HTTP(method = "DELETE", hasBody = true)
    Call<ResponseBody> deleteSync(@Url String url, @Body RequestBody body, @HeaderMap Map<String, String> headers);

    @HTTP(method = "DELETE", hasBody = true)
    Call<ResponseBody> deleteSync(@Url String url, @HeaderMap Map<String, String> headers,
                                  @QueryMap Map<String, Object> params, @Body RequestBody body);
}
