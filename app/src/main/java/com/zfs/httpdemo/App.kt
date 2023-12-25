package com.zfs.httpdemo

import android.app.Application
import android.os.Environment
import cn.wandersnail.commons.base.AppHolder
import cn.wandersnail.commons.helper.CrashHandler
import java.io.File

/**
 * 描述:
 * 时间: 2018/12/5 10:12
 * 作者: zengfansheng
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        AppHolder.initialize(this)
    }
}