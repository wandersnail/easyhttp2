package com.zfs.httpdemo

import android.app.Application
import android.os.Environment
import com.snail.commons.AppHolder
import com.snail.commons.entity.CrashHandler
import java.io.File

/**
 * 描述:
 * 时间: 2018/12/5 10:12
 * 作者: zengfansheng
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppHolder.init(this)
        CrashHandler.instance.init(this, File(Environment.getExternalStorageDirectory(), "network-requester/crash.txt"), null)
    }
}