package cn.wandersnail.http.upload;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import cn.wandersnail.http.TaskInfo;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * date: 2019/8/23 18:26
 * author: zengfansheng
 */
class UploadObserver<T> implements Observer<Response<ResponseBody>>, Disposable, UploadProgressListener {
    private static final int UPDATE_LIMIT_DURATION = 500;//限制进度更新频率，毫秒
    private final UploadInfo<T> info;
    private final UploadListener<T> listener;
    private Disposable disposable;
    private long lastUpdateTime;//上次进度更新时间
    private final Map<String, Long> contentLengthMap = new HashMap<>();

    UploadObserver(@NonNull UploadInfo<T> info, UploadListener<T> listener) {
        this.info = info;
        this.listener = listener;
    }

    @Override
    public void onProgress(String filename, long progress, long max) {
        AndroidSchedulers.mainThread().scheduleDirect(() -> {
            long completionLength = progress;
            Long contentLen = contentLengthMap.get(filename);
            if (contentLen != null && contentLen > max) {
                completionLength += contentLen - max;
            } else {
                contentLengthMap.put(filename, max);
                contentLen = max;
            }
            if (System.currentTimeMillis() - lastUpdateTime >= UPDATE_LIMIT_DURATION && (info.state == TaskInfo.State.IDLE ||
                    info.state == TaskInfo.State.START || info.state == TaskInfo.State.ONGOING)) {
                if (info.state != TaskInfo.State.ONGOING) {
                    info.state = TaskInfo.State.ONGOING;
                    if (listener != null) {
                        listener.onStateChange(info.state, null);
                    }
                }
                if (listener != null) {
                    listener.onProgress(filename, completionLength, contentLen);
                }
                lastUpdateTime = System.currentTimeMillis();
            }
        });
    }

    @Override
    public void onSubscribe(Disposable d) {
        disposable = d;
        info.state = TaskInfo.State.START;
        if (listener != null) {
            listener.onStateChange(info.state, null);
        }
    }

    @Override
    public void onNext(Response<ResponseBody> response) {
        if (listener != null) {
            if (info.converter != null) {
                try {
                    T convertedBody = info.converter.convert(response.body());
                    listener.onResponseBodyParse(response, convertedBody);
                } catch (Exception e) {
                    listener.onConvertError(e);
                    listener.onResponseBodyParse(response, null);
                }
            } else {
                listener.onResponseBodyParse(response, null);
            }
        }        
    }

    @Override
    public void onError(Throwable e) {
        disposable = null;
        info.state = TaskInfo.State.ERROR;
        if (listener != null) {
            listener.onStateChange(info.state, e);
        }
    }

    @Override
    public void onComplete() {
        disposable = null;
        if (listener != null) {
            for (Map.Entry<String, Long> entry : contentLengthMap.entrySet()) {
                listener.onProgress(entry.getKey(), entry.getValue(), entry.getValue());
            }
        }
        info.state = TaskInfo.State.COMPLETED;
        if (listener != null) {
            listener.onStateChange(info.state, null);
        }
    }

    @Override
    public void dispose() {
        AndroidSchedulers.mainThread().scheduleDirect(() -> {
            if (!isDisposed()) {
                disposable.dispose();
            }
            if (info.state == TaskInfo.State.ONGOING || info.state == TaskInfo.State.START) {
                info.state = TaskInfo.State.CANCEL;
                if (listener != null) {
                    listener.onStateChange(info.state, null);
                }
            }
        });
    }

    @Override
    public boolean isDisposed() {
        return disposable == null || disposable.isDisposed();
    }
}
