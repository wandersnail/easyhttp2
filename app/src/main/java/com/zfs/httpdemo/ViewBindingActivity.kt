package com.zfs.httpdemo

import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding

/**
 *
 *
 * date: 2020/3/29 09:46
 * author: zengfansheng
 */
@Suppress("UNCHECKED_CAST")
abstract class ViewBindingActivity<VB : ViewBinding> : BaseActivity(), ViewBindingClassProvider<VB> {
    protected lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewBindingClass().getMethod("inflate", LayoutInflater::class.java).invoke(null, layoutInflater) as VB
        setContentView(binding.root)
    }
}