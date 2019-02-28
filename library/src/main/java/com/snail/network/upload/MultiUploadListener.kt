package com.snail.network.upload

import com.snail.network.callback.MultiProgressListener

/**
 *
 *
 * date: 2019/2/28 13:41
 * author: zengfansheng
 */
interface MultiUploadListener<K, T : UploadInfo<K>> : UploadListener<K, T>, MultiProgressListener