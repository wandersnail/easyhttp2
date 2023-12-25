package com.zfs.httpdemo

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import cn.wandersnail.commons.helper.PermissionsRequester
import cn.wandersnail.commons.util.ToastUtils
import cn.wandersnail.widget.listview.BaseListAdapter
import cn.wandersnail.widget.listview.BaseViewHolder
import com.zfs.httpdemo.databinding.ListItemBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    private var requester: PermissionsRequester? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)        
        requester = PermissionsRequester(this)
        val list = ArrayList<String>()
        list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requester?.checkAndRequest(list)
        requester?.setCallback { refusedPermissions ->
            if (refusedPermissions.isNotEmpty()) {
                ToastUtils.showShort("部分权限被拒绝，可能造成某些功能无法使用")
            }
        }
        val items = mutableListOf("文件上传")
        val itemClassList = mutableListOf(UploadActivity::class.java)
        val adapter = object : BaseListAdapter<String>(this, items) {
            override fun createViewHolder(position: Int): BaseViewHolder<String> {
                return object : BaseViewHolder<String> {
                    private lateinit var itemBinding: ListItemBinding

                    override fun onBind(item: String, position: Int) {
                        itemBinding.tv.text = item
                    }

                    override fun createView(): View {
                        itemBinding = ListItemBinding.inflate(layoutInflater)
                        return itemBinding.root
                    }
                }
            }
        }
        val lv: ListView = findViewById(R.id.lv)
        lv.adapter = adapter
        lv.setOnItemClickListener { _, _, position, _ ->
            startActivity(Intent(this, itemClassList[position]))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        requester?.onActivityResult(requestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        requester?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
