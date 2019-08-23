package cn.wandersnail.http.upload;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import cn.wandersnail.http.util.HttpUtils;
import cn.wandersnail.http.util.SchedulerUtils;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * 上传执行
 * <p>
 * date: 2019/8/23 21:41
 * author: zengfansheng
 */
public class UploadWorker<T> implements Disposable {
    private final UploadObserver<T> observer;

    public UploadWorker(UploadInfo<T> info, UploadListener<T> listener) {
        observer = new UploadObserver<>(info, listener);
        Retrofit.Builder builder = new Retrofit.Builder();
        if (info.client != null) {
            builder.client(HttpUtils.initHttpsClient(true, new OkHttpClient.Builder()).build());
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
        for (Map.Entry<String, File> entry : info.fileParts.entrySet()) {
            try {
                MultipartBody.Part part = MultipartBody.Part.createFormData(entry.getKey(),
                        URLEncoder.encode(entry.getValue().getName(), "utf-8"),
                        new ProgressRequestBody(MediaType.parse("multipart/form-data"), entry.getKey(),
                                entry.getValue(), observer));
                bodyBuilder.addPart(part);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        service.upload(info.url, bodyBuilder.build())
                .compose(SchedulerUtils.applyGeneralObservableSchedulers())
                .subscribe(observer);
    }

    @Override
    public void dispose() {
        observer.dispose();
    }

    @Override
    public boolean isDisposed() {
        return observer.isDisposed();
    }
    
    public void cancel() {
        dispose();
    }
}
