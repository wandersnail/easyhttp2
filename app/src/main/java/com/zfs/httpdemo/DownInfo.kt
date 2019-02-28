package com.zfs.httpdemo

import com.snail.network.download.DownloadInfo
import java.util.*

/**
 *
 *
 * date: 2019/2/24 15:10
 * author: zengfansheng
 */
class DownInfo @JvmOverloads constructor(url: String, savePath: String, tag: String = UUID.randomUUID().toString()) : DownloadInfo(url, savePath, tag)