package com.zfs.httpdemo

/**
 *
 *
 * date: 2019/2/26 17:18
 * author: zengfansheng
 */
class BaseResp {
    var code = 0
    var msg = ""
    
    override fun toString(): String {
        return "BaseResp(code=$code, msg='$msg')"
    }
}