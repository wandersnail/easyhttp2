package com.zfs.httpdemo

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider

/**
 *
 *
 * date: 2020/3/29 09:46
 * author: zengfansheng
 */
abstract class DataBindingActivity<VM : BaseAndroidViewModel, VB : ViewDataBinding> : ViewBindingActivity<VB>(),
    AndroidViewModelPage<VM> {
    protected lateinit var viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        val factory = ViewModelProvider.AndroidViewModelFactory(application)
        viewModel = ViewModelProvider(this, factory)[getViewModelClass()]
        viewModel.bindLifecycle(lifecycle)
    }
}