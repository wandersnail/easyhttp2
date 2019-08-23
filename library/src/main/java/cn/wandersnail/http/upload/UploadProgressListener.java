package cn.wandersnail.http.upload;

/**
 * date: 2019/8/23 18:12
 * author: zengfansheng
 */
public interface UploadProgressListener {
    /**
     * 进度更新
     *
     * @param name     文件参数名。fileParts里的key
     * @param progress 已完成的大小
     * @param max      总大小
     */
    void onProgress(String name, long progress, long max);
}
