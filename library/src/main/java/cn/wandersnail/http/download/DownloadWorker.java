package cn.wandersnail.http.download;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.wandersnail.http.TaskInfo;
import cn.wandersnail.http.exception.RetryWhenException;
import cn.wandersnail.http.util.HttpUtils;
import cn.wandersnail.http.util.SchedulerUtils;
import io.reactivex.disposables.Disposable;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * date: 2019/8/23 16:17
 * author: zengfansheng
 */
public class DownloadWorker<T extends DownloadInfo> implements Disposable {
    private final Map<T, DownloadObserver<T>> taskMap = new ConcurrentHashMap<>();
    private DownloadListener<T> listener;
    private int totalTasks;
    private int successNum;
    private int failureNum;
    private boolean isCanceled;

    public DownloadWorker(T info, DownloadListener<T> listener) {
        this.listener = listener;
        totalTasks = 1;
        info.reset();
        execute(info);
    }

    public DownloadWorker(List<T> infos, MultiDownloadListener<T> listener) {
        this.listener = listener;
        totalTasks = infos.size();
        for (T info : infos) {
            info.reset();
            execute(info);            
        }
    }
    
    @SuppressWarnings("unchecked")
    private void execute(T info) {
        //如果listener为空，说明不需要监听，不为空则在本地监听后，再传出去
        DownloadObserver observer = new DownloadObserver(info, listener == null ? null : new LocalTaskListener());
        synchronized (this) {
            taskMap.put(info, observer);
        }
        OkHttpClient httpClient = HttpUtils.initHttpsClient(true, new OkHttpClient.Builder())
                .addInterceptor(new ProgressInterceptor(observer))
                .build();
        new Retrofit.Builder()
                .client(httpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(info.getBaseUrl())
                .build()
                .create(DownloadService.class)
                .download("bytes=" + info.completionLength + "-", info.url)//断点续传
                .retryWhen(new RetryWhenException())
                .map(responseBodyResponse -> {
                    writeToDisk(responseBodyResponse.body(), info.getTemporaryFile(), info);
                    return responseBodyResponse;
                })
                .compose(SchedulerUtils.applyGeneralObservableSchedulers())
                .subscribe(observer);
    }

    //写入文件在本地
    private void writeToDisk(ResponseBody body, File file, T info) {
        if (body == null) {
            return;
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        InputStream input = body.byteStream();
        long allLen = info.contentLength == 0 ? body.contentLength() : info.contentLength;
        RandomAccessFile accessFile = null;
        FileChannel channel = null;
        try {
            accessFile = new RandomAccessFile(file, "rwd");
            channel = accessFile.getChannel();
            MappedByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, info.completionLength, 
                    allLen - info.completionLength);
            byte[] buffer = new byte[10240];
            int len;
            while ((len = input.read(buffer)) != -1) {
                byteBuffer.put(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            HttpUtils.closeQuietly(channel, accessFile, input);
        }
    }
        
    private class LocalTaskListener implements DownloadListener<T> {
        @Override
        public void onStateChange(@NonNull T info, @Nullable Throwable t) {
            if (listener != null) {
                listener.onStateChange(info, t);
            }
            if (totalTasks > 1) {
                if (info.state == TaskInfo.State.COMPLETED) {
                    taskMap.remove(info);
                    successNum++;                    
                } else if (info.state == TaskInfo.State.CANCEL || info.state == TaskInfo.State.ERROR) {
                    taskMap.remove(info);
                    failureNum++;
                } else if (info.state != TaskInfo.State.START) {
                    return;
                }
                if (listener instanceof MultiDownloadListener) {
                    ((MultiDownloadListener) listener).onTotalProgress(successNum, failureNum, totalTasks);
                }
            }
        }

        @Override
        public void onProgress(@NonNull T info) {
            if (listener != null) {
                listener.onProgress(info);
            }
        }
    }

    /**
     * 取消所有下载
     */
    @Override
    public void dispose() {
        cancel();
    }

    /**
     * 是否已取消
     */
    @Override
    public boolean isDisposed() {
        return false;
    }

    /**
     * 取消单个下载
     */
    public synchronized void cancel(@NonNull T info) {
        DownloadObserver<T> observer = taskMap.remove(info);
        if (observer != null) {
            observer.dispose(true);
            if (taskMap.isEmpty()) {
                isCanceled = true;
            }
        }
    }

    /**
     * 取消所有下载
     */
    public synchronized void cancel() {
        if (!taskMap.isEmpty()) {
            for (DownloadObserver<T> observer : taskMap.values()) {
                observer.dispose(true);
            }
            taskMap.clear();
            isCanceled = true;
        }
    }

    /**
     * 进行中的下载任务数
     */
    public int remaining() {
        return taskMap.size();
    }

    /**
     * 任务是否正在进行
     */
    public boolean isOngoing() {
        return remaining() > 0;
    }

    /**
     * 恢复单个下载
     */
    public synchronized void resume(@NonNull T info) {
        if (info.state == TaskInfo.State.PAUSE) {
            execute(info);
        }
    }

    /**
     * 恢复所有下载
     */
    public synchronized void resume() {
        for (T t : taskMap.keySet()) {
            if (t.state == TaskInfo.State.PAUSE) {
                execute(t);
            }
        }
    }

    /**
     * 暂停单个下载
     */
    public synchronized void pause(@NonNull T info) {
        DownloadObserver<T> observer = taskMap.get(info);
        if (observer != null) {
            observer.dispose(false);
        }
    }

    /**
     * 暂停所有下载
     */
    public synchronized void pause() {
        for (DownloadObserver<T> observer : taskMap.values()) {
            observer.dispose(false);
        }
    }
}
