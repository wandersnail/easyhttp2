package cn.wandersnail.http.download;

import okhttp3.ResponseBody;
import retrofit2.Call;
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
    Call<ResponseBody> download(@Header("RANGE") String offset, @Url String url);
}
