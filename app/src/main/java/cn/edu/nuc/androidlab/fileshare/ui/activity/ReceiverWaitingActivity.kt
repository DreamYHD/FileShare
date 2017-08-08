package cn.edu.nuc.androidlab.fileshare.ui.activity

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import cn.edu.nuc.androidlab.fileshare.MyApplication
import cn.edu.nuc.androidlab.fileshare.R
import cn.edu.nuc.androidlab.fileshare.bean.FileInfo
import cn.edu.nuc.androidlab.fileshare.bean.IpPortInfo
import cn.edu.nuc.androidlab.fileshare.ui.receiver.WifiBroadcastReceiver
import cn.edu.nuc.androidlab.fileshare.util.ApUtil
import cn.edu.nuc.androidlab.fileshare.util.Config
import cn.edu.nuc.androidlab.fileshare.util.WifiUtil
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_receiver_waiting.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * 等待接收页面
 * WA接收数据刷新
 * MSG
 * Created by MurphySL on 2017/8/7.
 */
class ReceiverWaitingActivity : AppCompatActivity(){
    private val TAG : String = this.javaClass.simpleName

    private val REQUEST_WRITE_SETTINGS_CODE = 0x00

    private lateinit var datagramSocket : DatagramSocket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receiver_waiting)

        requestWriteSettingPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        closeSocket()
    }

    /**
     * 获取修改系统权限
     */
    private fun requestWriteSettingPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(!Settings.System.canWrite(this)){
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:" + this.packageName)
                this.startActivityForResult(intent, REQUEST_WRITE_SETTINGS_CODE)
            }else{
                requestPermissions()
            }
        }else{
            requestPermissions()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_WRITE_SETTINGS_CODE && Settings.System.canWrite(this)){
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        val rxPermission = RxPermissions(this)
        rxPermission.request(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_WIFI_STATE)
                .subscribe {
                    granted ->
                    if(granted){
                        WifiUtil.getInstance(ReceiverWaitingActivity@this).closeWiFi()

                        val ssid = android.os.Build.DEVICE
                        ApUtil.configApState(this, ssid)

                        val receiver = object : WifiBroadcastReceiver(){
                            override fun onWifiEnabled() {
                                Log.i(TAG, "开启 Hotspot 成功")
                                status.text = "创建成功"
                                val task = createSendMsgRunnable()
                                MyApplication.instance.mainExecutor.execute(task)
                            }
                        }

                        val filter = IntentFilter(ApUtil.ACTION_WIFI_AP_STATE_CHANGED)
                        registerReceiver(receiver, filter)

                    }else{
                        Snackbar.make(status, "没有授权", Snackbar.LENGTH_LONG).show()
                    }
                }
    }

    private fun createSendMsgRunnable() =
    Runnable {
        startReceiverServer(Config.DEFAULT_SERVER_PORT)
    }

    private fun confirm() : Boolean{
        var time = 0
        var localAddress = WifiUtil.getInstance(this).localIpAddress
        while(time < Config.DEFAULT_TRY_TIME && localAddress == Config.DEFAULT_UNKNOW_IP){
            Thread.sleep(1000)
            localAddress = WifiUtil.getInstance(this).localIpAddress
            time ++
        }
        if(time == Config.DEFAULT_TRY_TIME && localAddress == Config.DEFAULT_UNKNOW_IP){
            return false
        }
        Log.i(TAG, "ip address : $localAddress")
        return true
    }

    // WA接收数据刷新
    private fun startReceiverServer(port : Int) {

        if(!confirm()) return

        datagramSocket = DatagramSocket(port)
        var receiveData = ByteArray(1024)

        while(true){
            val datagramPacket = DatagramPacket(receiveData, receiveData.size)
            datagramSocket.receive(datagramPacket)
            Log.i(TAG, "${datagramPacket.data.size}")
            val msg : String? = String(datagramPacket.data).substringBeforeLast("}")
            msg?.let{
                val inetAddress : InetAddress = datagramPacket.address
                val senderPort : Int = datagramPacket.port
                if(msg.isNotEmpty() && msg.startsWith(Config.MSG_SEND_INIT)){

                    Log.i(TAG, "receive sender init msg success")

                    val intent = Intent()
                    intent.setClass(ReceiverWaitingActivity@this, ReceiveFileActivity::class.java)
                    val bundle = Bundle()
                    bundle.putSerializable(Config.KEY_IP_PORT_INFO, IpPortInfo(inetAddress, senderPort))
                    intent.putExtras(bundle)
                    startActivity(intent)
                }else{
                    // 文件列表
                    Log.i(TAG, "GET FileInfo")
                    parseFileInfo("$msg}")
                }
                receiveData = ByteArray(1024)
            }
        }
    }

    private fun closeSocket() {
        datagramSocket.let {
            it.disconnect()
            it.close()
        }
    }

    private fun parseFileInfo(msg: String) {
        Log.i(TAG, msg)
        val fileInfo = FileInfo.toFileInfo(msg)
        fileInfo?.let {
            Log.i(TAG, it.name)
            MyApplication.instance.addFileInfo(it)
        }

    }

}