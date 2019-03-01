package com.zfs.httpdemo

import android.os.Bundle
import android.util.Log
import com.snail.network.NetworkRequester
import com.snail.network.converter.JsonResponseConverter
import com.snail.network.converter.StringResponseConverter
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_general_request.*
import okhttp3.ResponseBody

/**
 *
 *
 * date: 2019/2/25 22:40
 * author: zengfansheng
 */
class GeneralRequestActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_general_request)
        btnGet.setOnClickListener { 
            NetworkRequester.get("http://192.168.137.1:8080/testapi?username=get&password=123456", object : Observer<ResponseBody> {
                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(t: ResponseBody) {
                    val resp = t.string()
                    Log.e("get", "onNext: $resp")
                    tvResp.text = resp
                }

                override fun onError(e: Throwable) {
                    Log.e("get", "onError: ${e.message}")
                }
            })
        }
        btnPostText.setOnClickListener {
            NetworkRequester.postText("http://192.168.137.1:8080/testapi", "Hello world!", StringResponseConverter(), object : Observer<String> {
                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(t: String) {
                    tvResp.text = t
                }

                override fun onError(e: Throwable) {
                    Log.e("btnPostText", "onError: ${e.message}")
                }
            })          
        }
        btnPostJson.setOnClickListener {
            NetworkRequester.postJson("http://192.168.137.1:8080/testapi", "{\"msg\":\"Hello world!\"}", StringResponseConverter(), object : Observer<String> {
                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(t: String) {
                    tvResp.text = t
                }

                override fun onError(e: Throwable) {
                    Log.e("btnPostText", "onError: ${e.message}")
                }
            })
        }
        btnPostForm.setOnClickListener {
            val map = HashMap<String, Any>()
            map["postForm"] = "laomao"
            map["password"] = 123456
            NetworkRequester.postForm("http://192.168.137.1:8080/testapi", map, JsonResponseConverter(BaseResp::class.java), object : Observer<BaseResp> {
                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {                    
                }

                override fun onNext(t: BaseResp) {
                    Log.e("postForm", "onNext: code = ${t.code}, msg = ${t.msg}")
                    tvResp.text = "code = ${t.code}, msg = ${t.msg}"
                }

                override fun onError(e: Throwable) {
                    Log.e("postForm", "onError: ${e.message}")
                }
            })
        }
    }
}