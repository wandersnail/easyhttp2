package cn.wandersnail.http.upload;

import androidx.annotation.NonNull;

/**
 * Created by 曾繁盛 on 2024/3/20 10:34
 */
interface InternalUploadListener extends UploadProgressListener {
    void onComplete(@NonNull FileInfo fileInfo);
}
