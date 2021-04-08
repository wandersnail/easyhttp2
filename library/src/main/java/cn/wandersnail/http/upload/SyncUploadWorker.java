package cn.wandersnail.http.upload;

import android.os.Handler;
import android.os.Looper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import cn.wandersnail.http.ConvertedResponse;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * 同步上传任务
 * <p>
 * date: 2019/8/23 20:38
 * author: zengfansheng
 */
public class SyncUploadWorker<T> {
    public ConvertedResponse<T> convertedResp;

    public SyncUploadWorker(UploadInfo<T> info, UploadProgressListener listener) {
        Retrofit.Builder builder = new Retrofit.Builder();
        if (info.client != null) {
            builder.client(info.client);
        }
        UploadService service = builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(info.getBaseUrl())
                .build()
                .create(UploadService.class);
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
        if (info.paramParts != null) {
            for (Map.Entry<String, String> entry : info.paramParts.entrySet()) {
                bodyBuilder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }
        Handler handler = new Handler(Looper.getMainLooper());
        UploadProgressListener localListener = (name, progress, max) -> handler.post(() -> {
            if (listener != null) {
                handler.post(() -> listener.onProgress(name, progress, max));
            }
        });
        for (FileInfo fileInfo : info.fileInfos) {
            try {
                MultipartBody.Part part = MultipartBody.Part.createFormData(fileInfo.getFromDataName(),
                        URLEncoder.encode(fileInfo.getFilename(), "utf-8"),
                        new ProgressRequestBody(MediaType.parse("multipart/form-data"), fileInfo.getFilename(),
                                fileInfo.getInputStream(), localListener));
                bodyBuilder.addPart(part);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        Call<ResponseBody> call;
        if (info.headers == null || info.headers.isEmpty()) {
            call = service.uploadSync(info.url, bodyBuilder.build());
        } else {
            call = service.uploadSync(info.url, bodyBuilder.build(), info.headers);
        }
        convertedResp = new ConvertedResponse<>(call);
        try {
            Response<ResponseBody> response = call.execute();
            convertedResp.raw = response.raw();
            if (response.isSuccessful() && info.converter != null && response.body() != null) {
                try {
                    convertedResp.convertedResponse = info.converter.convert(response.body());
                } catch (Throwable t) {
                    convertedResp.convertError = t;
                }
            }
        } catch (Exception e) {
            //取消任务会抛异常
        }
    }
}
