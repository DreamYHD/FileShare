package cn.edu.nuc.androidlab.fileshare.ui.activity

import android.Manifest
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import cn.edu.nuc.androidlab.fileshare.MyApplication
import cn.edu.nuc.androidlab.fileshare.R
import cn.edu.nuc.androidlab.fileshare.bean.FileInfo
import cn.edu.nuc.androidlab.fileshare.core.SendTransfer
import cn.edu.nuc.androidlab.fileshare.util.Config
import cn.edu.nuc.androidlab.fileshare.util.FileUtil
import cn.edu.nuc.androidlab.fileshare.util.WifiUtil
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder
import kotlinx.android.synthetic.main.activity_send_file.*

/**
 * Created by MurphySL on 2017/8/8.
 */
class SendFileActivity : AppCompatActivity(){
    private val TAG : String = this.javaClass.simpleName

    private val fileInfoList = ArrayList<FileInfo>()
    private var sendSize = 0L
    private var startTime : Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_file)

        initData()
        initView()
        requestPermission()
    }

    private fun initData() {
        MyApplication.instance.fileInfoMap.values.forEach {
            fileInfoList.add(it)
        }
    }

    private fun initView() {
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = object : CommonAdapter<FileInfo>(this, R.layout.item_send, fileInfoList){
            override fun convert(holder: ViewHolder?, t: FileInfo?, position: Int) {
                t?.let {
                    holder?.setText(R.id.name, t.name)
                    holder?.setText(R.id.size, "${t.size}")
                }
            }
        }
    }

    private fun requestPermission() {
        val rxPermission = RxPermissions(this)
        rxPermission.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe {
                    granted ->
                    if(granted){
                        MyApplication.instance.mainExecutor.execute(createSendServer())
                    }else{
                        Snackbar.make(recycler_view, "没有权限", Snackbar.LENGTH_LONG).show()
                    }
                }
    }

    private fun createSendServer() : Runnable = Runnable {
        startSendServer()
    }

    private fun startSendServer(){
        val ip = WifiUtil.getInstance(this).ipAddressFromHotspot
        fileInfoList.forEach {
            val fileSender = SendTransfer(SendFileActivity@this, it, ip, Config.DEFAULT_SERVER_PORT)
            var filetotal = 0L
            fileSender.setOnSendListener(object : SendTransfer.OnSendListener{
                override fun onStart() {
                    startTime = System.currentTimeMillis()
                }

                override fun onProgress(progress: Long, total: Long) {
                    size.text = FileUtil.byte2MemorySize(progress + sendSize)
                    filetotal = total
                }

                override fun onSuccess(fileInfo: FileInfo) {
                    sendSize += filetotal
                    Log.i(TAG, "send success : ${fileInfo.name}")
                    Snackbar.make(recycler_view, "发送成功", Snackbar.LENGTH_LONG).show()
                }

                override fun onFailure(t: Throwable, fileInfo: FileInfo) {
                    Log.i(TAG, "send success : ${fileInfo.name}")
                }
            })

            MyApplication.instance.mainExecutor.execute(fileSender)
        }
        Log.i(TAG, "传输完成")
    }
}