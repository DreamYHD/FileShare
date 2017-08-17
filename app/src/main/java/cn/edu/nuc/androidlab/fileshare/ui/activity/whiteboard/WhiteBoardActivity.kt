package cn.edu.nuc.androidlab.fileshare.ui.activity.whiteboard

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import cn.edu.nuc.androidlab.fileshare.MyApplication
import cn.edu.nuc.androidlab.fileshare.R
import cn.edu.nuc.androidlab.fileshare.bean.FileInfo
import cn.edu.nuc.androidlab.fileshare.bean.IpPortInfo
import cn.edu.nuc.androidlab.fileshare.core.ReceiveTransfer
import cn.edu.nuc.androidlab.fileshare.core.SendTransfer
import cn.edu.nuc.androidlab.fileshare.core.WhiteboardSendTransfer
import cn.edu.nuc.androidlab.fileshare.util.ApUtil
import cn.edu.nuc.androidlab.fileshare.util.Config
import cn.edu.nuc.androidlab.fileshare.util.WifiUtil
import com.tbruyelle.rxpermissions2.RxPermissions
import com.yinghe.whiteboardlib.fragment.WhiteBoardFragment
import kotlinx.android.synthetic.main.activity_receiver_file.*
import kotlinx.android.synthetic.main.activity_white_board.*
import java.io.File
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.ServerSocket
import kotlin.coroutines.experimental.EmptyCoroutineContext.plus

/**
 * Created by MurphySL on 2017/8/8.
 */
class WhiteBoardActivity : AppCompatActivity(){
    private val TAG: String = this.javaClass.simpleName

    private lateinit var sendBtnCallback : WhiteBoardFragment.SendBtnCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_white_board)

        requestPermission()
    }

    private fun requestPermission() {
        val rxPermission = RxPermissions(this)
        rxPermission.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe {
                    granted ->
                    if(granted){
                        initView()
                    }else{
                        Snackbar.make(recycler_view, "没有权限", Snackbar.LENGTH_LONG).show()
                    }
                }
    }

    private fun createSendServer(filePath : File) : Runnable = Runnable {
        startSendServer(filePath)
    }

    private fun startSendServer(filePath: File){
        val ip = WifiUtil.getInstance(this).ipAddressFromHotspot
        val fileSender : WhiteboardSendTransfer = WhiteboardSendTransfer(SendFileActivity@this, filePath, ip, Config.DEFAULT_SERVER_PORT)
        fileSender.setOnSendListener(object : WhiteboardSendTransfer.OnSendListener{
            override fun onStart() {
                Log.i(TAG, "START")
            }

            override fun onProgress(progress: Long, total: Long) {
                Log.i(TAG, "Progress:$progress $total")
            }

            override fun onSuccess(filPath: String) {
                Log.i(TAG, "send success : $filPath")
            }

            override fun onFailure(t: Throwable, fileInfo: FileInfo) {
                Log.i(TAG, "send fail : $t ${fileInfo.name}")
            }

        })

        MyApplication.instance.mainExecutor.execute(fileSender)
    }

    private fun initView() {
        val ts = supportFragmentManager.beginTransaction()
        sendBtnCallback = WhiteBoardFragment.SendBtnCallback {
            filePath ->  MyApplication.instance.mainExecutor.execute(createSendServer(filePath))
        }

        val whiteBoard2 : WhiteBoardFragment = WhiteBoardFragment.newInstance(sendBtnCallback)

        ts.add(R.id.content1, whiteBoard2, "wb2").commit()
    }
}