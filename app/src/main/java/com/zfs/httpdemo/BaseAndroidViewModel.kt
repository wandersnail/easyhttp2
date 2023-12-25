package com.zfs.httpdemo

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle

/**
 *
 *
 * date: 2019/12/9 10:44
 * author: zengfansheng
 */
abstract class BaseAndroidViewModel(application: Application) : AndroidViewModel(application), DefaultLifecycleObserver {    
    /**
     * 绑定页面生命周期
     */
    fun bindLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
    }
    
    fun getString(@StringRes resId: Int): String {
        return getApplication<Application>().getString(resId)
    }
    
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return getApplication<Application>().getString(resId, formatArgs)
    }
}