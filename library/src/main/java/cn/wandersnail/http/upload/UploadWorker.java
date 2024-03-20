package cn.wandersnail.http.upload;

import androidx.annotation.NonNull;

import java.util.Map;

import cn.wandersnail.http.util.HttpUtils;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * 上传执行
 * <p>
 * date: 2019/8/23 21:41
 * author: zengfansheng
 */
public class UploadWorker<T> {
    private final Call<ResponseBody> call;
    private final UploadController<T> controller;
    private int completeCount;

    public UploadWorker(UploadInfo<T> info, UploadListener<T> listener) {
        controller = new UploadController<>(info, listener);
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
        final InternalUploadListener uploadListener = new InternalUploadListener() {
            @Override
            public void onComplete(@NonNull FileInfo fileInfo) {
                completeCount++;
                if (completeCount >= info.fileInfos.size()) {
                    controller.onComplete();
                }
            }

            @Override
            public void onProgress(@NonNull FileInfo fileInfo, long progress, long max) {
                controller.onProgress(fileInfo, progress, max);
            }
        };
        for (FileInfo fileInfo : info.fileInfos) {
            ProgressRequestBody body = new ProgressRequestBody(fileInfo.getMediaType(), fileInfo, uploadListener);
            bodyBuilder.addFormDataPart(fileInfo.getFormDataName(), fileInfo.getFilename(), body);
        }
        if (info.headers == null || info.headers.isEmpty()) {
            call = service.upload(info.url, bodyBuilder.build());
        } else {
            call = service.upload(info.url, bodyBuilder.build(), info.headers);
        }
        controller.onStart();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                controller.onResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                controller.onError(throwable);
            }
        });
    }    

    public boolean isCanceled() {
        return call.isCanceled();
    }
    
    public void cancel() {
        call.cancel();
        controller.onCancel();
    }
}
