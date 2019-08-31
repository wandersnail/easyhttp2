package cn.wandersnail.http.download;

import android.annotation.SuppressLint;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;

import cn.wandersnail.http.TaskInfo;

/**
 * 下载信息类，包含下载状态及进度监听
 * <p>
 * date: 2019/8/23 15:28
 * author: zengfansheng
 */
public class DownloadInfo extends TaskInfo {
    public final String savePath;
    long contentLength;//总长度
    long completionLength;//已完成长度
    private Application application;
    private final String tempFilename;

    /**
     * @param url      请求地址
     * @param savePath 文件保存路径
     */
    public DownloadInfo(@NonNull String url, @NonNull String savePath) {
        this(UUID.randomUUID().toString(), url, savePath);
    }

    /**
     * @param tag      唯一标识
     * @param url      请求地址
     * @param savePath 文件保存路径
     */
    public DownloadInfo(String tag, @NonNull String url, @NonNull String savePath) {
        super(tag, url);
        this.savePath = savePath;
        application = tryGetApplication();
        tempFilename = UUID.randomUUID().toString();
    }

    /**
     * 进度：已完成长度
     */
    public long getCompletionLength() {
        return completionLength;
    }

    /**
     * 进度：总长度
     */
    public long getContentLength() {
        return contentLength;
    }

    /**
     * 获取下载的临时文件，下载完成后再重命名
     */
    @NonNull
    public File getTemporaryFile() {     
        if (application != null) {
            return new File(application.getCacheDir(), tempFilename);
        } else {
            return new File(savePath + tempFilename);
        }
    }

    @SuppressLint("PrivateApi")
    @Nullable
    private Application tryGetApplication() {
        try {
            Class<?> cls = Class.forName("android.app.ActivityThread");
            Method catMethod = cls.getMethod("currentActivityThread");
            catMethod.setAccessible(true);
            Object aThread = catMethod.invoke(null);
            Method method = aThread.getClass().getMethod("getApplication");
            return (Application) method.invoke(aThread);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public void reset() {
        completionLength = 0;
        contentLength = 0;
        getTemporaryFile().delete();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DownloadInfo)) return false;
        DownloadInfo that = (DownloadInfo) o;
        return savePath.equals(that.savePath) && url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(savePath, url);
    }
}
