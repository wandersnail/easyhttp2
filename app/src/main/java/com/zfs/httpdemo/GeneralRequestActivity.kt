package com.zfs.httpdemo

import android.os.Bundle
import android.util.Log
import cn.wandersnail.http.Configuration
import cn.wandersnail.http.EasyHttp
import cn.wandersnail.http.callback.RequestCallback
import cn.wandersnail.http.converter.JsonResponseConverter
import cn.wandersnail.http.converter.StringResponseConverter
import kotlinx.android.synthetic.main.activity_general_request.*
import okhttp3.Response
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
            val config = Configuration()
            config.callTimeout = 4
            EasyHttp.get(config, "http://192.168.137.1:8080/testapi?username=get&password=123456", object : RequestCallback<ResponseBody> {
                override fun onSuccess(response: Response, convertedBody: ResponseBody?) {
                    val resp = convertedBody?.string()
                    Log.e("get", "onNext: $resp")
                    tvResp.text = resp
                }

                override fun onError(t: Throwable) {
                    Log.e("get", "onError: ${t.message}")
                }
            })
        }
        btnPostText.setOnClickListener {
            EasyHttp.postText("http://192.168.137.1:8080/testapi", "Hello world!", StringResponseConverter(), object : RequestCallback<String> {
                override fun onSuccess(response: Response, convertedBody: String?) {
                    tvResp.text = convertedBody
                }

                override fun onError(t: Throwable) {
                    Log.e("btnPostText", "onError: ${t.message}")
                }
            })          
        }
        btnPostJson.setOnClickListener {
            EasyHttp.postJson("http://192.168.137.1:8080/testapi", "{\"msg\":\"Hello world!\"}", StringResponseConverter(), object : RequestCallback<String> {

                override fun onSuccess(response: Response, convertedBody: String?) {
                    tvResp.text = convertedBody
                }

                override fun onError(t: Throwable) {
                    Log.e("btnPostText", "onError: ${t.message}")
                }
            })
        }
        btnPostForm.setOnClickListener {
            val map = HashMap<String, Any>()
            map["postForm"] = "laomao"
            map["password"] = 123456
            EasyHttp.postForm("http://192.168.137.1:8080/testapi", map, JsonResponseConverter(BaseResp::class.java), object :
                RequestCallback<BaseResp> {

                override fun onSuccess(response: Response, convertedBody: BaseResp?) {
                    Log.e("postForm", "onNext: code = ${convertedBody?.code}, msg = ${convertedBody?.msg}")
                    tvResp.text = "code = ${convertedBody?.code}, msg = ${convertedBody?.msg}"
                }

                override fun onError(t: Throwable) {
                    Log.e("postForm", "onError: ${t.message}")
                }
            })
        }
    }
}