package cn.wandersnail.http;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.wandersnail.http.download.DownloadWorkerBuilder;
import cn.wandersnail.http.download.MultiDownloadWorkerBuilder;
import cn.wandersnail.http.upload.SyncUploadWorkerBuilder;
import cn.wandersnail.http.upload.UploadWorkerBuilder;

/**
 * http网络请求，包含普通的get和post、上传、下载
 * <p>
 * date: 2019/8/23 21:08
 * author: zengfansheng
 */
public class EasyHttp {
    static final ExecutorService executorService = Executors.newCachedThreadPool();    
    private Gson gson;
    private final GlobalConfiguration globalConfiguration = new GlobalConfiguration();

    private static final class InstanceHolder {
        static final EasyHttp instance = new EasyHttp();
    }

    public static EasyHttp getInstance() {
        return InstanceHolder.instance;
    }

    public static void executeOnIo(@NonNull Runnable runnable) {
        executorService.execute(runnable);
    }

    @NonNull
    public Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    @NonNull
    public GlobalConfiguration getGlobalConfiguration() {
        return globalConfiguration;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }
    
    /**
     * 单文件下载
     */
    public static DownloadWorkerBuilder singleDownloadWorkerBuilder() {
        return new DownloadWorkerBuilder();
    }

    /**
     * 多文件下载
     */
    public static MultiDownloadWorkerBuilder multiDownloadWorkerBuilder() {
        return new MultiDownloadWorkerBuilder();
    }

    /**
     * 异步上传
     */
    public static <T> UploadWorkerBuilder<T> uploadWorkerBuilder() {
        return new UploadWorkerBuilder<>();
    }
    
    /**
     * 异步上传
     */
    public static <T> UploadWorkerBuilder<T> uploadWorkerBuilder(Class<T> cls) {
        return new UploadWorkerBuilder<>();
    }

    /**
     * 同步上传
     */
    public static <T> SyncUploadWorkerBuilder<T> syncUploadWorkerBuilder() {
        return new SyncUploadWorkerBuilder<>();
    }

    /**
     * 同步上传
     */
    public static <T> SyncUploadWorkerBuilder<T> syncUploadWorkerBuilder(Class<T> cls) {
        return new SyncUploadWorkerBuilder<>();
    }

    /**
     * GET请求器
     */
    public static <T> GetRequester<T> getRequester() {
        return new GetRequester<>();
    }

    /**
     * GET请求器
     */
    public static <T> GetRequester<T> getRequester(Class<T> cls) {
        return new GetRequester<>();
    }

    /**
     * POST请求器
     */
    public static <T> PostRequester<T> postRequester() {
        return new PostRequester<>();
    }

    /**
     * POST请求器
     */
    public static <T> PostRequester<T> postRequester(Class<T> cls) {
        return new PostRequester<>();
    }

    /**
     * DELETE请求
     */
    public static <T> DeleteRequester<T> deleteRequester() {
        return new DeleteRequester<>();
    }

    /**
     * DELETE请求
     */
    public static <T> DeleteRequester<T> deleteRequester(Class<T> cls) {
        return new DeleteRequester<>();
    }
}
