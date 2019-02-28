package com.zfs.httpdemo

import android.os.Bundle
import android.os.Environment
import android.util.Log
import com.snail.network.NetworkRequester
import com.snail.network.TaskInfo
import com.snail.network.callback.TaskListener
import com.snail.network.converter.StringResponseBodyConverter
import com.snail.network.upload.UploadInfo
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_general_request.*
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.io.File

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
            NetworkRequester.postText("http://192.168.137.1:8080/testapi", "Hello world!", StringResponseBodyConverter(), object : Observer<String> {
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
            NetworkRequester.postJson("http://192.168.137.1:8080/testapi", "{\"msg\":\"Hello world!\"}", StringResponseBodyConverter(), object : Observer<String> {
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
            NetworkRequester.postForm("http://192.168.137.1:8080/testapi", map, BaseResp::class.java, object : Observer<BaseResp> {
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
        btnUpload.setOnClickListener { 
            val args = HashMap<String, RequestBody>()
            args["upload"] = RequestBody.create(MediaType.parse("text/plain"), "Hello")
            val file = File(Environment.getExternalStorageDirectory(), "test.jpg")
            val url = "http://192.168.137.1:8080/testupload"
            val converter = StringResponseBodyConverter()
            val info = UploadInfo(url, file, converter, null, args)
            NetworkRequester.upload(info, object : TaskListener<UploadInfo<String>> {
                override fun onStateChange(info: UploadInfo<String>, t: Throwable?) {
                    t?.printStackTrace()
                    val log = when (info.state) {
                        TaskInfo.State.IDLE -> "闲置状态"
                        TaskInfo.State.START -> "开始上传"
                        TaskInfo.State.ERROR -> "上传错误, ${t?.message}"
                        TaskInfo.State.COMPLETED -> "上传成功"
                        TaskInfo.State.CANCEL -> "上传取消"
                        TaskInfo.State.PAUSE -> "上传暂停"
                        TaskInfo.State.ONGOING -> "上传中..."
                    }
                }

                override fun onProgress(info: UploadInfo<String>) {
                    
                }
            })
        }
    }
}