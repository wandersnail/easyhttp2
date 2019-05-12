package com.zfs.httpdemo

import android.os.Bundle
import android.util.Log
import com.snail.network.Configuration
import com.snail.network.NetworkRequester
import com.snail.network.callback.RequestCallback
import com.snail.network.converter.JsonResponseConverter
import com.snail.network.converter.StringResponseConverter
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
            NetworkRequester.get(config, "http://192.168.137.1:8080/testapi?username=get&password=123456", object : RequestCallback<ResponseBody> {
                override fun onSuccess(response: Response, parsedBody: ResponseBody) {
                    val resp = parsedBody.string()
                    Log.e("get", "onNext: $resp")
                    tvResp.text = resp
                }

                override fun onError(t: Throwable) {
                    Log.e("get", "onError: ${t.message}")
                }
            })
        }
        btnPostText.setOnClickListener {
            NetworkRequester.postText("http://192.168.137.1:8080/testapi", "Hello world!", StringResponseConverter(), object : RequestCallback<String> {
                override fun onSuccess(response: Response, parsedBody: String) {
                    tvResp.text = parsedBody
                }

                override fun onError(t: Throwable) {
                    Log.e("btnPostText", "onError: ${t.message}")
                }
            })          
        }
        btnPostJson.setOnClickListener {
            NetworkRequester.postJson("http://192.168.137.1:8080/testapi", "{\"msg\":\"Hello world!\"}", StringResponseConverter(), object : RequestCallback<String> {

                override fun onSuccess(response: Response, parsedBody: String) {
                    tvResp.text = parsedBody
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
            NetworkRequester.postForm("http://192.168.137.1:8080/testapi", map, JsonResponseConverter(BaseResp::class.java), object : RequestCallback<BaseResp> {

                override fun onSuccess(response: Response, parsedBody: BaseResp) {
                    Log.e("postForm", "onNext: code = ${parsedBody.code}, msg = ${parsedBody.msg}")
                    tvResp.text = "code = ${parsedBody.code}, msg = ${parsedBody.msg}"
                }

                override fun onError(t: Throwable) {
                    Log.e("postForm", "onError: ${t.message}")
                }
            })
        }
    }
}