package cn.edu.nuc.androidlab.fileshare.ui.activity

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import cn.edu.nuc.androidlab.fileshare.MyApplication
import cn.edu.nuc.androidlab.fileshare.R
import cn.edu.nuc.androidlab.fileshare.bean.FileInfo
import cn.edu.nuc.androidlab.fileshare.bean.IpPortInfo
import cn.edu.nuc.androidlab.fileshare.core.ReceiveTransfer
import cn.edu.nuc.androidlab.fileshare.util.ApUtil
import cn.edu.nuc.androidlab.fileshare.util.Config
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder
import kotlinx.android.synthetic.main.activity_receiver_file.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.ServerSocket

/**
 * Created by MurphySL on 2017/8/7.
 */
class ReceiveFileActivity : AppCompatActivity(){
    private val TAG : String = this.javaClass.simpleName
    private val context = this

    private val fileInfoList = ArrayList<FileInfo>()
    private lateinit var ipPortInfo : IpPortInfo

    private var count = 0
    private var size = 0L
    private var time = System.currentTimeMillis()
    private lateinit var adapter : CommonAdapter<FileInfo>
    private var datagramSocket : DatagramSocket? = null
    private var serverSocket : ServerSocket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receiver_file)

        ipPortInfo = intent.extras.getSerializable(Config.KEY_IP_PORT_INFO) as IpPortInfo
        Log.i(TAG, ipPortInfo.toString())

        initView()
        requestPermission()

    }

    override fun onDestroy() {
        super.onDestroy()
        closeSocket()
        ApUtil.disableAp(context)
    }

    private fun closeSocket() {
        datagramSocket?.let {
            it.disconnect()
            it.close()
        }
        serverSocket?.close()
    }

    private fun initView() {
        recycler_view.layoutManager = LinearLayoutManager(this)
        adapter = object : CommonAdapter<FileInfo>(this, R.layout.item_send, fileInfoList){
            override fun convert(holder: ViewHolder?, t: FileInfo?, position: Int) {
                t?.let {
                    holder?.setText(R.id.name, t.name)
                    holder?.setText(R.id.size, "${t.size}")
                }
            }
        }
        recycler_view.adapter = adapter
    }

    private fun requestPermission() {
        val rxPermission = RxPermissions(this)
        rxPermission.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe {
                    granted ->
                    if(granted){
                        startReceiveServer()
                    }else{
                        Snackbar.make(recycler_view, "没有权限", Snackbar.LENGTH_LONG).show()
                    }
                }
    }

    private fun startReceiveServer() {
        val serverTask = Runnable {
            serverSocket = ServerSocket(Config.DEFAULT_SERVER_PORT)
            object : Thread(){
                override fun run() {
                    sendInitMsg(ipPortInfo)
                }
            }.start()
            while (true){
                val socket = serverSocket!!.accept()
                val fileReceiver = ReceiveTransfer(context, socket)
                fileReceiver.setOnReceiveListener(object : ReceiveTransfer.OnReceiveListener{
                    override fun onStart() {
                        size = 0
                        time = System.currentTimeMillis()
                    }

                    override fun onGetFileInfo(fileInfo: FileInfo) {
                        fileInfoList.add(fileInfo)
                        adapter.notifyDataSetChanged()
                    }

                    override fun onGetThumbnail(bitmap: Bitmap) {

                    }

                    override fun onProgress(progress: Long, total: Long) {

                    }

                    override fun onSuccess(fileInfo: FileInfo) {
                        count ++
                        Log.i(TAG, "SUCCESS")
                    }

                    override fun onFailure(t: Throwable, fileInfo: FileInfo?) {
                        count ++
                        Log.i(TAG, "FAIL")
                    }

                })
                MyApplication.instance.mainExecutor.execute(fileReceiver)
            }

        }
        Thread(serverTask).start()

    }


    private fun  sendInitMsg(ipPortInfo: IpPortInfo) {
        datagramSocket = DatagramSocket(ipPortInfo.port + 1) // 端口加一
        val sendData = Config.MSG_RECEIVER_INIT_SUCCESS.toByteArray(Charsets.UTF_8)
        val ipAddress = ipPortInfo.inetAddress

        val datagramPacket = DatagramPacket(sendData, sendData.size, ipAddress, ipPortInfo.port)
        datagramSocket?.send(datagramPacket)
        Log.i(TAG, "send msg to fileSender : ")
        datagramSocket?.close()
    }


}