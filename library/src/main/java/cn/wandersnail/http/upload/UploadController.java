package cn.wandersnail.http.upload;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import cn.wandersnail.http.TaskInfo;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * date: 2019/8/23 18:26
 * author: zengfansheng
 */
class UploadController<T> implements UploadProgressListener {
    private static final int UPDATE_LIMIT_DURATION = 100;//限制进度更新频率，毫秒
    private final UploadInfo<T> info;
    private final UploadListener<T> listener;
    private boolean isCancel;
    private long lastUpdateTime;//上次进度更新时间
    private final Map<FileInfo, Long> contentLengthMap = new HashMap<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    UploadController(@NonNull UploadInfo<T> info, UploadListener<T> listener) {
        this.info = info;
        this.listener = listener;
    }

    @Override
    public void onProgress(@NonNull FileInfo fileInfo, long progress, long max) {
        mainHandler.post(() -> {
            long completionLength = progress;
            Long contentLen = contentLengthMap.get(fileInfo);
            if (contentLen != null && contentLen > max) {
                completionLength += contentLen - max;
            } else {
                contentLengthMap.put(fileInfo, max);
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
                    listener.onProgress(fileInfo, completionLength, contentLen);
                }
                lastUpdateTime = System.currentTimeMillis();
            }
        });
    }

    public void onStart() {
        info.state = TaskInfo.State.START;
        if (listener != null) {
            listener.onStateChange(info.state, null);
        }
    }

    public void onResponse(Response<ResponseBody> response) {
        if (listener != null) {
            if (info.converter != null) {
                try {
                    ResponseBody body = response.body();
                    T convertedBody = info.converter.convert(body);
                    listener.onResponseBodyParse(response, convertedBody);
                    body.close();
                } catch (Exception e) {
                    listener.onConvertError(e);
                    listener.onResponseBodyParse(response, null);
                }
            } else {
                listener.onResponseBodyParse(response, null);
            }
        }        
    }

    public void onError(Throwable e) {
        info.state = TaskInfo.State.ERROR;
        if (listener != null) {
            listener.onStateChange(info.state, e);
        }
    }

    public void onComplete() {
        if (listener != null) {
            for (Map.Entry<FileInfo, Long> entry : contentLengthMap.entrySet()) {
                listener.onProgress(entry.getKey(), entry.getValue(), entry.getValue());
            }
        }
        info.state = TaskInfo.State.COMPLETED;
        if (listener != null) {
            listener.onStateChange(info.state, null);
        }
    }

    public void onCancel() {
        if (isCancel) {
            return;
        }
        isCancel = true;
        mainHandler.post(() -> {
            if (info.state == TaskInfo.State.ONGOING || info.state == TaskInfo.State.START) {
                info.state = TaskInfo.State.CANCEL;
                if (listener != null) {
                    listener.onStateChange(info.state, null);
                }
            }
        });
    }

    public boolean isCanceled() {
        return isCancel;
    }
}
