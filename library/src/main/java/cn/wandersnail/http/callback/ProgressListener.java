package cn.wandersnail.http.callback;

/**
 * 下载进度监听
 * <p>
 * date: 2019/8/23 15:21
 * author: zengfansheng
 */
public interface ProgressListener {
    /**
     * 进度更新
     *
     * @param progress 已完成的大小
     * @param max      总大小
     */
    void onProgress(long progress, long max);
}
