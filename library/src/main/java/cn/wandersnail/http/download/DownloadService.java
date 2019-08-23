package cn.wandersnail.http.download;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * 下载接口
 * 
 * date: 2019/8/23 15:38
 * author: zengfansheng
 */
public interface DownloadService {
    @Streaming
    @GET
    Observable<Response<ResponseBody>> download(@Header("RANGE") String offset, @Url String url);
}
