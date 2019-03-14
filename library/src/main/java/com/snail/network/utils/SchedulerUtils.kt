package com.snail.network.utils

import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 *
 *
 * date: 2019/2/23 18:54
 * author: zengfansheng
 */
object SchedulerUtils {
    fun <T> applyGeneralObservableSchedulers(): ObservableTransformer<T, T> {
        return ObservableTransformer {
            it.subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }
    }
    
    fun <T> applyGeneralFlowableSchedulers(): FlowableTransformer<T, T> {
        return FlowableTransformer {
            it.subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }
    }
    
    fun <T> applyGeneralSingleSchedulers(): SingleTransformer<T, T> {
        return SingleTransformer { 
            it.subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun <T> applyGeneralMaybeSchedulers(): MaybeTransformer<T, T> {
        return MaybeTransformer {
            it.subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun applyGeneralCompletableSchedulers(): CompletableTransformer {
        return CompletableTransformer {
            it.subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        }
    }
}