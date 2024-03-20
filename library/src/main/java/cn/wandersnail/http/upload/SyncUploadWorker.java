package cn.wandersnail.http.upload;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.Map;

import cn.wandersnail.http.ConvertedResponse;
import cn.wandersnail.http.util.HttpUtils;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * 同步上传任务
 * <p>
 * date: 2019/8/23 20:38
 * author: zengfansheng
 */
public class SyncUploadWorker<T> {
    public ConvertedResponse<T> convertedResp;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public SyncUploadWorker(UploadInfo<T> info, UploadProgressListener listener) {
        Retrofit.Builder builder = new Retrofit.Builder();
        if (info.client == null) {
            builder.client(HttpUtils.initHttpsClient(true, new OkHttpClient.Builder()).build());
        } else {
            builder.client(info.client);
        }
        UploadService service = builder.baseUrl(info.getBaseUrl())
                .build()
                .create(UploadService.class);
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
        bodyBuilder.setType(MultipartBody.FORM);
        if (info.paramParts != null) {
            for (Map.Entry<String, String> entry : info.paramParts.entrySet()) {
                bodyBuilder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }
        final InternalUploadListener localListener = new InternalUploadListener() {
            @Override
            public void onComplete(@NonNull FileInfo fileInfo) {
            }

            @Override
            public void onProgress(@NonNull FileInfo fileInfo, long progress, long max) {
                if (listener != null) {
                    handler.post(() -> listener.onProgress(fileInfo, progress, max));
                }
            }
        };
        for (FileInfo fileInfo : info.fileInfos) {
            ProgressRequestBody body = new ProgressRequestBody(fileInfo.getMediaType(), fileInfo, localListener);
            bodyBuilder.addFormDataPart(fileInfo.getFormDataName(), fileInfo.getFilename(), body);
        }
        Call<ResponseBody> call;
        if (info.headers == null || info.headers.isEmpty()) {
            call = service.upload(info.url, bodyBuilder.build());
        } else {
            call = service.upload(info.url, bodyBuilder.build(), info.headers);
        }
        convertedResp = new ConvertedResponse<>(call);
        try {
            Response<ResponseBody> response = call.execute();
            convertedResp.response = response;
            ResponseBody body = response.body();
            if (response.isSuccessful() && info.converter != null && body != null) {
                try {
                    convertedResp.convertedResponse = info.converter.convert(body);
                    body.close();
                } catch (Throwable t) {
                    convertedResp.convertError = t;
                }
            }
        } catch (Exception e) {
            //取消任务会抛异常
        }
    }
}
