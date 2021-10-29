package cn.wandersnail.http.upload;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import cn.wandersnail.http.util.HttpUtils;
import cn.wandersnail.http.util.SchedulerUtils;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
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
        for (FileInfo fileInfo : info.fileInfos) {
            try {
                MultipartBody.Part part = MultipartBody.Part.createFormData(fileInfo.getFormDataName(),
                        URLEncoder.encode(fileInfo.getFilename(), "utf-8"),
                        new ProgressRequestBody(MediaType.parse("multipart/form-data"), fileInfo.getFilename(),
                                fileInfo.getInputStream(), observer));
                bodyBuilder.addPart(part);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        Observable<Response<ResponseBody>> observable;
        if (info.headers == null || info.headers.isEmpty()) {
            observable = service.upload(info.url, bodyBuilder.build());
        } else {
            observable = service.upload(info.url, bodyBuilder.build(), info.headers);
        }
        observable.compose(SchedulerUtils.applyGeneralObservableSchedulers())
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
