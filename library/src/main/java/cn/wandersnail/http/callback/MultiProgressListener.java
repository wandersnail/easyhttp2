package cn.wandersnail.http.callback;

/**
 * 多任务进度监听
 * <p>
 * date: 2019/8/23 15:25
 * author: zengfansheng
 */
public interface MultiProgressListener {
    /**
     * 总体任务进度
     *
     * @param successNum 成功个数
     * @param failureNum 失败个数，包含主动取消的
     * @param total      总任务数
     */
    void onTotalProgress(int successNum, int failureNum, int total);
}
