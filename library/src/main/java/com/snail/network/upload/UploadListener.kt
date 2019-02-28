package com.snail.network.upload

import com.snail.network.callback.TaskStateListener

/**
 *
 *
 * date: 2019/2/23 23:21
 * author: zengfansheng
 */
interface UploadListener<R, T : UploadInfo<R>> : TaskStateListener<T>