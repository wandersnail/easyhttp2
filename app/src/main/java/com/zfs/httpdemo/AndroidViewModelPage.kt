package com.zfs.httpdemo

/**
 *
 *
 * date: 2020/3/29 12:29
 * author: zengfansheng
 */
interface AndroidViewModelPage<VM : BaseAndroidViewModel> {
    fun getViewModelClass(): Class<VM>
}