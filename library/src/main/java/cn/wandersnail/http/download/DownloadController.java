package cn.wandersnail.http.download;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import cn.wandersnail.http.EasyHttp;
import cn.wandersnail.http.TaskInfo;
import cn.wandersnail.http.callback.ProgressListener;
import cn.wandersnail.http.util.HttpUtils;

/**
 * date: 2019/8/23 15:49
 * author: zengfansheng
 */
class DownloadController<T extends DownloadInfo> implements ProgressListener {
    private static final int UPDATE_LIMIT_DURATION = 500;//限制进度更新频率，毫秒
    private final T info;
    private final DownloadListener<T> listener;
    private boolean isCancel;
    private long lastUpdateTime;//上次进度更新时间
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    DownloadController(@NonNull T info, DownloadListener<T> listener) {
        this.info = info;
        this.listener = listener;
    }

    @Override
    public void onProgress(long progress, long max) {
        mainHandler.post(() -> {
            long completionLength = progress;
            if (info.contentLength > max) {
                completionLength += info.contentLength - max;
            } else {
                info.contentLength = max;
            }
            info.completionLength = completionLength;
            if (System.currentTimeMillis() - lastUpdateTime >= UPDATE_LIMIT_DURATION && (info.state == TaskInfo.State.IDLE ||
                    info.state == TaskInfo.State.START || info.state == TaskInfo.State.ONGOING)) {
                if (info.state != TaskInfo.State.ONGOING) {
                    info.state = TaskInfo.State.ONGOING;
                    if (listener != null) {
                        listener.onStateChange(info, null);
                    }
                }
                updateProgress();
                lastUpdateTime = System.currentTimeMillis();
            }
        });
    }

    public void onStart() {
        info.state = TaskInfo.State.START;
        if (listener != null) {
            mainHandler.post(()-> listener.onStateChange(info, null));
        }
    }

    public void onError(Throwable e) {
        info.state = TaskInfo.State.ERROR;
        info.getTemporaryFile().delete();
        if (listener != null) {
            mainHandler.post(()-> listener.onStateChange(info, e));
        }
    }

    public void onComplete() {
        EasyHttp.executeOnIo(() -> {
            //将临时文件重命名为目标路径
            File destFile = new File(info.savePath);
            File parentFile = destFile.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs();
            }
            if (destFile.exists()) {
                destFile.delete();//如果目标有文件，删除
            }
            File tempFile = info.getTemporaryFile();
            FileUtil.copyFile(tempFile, destFile);
            boolean success = destFile.exists() && tempFile.length() == destFile.length();
            if (!success) {
                destFile.delete();
            }
            tempFile.delete();
            mainHandler.post(() -> {
                if (success) {
                    //更新进度
                    info.completionLength = info.contentLength;
                    updateProgress();
                    info.state = TaskInfo.State.COMPLETED;
                    if (listener != null) {
                        listener.onStateChange(info, null);
                    }
                } else {
                    info.state = TaskInfo.State.ERROR;
                    if (listener != null) {
                        listener.onStateChange(info, new Throwable("Renaming to target file failed"));
                    }
                }
            });
        });
    }

    private void updateProgress() {
        if (info.completionLength > 0 && info.contentLength > 0 && listener != null) {
            listener.onProgress(info, (int) (info.completionLength * 100 / info.contentLength));
        }
    }

    public boolean isCancel() {
        return isCancel;
    }

    public void cancel() {
        if (isCancel) {
            return;
        }
        isCancel = true;
        if (info.state != TaskInfo.State.CANCEL && info.state != TaskInfo.State.ERROR) {
            info.state = TaskInfo.State.CANCEL;
            info.getTemporaryFile().delete();
            if (listener != null) {
                listener.onStateChange(info, null);
            }
        }
    }

    public void pause() {
        if (isCancel) {
            return;
        }
        if (info.state == TaskInfo.State.ONGOING || info.state == TaskInfo.State.START) {
            info.state = TaskInfo.State.PAUSE;
            if (listener != null) {
                listener.onStateChange(info, null);
            }
        }
    }
}
