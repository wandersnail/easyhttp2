package com.snail.network.callback

import com.snail.network.TaskInfo

/**
 *
 *
 * date: 2019/2/28 20:26
 * author: zengfansheng
 */
interface MultiTaskListener<T : TaskInfo> : TaskListener<T>, MultiProgressListener