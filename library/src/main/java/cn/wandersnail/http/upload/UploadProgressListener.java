package cn.wandersnail.http.upload;

import androidx.annotation.NonNull;

/**
 * date: 2019/8/23 18:12
 * author: zengfansheng
 */
public interface UploadProgressListener {

    /**
     * 进度更新
     *
     * @param fileInfo 文件信息
     * @param progress 已完成的大小
     * @param max      总大小
     */
    void onProgress(@NonNull FileInfo fileInfo, long progress, long max);
}
