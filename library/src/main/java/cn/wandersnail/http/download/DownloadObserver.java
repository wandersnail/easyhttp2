package cn.wandersnail.http.download;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.wandersnail.http.TaskInfo;
import cn.wandersnail.http.callback.ProgressListener;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * date: 2019/8/23 15:49
 * author: zengfansheng
 */
class DownloadObserver<T extends DownloadInfo> implements Observer<Response<ResponseBody>>, ProgressListener {
    private static final int UPDATE_LIMIT_DURATION = 500;//限制进度更新频率，毫秒
    private final T info;
    private final DownloadListener<T> listener;
    private Disposable disposable;
    private long lastUpdateTime;//上次进度更新时间

    DownloadObserver(@NonNull T info, DownloadListener<T> listener) {
        this.info = info;
        this.listener = listener;
    }

    @Override
    public void onProgress(long progress, long max) {
        AndroidSchedulers.mainThread().scheduleDirect(() -> {
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

    @Override
    public void onSubscribe(Disposable d) {
        disposable = d;
        info.state = TaskInfo.State.START;
        if (listener != null) {
            listener.onStateChange(info, null);
        }
    }

    @Override
    public void onNext(Response<ResponseBody> responseBodyResponse) {

    }

    @Override
    public void onError(Throwable e) {
        disposable = null;
        info.state = TaskInfo.State.ERROR;
        if (listener != null) {
            listener.onStateChange(info, e);
        }
    }

    @Override
    public void onComplete() {
        disposable = null;
        Schedulers.io().scheduleDirect(() -> {
            //将临时文件重命名为目标路径
            File destFile = new File(info.savePath);
            destFile.delete();//如果目标有文件，删除
            File tempFile = info.getTemporaryFile();
            copyFile(tempFile, destFile);            
            boolean success = destFile.exists() && tempFile.length() == destFile.length();
            if (!success) {
                destFile.delete();
            }
            tempFile.delete();
            AndroidSchedulers.mainThread().scheduleDirect(() -> {
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

    private void copyFile(@NonNull File src, @NonNull File target) {
        BufferedInputStream fis = null;
        BufferedOutputStream fos = null;
        try {
            fis = new BufferedInputStream(new FileInputStream(src));
            fos = new BufferedOutputStream(new FileOutputStream(target));
            byte[] buffer = new byte[40960];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
        } catch (Exception e) {
            target.delete();
        } finally {
            closeQuietly(fis, fos);
        }
    }
    
    private void closeQuietly(Closeable... closeables) {
        try {
            if (closeables != null) {
                for (Closeable closeable : closeables) {
                    if (closeable != null) {
                        closeable.close();
                    }
                }
            }
        } catch (IOException ignore) {
        }
    }
    
    private void updateProgress() {
        if (info.completionLength > 0 && info.contentLength > 0 && listener != null) {
            listener.onProgress(info);
        }
    }
    
    public void dispose(boolean cancel) {
        AndroidSchedulers.mainThread().scheduleDirect(() -> {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
            if (info.state == TaskInfo.State.ONGOING || info.state == TaskInfo.State.START) {
                if (cancel) {
                    info.state = TaskInfo.State.CANCEL;
                    //如果取消，删除临时文件
                    info.getTemporaryFile().delete();
                } else {
                    info.state = TaskInfo.State.PAUSE;
                }
                if (listener != null) {
                    listener.onStateChange(info, null);
                }
            }
        });
    }
}
