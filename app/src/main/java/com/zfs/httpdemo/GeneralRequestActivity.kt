package com.zfs.httpdemo

import android.os.Bundle
import cn.wandersnail.commons.util.ToastUtils
import cn.wandersnail.http.Configuration
import cn.wandersnail.http.EasyHttp
import cn.wandersnail.http.callback.RequestCallback
import cn.wandersnail.http.converter.StringResponseConverter
import com.zfs.httpdemo.databinding.GeneralRequestActivityBinding
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response

/**
 * Created by 曾繁盛 on 2024/3/20 18:09
 */
class GeneralRequestActivity : ViewBindingActivity<GeneralRequestActivityBinding>() {
    override fun getViewBindingClass(): Class<GeneralRequestActivityBinding> {
        return GeneralRequestActivityBinding::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnGet.setOnClickListener {
            EasyHttp.getRequester(String::class.java)
                .setUrl("http://192.168.0.107:9991/user/info?userId=1")
                .setConverter(StringResponseConverter())
                .enqueue(object : RequestCallback<String> {
                    override fun onError(t: Throwable) {
                        ToastUtils.showShort("请求失败：${t.message}")
                    }

                    override fun onResponse(
                        response: Response<ResponseBody>,
                        successBody: String?,
                        errorBody: String?
                    ) {
                        binding.tvResp.text = successBody ?: errorBody
                    }
                })
        }
        binding.btnPostForm.setOnClickListener {
            EasyHttp.postRequester(String::class.java)
                .setUrl("http://192.168.0.107:9991/user/info")
                .setParams(mapOf(Pair("userId", "2")))
                .setConverter(StringResponseConverter())
                .enqueue(object : RequestCallback<String> {
                    override fun onError(t: Throwable) {
                        ToastUtils.showShort("请求失败：${t.message}")
                    }

                    override fun onResponse(
                        response: Response<ResponseBody>,
                        successBody: String?,
                        errorBody: String?
                    ) {
                        binding.tvResp.text = successBody ?: errorBody
                    }
                })
        }
        binding.btnPostJson.setOnClickListener {
            val config = Configuration()
            config.headers = HashMap<String, String>()
            config.headers["token"] = "glaskdjla"
            val jsonObject = JSONObject()
            jsonObject.put("username", "张三")
            jsonObject.put("password", "123456")
            EasyHttp.postRequester(String::class.java)
                .setUrl("http://192.168.0.107:9991/user/login")
                .setJsonBody(jsonObject.toString())
                .setConverter(StringResponseConverter())
                .setConfiguration(config)
                .enqueue(object : RequestCallback<String> {
                    override fun onError(t: Throwable) {
                        ToastUtils.showShort("请求失败：${t.message}")
                    }

                    override fun onResponse(
                        response: Response<ResponseBody>,
                        successBody: String?,
                        errorBody: String?
                    ) {
                        binding.tvResp.text = successBody ?: errorBody
                    }
                })
        }
        binding.btnDelete.setOnClickListener {
            EasyHttp.deleteRequester(String::class.java)
                .setUrl("http://192.168.0.107:9991/user/delete?id=2")
                .setConverter(StringResponseConverter())
                .enqueue(object : RequestCallback<String> {
                    override fun onError(t: Throwable) {
                        ToastUtils.showShort("请求失败：${t.message}")
                    }

                    override fun onResponse(
                        response: Response<ResponseBody>,
                        successBody: String?,
                        errorBody: String?
                    ) {
                        binding.tvResp.text = successBody ?: errorBody
                    }
                })
        }
    }
}