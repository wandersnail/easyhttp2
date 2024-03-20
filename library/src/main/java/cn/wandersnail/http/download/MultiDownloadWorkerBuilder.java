package cn.wandersnail.http.download;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * date: 2021/4/8 14:20
 * author: zengfansheng
 */
public class MultiDownloadWorkerBuilder {
    private final List<DownloadInfo> infoList = new ArrayList<>();
    private MultiDownloadListener<DownloadInfo> listener;

    /**
     * 添加下载文件信息
     *
     * @param url      下载地址
     * @param savePath 文件保存路径
     */
    public MultiDownloadWorkerBuilder addFileInfo(@NonNull String url, @NonNull String savePath) {
        return addFileInfo(UUID.randomUUID().toString(), url, savePath);
    }

    /**
     * 添加下载文件信息
     *
     * @param tag      下载任务标识
     * @param url      下载地址
     * @param savePath 文件保存路径
     */
    public MultiDownloadWorkerBuilder addFileInfo(@NonNull String tag, @NonNull String url, @NonNull String savePath) {
        return addFileInfo(new DownloadInfo(tag, url, savePath));
    }

    /**
     * 添加下载文件信息
     *
     * @param info 下载文件信息
     */
    public MultiDownloadWorkerBuilder addFileInfo(@NonNull DownloadInfo info) {
        if (!infoList.contains(info)) {
            infoList.add(info);
        }
        return this;
    }

    /**
     * 设置下载进度监听器
     */
    public MultiDownloadWorkerBuilder setListener(@NonNull MultiDownloadListener<DownloadInfo> listener) {
        this.listener = listener;
        return this;
    }

    /**
     * 开始下载
     */
    public DownloadWorker<DownloadInfo> buildAndDownload() {
        return new DownloadWorker<>(infoList, listener);
    }
}
