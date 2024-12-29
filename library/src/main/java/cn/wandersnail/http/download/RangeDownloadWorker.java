package cn.wandersnail.http.download;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;

import cn.wandersnail.http.EasyHttp;
import cn.wandersnail.http.TaskInfo;
import cn.wandersnail.http.util.HttpUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by 曾繁盛 on 2024/12/28 22:58
 */
public class RangeDownloadWorker<T extends DownloadInfo> {
    private static final int UPDATE_LIMIT_DURATION = 500;//限制进度更新频率，毫秒
    private final T info;
    private final DownloadListener<T> listener;
    private final int blockSize;
    private boolean isCancel;
    private boolean isStart;
    private long lastUpdateTime;//上次进度更新时间
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Map<String, String> headers;
    private final String rangeHeaderName;

    public RangeDownloadWorker(@NonNull T info, int blockSize, @NonNull Map<String, String> headers,
                               @Nullable String rangeHeaderName, @Nullable DownloadListener<T> listener) {
        this.info = info;
        this.listener = listener;
        this.headers = headers;
        this.rangeHeaderName = rangeHeaderName;
        this.blockSize = blockSize;
    }

    private static class HeaderInterceptor implements okhttp3.Interceptor {
        private final Map<String, String> headers;

        private HeaderInterceptor(@NonNull Map<String, String> headers) {
            this.headers = headers;
        }

        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request origin = chain.request();
            Request.Builder builder = origin.newBuilder();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
            builder.method(origin.method(), origin.body());
            return chain.proceed(builder.build());
        }
    }

    public void download() {
        synchronized (this) {
            if (isStart) {
                return;
            }
            isStart = true;
        }
        EasyHttp.executeOnIo(()-> {
            long readBytes = 0;
            while (!isCancel) {
                long endBytes = readBytes + blockSize - 1;
                if (info.contentLength > 0 && endBytes >= info.contentLength) {
                    endBytes = info.contentLength - 1;
                }
                String rangeValue = "bytes=" + readBytes + "-" + endBytes;
                OkHttpClient.Builder builder = HttpUtils.initHttpsClient(true, new OkHttpClient.Builder());
                if (!headers.isEmpty() || !TextUtils.isEmpty(rangeHeaderName)) {
                    if (!TextUtils.isEmpty(rangeHeaderName)) {
                        headers.put(rangeHeaderName, rangeValue);
                    }
                    builder.addInterceptor(new HeaderInterceptor(headers));
                }
                Call<ResponseBody> call = new Retrofit.Builder()
                        .client(builder.build())
                        .baseUrl(info.getBaseUrl())
                        .build()
                        .create(DownloadService.class)
                        .download(rangeValue, info.url);//分段下载
                try {
                    Response<ResponseBody> response = call.execute();
                    if (response.isSuccessful()) {
                        try (ResponseBody body = response.body()) {
                            if (body == null) {
                                onError(new Throwable("body is null"));
                                break;
                            }
                            String range = response.headers().get("Content-Range");
                            if (range == null) {//没有这个响应头说明不支持分段下载
                                //获取文件总大小
                                info.contentLength = body.contentLength();
                                if (info.contentLength > 0) {
                                    writeToDisk(body, readBytes, info.contentLength);
                                } else {
                                    onError(new Throwable("Content-Length is 0"));
                                }
                                break;
                            }
                            //获取文件总大小，格式：bytes start-end/total
                            try {
                                if (info.contentLength == 0) {
                                    info.contentLength = Long.parseLong(range.substring(range.lastIndexOf("/") + 1));
                                }
                                //写入文件
                                long length = body.contentLength();
                                if (length <= 0) {
                                    //解析开始结束位置
                                    String[] ranges = range.substring(range.indexOf("bytes ") + 6).split("-");
                                    long start = Long.parseLong(ranges[0]);
                                    long end = Long.parseLong(ranges[1]);
                                    length = end - start + 1;
                                }
                                writeToDisk(body, readBytes, length);
                                readBytes += length;
                            } catch (Exception e) {
                                onError(new Throwable("Content-Range format error"));
                                break;
                            }
                        }
                    } else {
                        onError(new Throwable("下载失败，HTTP状态码：" + response.code()));
                        break;
                    }
                } catch (IOException e) {
                    onError(e);
                    break;
                }
                if (readBytes >= info.contentLength) {
                    break;
                }
            }
        });

    }

    public void onReadBytes(long readBytes) {
        long completionLength = info.completionLength + readBytes;
        if (info.contentLength > 0 && completionLength > info.contentLength) {
            completionLength = info.contentLength;
        }
        info.completionLength = completionLength;
        mainHandler.post(() -> {
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

    //写入文件在本地
    private void writeToDisk(ResponseBody body, long start, long length) {
        File file = info.getTemporaryFile();
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        RandomAccessFile accessFile = null;
        FileChannel channel = null;
        InputStream input = null;
        try {
            input = body.byteStream();
            accessFile = new RandomAccessFile(file, "rwd");
            channel = accessFile.getChannel();
            MappedByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, start, length);
            byte[] buffer = new byte[10240];
            int len;
            if (start == 0) {
                onStart();
            }
            while (!isCancel && (len = input.read(buffer)) != -1) {
                byteBuffer.put(buffer, 0, len);
                onReadBytes(len);
            }
            if (info.contentLength <= info.completionLength) {
                onComplete();
            }
        } catch (Exception e) {
            Log.e("RangeDownloadWorker", "文件保存失败：" + e.getMessage());
            onError(e);
        } finally {
            HttpUtils.closeQuietly(channel, accessFile, input);
            if (isCancel) {
                info.getTemporaryFile().delete();
            }
        }
    }

    private void onStart() {
        info.state = TaskInfo.State.START;
        if (listener != null) {
            mainHandler.post(()-> listener.onStateChange(info, null));
        }
    }

    private void onError(Throwable e) {
        info.state = TaskInfo.State.ERROR;
        info.getTemporaryFile().delete();
        if (listener != null) {
            mainHandler.post(()-> listener.onStateChange(info, e));
        }
    }

    private void updateProgress() {
        if (info.completionLength > 0 && info.contentLength > 0 && listener != null) {
            listener.onProgress(info, (int) (info.completionLength * 100 / info.contentLength));
        }
    }

    private void onComplete() {
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
                mainHandler.post(() -> listener.onStateChange(info, null));
            }
        }
    }
}
