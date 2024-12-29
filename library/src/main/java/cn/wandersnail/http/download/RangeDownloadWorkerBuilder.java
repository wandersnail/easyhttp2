package cn.wandersnail.http.download;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by 曾繁盛 on 2024/12/28 22:54
 */
public class RangeDownloadWorkerBuilder {
    private static final int UPDATE_LIMIT_DURATION = 500;//限制进度更新频率，毫秒
    private DownloadInfo info;
    private DownloadListener<DownloadInfo> listener;
    private int blockSize = 1024 * 1024;
    private final Map<String, String> headers = new HashMap<>();
    private String rangeHeaderName;

    /**
     * 设置下载文件信息
     *
     * @param url      下载地址
     * @param savePath 文件保存路径
     */
    public RangeDownloadWorkerBuilder setFileInfo(@NonNull String url, @NonNull String savePath) {
        return setFileInfo(UUID.randomUUID().toString(), url, savePath);
    }

    /**
     * 设置下载文件信息
     *
     * @param tag      下载任务标识
     * @param url      下载地址
     * @param savePath 文件保存路径
     */
    public RangeDownloadWorkerBuilder setFileInfo(@NonNull String tag, @NonNull String url, @NonNull String savePath) {
        info = new DownloadInfo(tag, url, savePath);
        return this;
    }

    /**
     * 设置下载文件信息
     *
     * @param info 下载文件信息
     */
    public RangeDownloadWorkerBuilder setFileInfo(@NonNull DownloadInfo info) {
        this.info = info;
        return this;
    }

    /**
     * 设置下载进度监听
     */
    public RangeDownloadWorkerBuilder setListener(@NonNull DownloadListener<DownloadInfo> listener) {
        this.listener = listener;
        return this;
    }

    /**
     * 设置每次下载的分块大小，单位：字节，默认1MB
     */
    public RangeDownloadWorkerBuilder setBlockSize(@IntRange(from = 100) int blockSize) {
        if (blockSize < 100) {
            throw new IllegalArgumentException("block size must be greater than 100 bytes");
        }
        this.blockSize = blockSize;
        return this;
    }

    /**
     * 设置请求头
     */
    public RangeDownloadWorkerBuilder addHeader(@NonNull String key, @NonNull String value) {
        headers.put(key, value);
        return this;
    }

    /**
     * 设置自定义分段请求头
     */
    public RangeDownloadWorkerBuilder setRangeHeaderName(@NonNull String rangeHeaderName) {
        this.rangeHeaderName = rangeHeaderName;
        return this;
    }

    /**
     * 构建下载工作器
     */
    public RangeDownloadWorker<DownloadInfo> create() {
        return new RangeDownloadWorker<>(info, blockSize, headers, rangeHeaderName, listener);
    }

    /**
     * 开始下载
     */
    public RangeDownloadWorker<DownloadInfo> download() {
        RangeDownloadWorker<DownloadInfo> worker = new RangeDownloadWorker<>(info, blockSize, headers, rangeHeaderName, listener);
        worker.download();
        return worker;
    }
}
