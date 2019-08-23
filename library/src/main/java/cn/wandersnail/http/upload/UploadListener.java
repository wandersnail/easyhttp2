package cn.wandersnail.http.upload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.wandersnail.http.TaskInfo;
import okhttp3.Response;

/**
 * date: 2019/8/23 18:08
 * author: zengfansheng
 */
public interface UploadListener<T> extends UploadProgressListener {
    /**
     * 任务上传状态改变
     */
    void onStateChange(@NonNull TaskInfo.State state, @Nullable Throwable t);

    /**
     * 响应结果
     *
     * @param response          原始响应
     * @param convertedResponse 经过设置的转换器转换后的结果
     */
    void onResponseBodyParse(@NonNull Response response, @Nullable T convertedResponse);
}
