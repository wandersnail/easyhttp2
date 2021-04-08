package cn.wandersnail.http.download;

import androidx.annotation.NonNull;

import java.util.UUID;

/**
 * date: 2021/4/8 14:20
 * author: zengfansheng
 */
public class DownloadWorkerBuilder {
    private DownloadInfo info;
    private DownloadListener<DownloadInfo> listener;

    /**
     * 设置下载文件信息
     *
     * @param url      下载地址
     * @param savePath 文件保存路径
     */
    public DownloadWorkerBuilder setFileInfo(@NonNull String url, @NonNull String savePath) {
        return setFileInfo(UUID.randomUUID().toString(), url, savePath);
    }

    /**
     * 设置下载文件信息
     *
     * @param tag      下载任务标识
     * @param url      下载地址
     * @param savePath 文件保存路径
     */
    public DownloadWorkerBuilder setFileInfo(@NonNull String tag, @NonNull String url, @NonNull String savePath) {
        info = new DownloadInfo(tag, url, savePath);
        return this;
    }

    /**
     * 设置下载进度监听
     */
    public DownloadWorkerBuilder setListener(DownloadListener<DownloadInfo> listener) {
        this.listener = listener;
        return this;
    }
    
    public DownloadWorker<DownloadInfo> build() {
        return new DownloadWorker<>(info, listener);
    }
}
