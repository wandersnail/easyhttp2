package com.zfs.httpdemo

import androidx.viewbinding.ViewBinding

/**
 *
 *
 * date: 2020/3/29 12:29
 * author: zengfansheng
 */
interface ViewBindingClassProvider<VB: ViewBinding> {
    fun getViewBindingClass(): Class<VB>
}