package cn.wandersnail.http.download;

import android.util.Log;

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

import cn.wandersnail.http.EasyHttp;
import cn.wandersnail.http.TaskInfo;
import cn.wandersnail.http.util.HttpUtils;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * date: 2019/8/23 16:17
 * author: zengfansheng
 */
public class DownloadWorker<T extends DownloadInfo> {
    private final Map<T, DownloadController<T>> taskMap = new ConcurrentHashMap<>();
    private final DownloadListener<T> listener;
    private int successNum;
    private int failureNum;
    private boolean isCanceled;
    private final int taskCount;

    public DownloadWorker(@NonNull T info, @Nullable DownloadListener<T> listener) {
        this.listener = listener;
        taskCount = 1;
        info.reset();
        execute(info);
    }

    public DownloadWorker(@NonNull List<T> infoList, @Nullable MultiDownloadListener<T> listener) {
        this.listener = listener;
        taskCount = infoList.size();
        for (T info : infoList) {
            info.reset();
            execute(info);
        }
    }
    
    private void execute(T info) {
        //如果listener为空，说明不需要监听，不为空则在本地监听后，再传出去
        DownloadController<T> controller = new DownloadController<>(info, listener == null ? null : new LocalTaskListener());
        synchronized (this) {
            taskMap.put(info, controller);
        }
        OkHttpClient httpClient = HttpUtils.initHttpsClient(true, new OkHttpClient.Builder())
                .addInterceptor(new ProgressInterceptor(controller))
                .build();
        Call<ResponseBody> call = new Retrofit.Builder()
                .client(httpClient)
                .baseUrl(info.getBaseUrl())
                .build()
                .create(DownloadService.class)
                .download("bytes=" + info.completionLength + "-", info.url);//断点续传
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                EasyHttp.executeOnIo(()-> writeToDisk(response.body(), info.getTemporaryFile(), info, controller));
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                controller.onError(t);
            }
        });
    }

    //写入文件在本地
    private void writeToDisk(ResponseBody body, File file, T info, DownloadController<T> controller) {
        if (body == null) {
            controller.onError(new Throwable("body is null"));
            return;
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        long allLen = info.contentLength == 0 ? body.contentLength() : info.contentLength;
        RandomAccessFile accessFile = null;
        FileChannel channel = null;
        InputStream input = null;
        try {
            long readLen = 0;
            input = body.byteStream();
            accessFile = new RandomAccessFile(file, "rwd");
            channel = accessFile.getChannel();
            MappedByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, info.completionLength, 
                    allLen - info.completionLength);
            byte[] buffer = new byte[10240];
            int len;
            controller.onStart();
            while (!controller.isCancel() && (len = input.read(buffer)) != -1) {
                byteBuffer.put(buffer, 0, len);
                readLen += len;
            }
            if (allLen <= readLen) {
                controller.onComplete();
            }
        } catch (Exception e) {
            Log.e("DownloadWorker", "文件保存失败：" + e.getMessage());
            controller.onError(e);
        } finally {
            HttpUtils.closeQuietly(channel, accessFile, input);
            if (controller.isCancel()) {
                file.delete();
            }
        }
    }
        
    private class LocalTaskListener implements DownloadListener<T> {
        @Override
        public void onStateChange(@NonNull T info, @Nullable Throwable t) {
            if (listener != null) {
                listener.onStateChange(info, t);
            }
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
                ((MultiDownloadListener<T>) listener).onTotalProgress(successNum, failureNum, taskCount);
            }
        }

        @Override
        public void onProgress(@NonNull T info, int progress) {
            if (listener != null) {
                listener.onProgress(info, progress);
            }
        }
    }


    /**
     * 取消单个下载
     */
    public synchronized void cancel(@NonNull T info) {
        DownloadController<T> controller = taskMap.remove(info);
        if (controller != null) {
            controller.cancel();
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
            for (DownloadController<T> controller : taskMap.values()) {
                controller.cancel();
            }
            taskMap.clear();
            isCanceled = true;
        }
    }

    /**
     * 是否已取消
     */
    public boolean isCanceled() {
        return isCanceled;
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
        DownloadController<T> controller = taskMap.get(info);
        if (controller != null) {
            controller.pause();
        }
    }

    /**
     * 暂停所有下载
     */
    public synchronized void pause() {
        for (DownloadController<T> controller : taskMap.values()) {
            controller.pause();
        }
    }
}
