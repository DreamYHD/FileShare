package cn.edu.nuc.androidlab.fileshare.ui.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.net.wifi.ScanResult
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import cn.edu.nuc.androidlab.fileshare.MyApplication
import cn.edu.nuc.androidlab.fileshare.R
import cn.edu.nuc.androidlab.fileshare.util.Config
import cn.edu.nuc.androidlab.fileshare.util.NetworkUtil
import cn.edu.nuc.androidlab.fileshare.util.WifiUtil
import com.bigkoo.pickerview.OptionsPickerView
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_choose_receiver.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * 选择接收者
 * WA : WIFI 变更
 * options 泛型
 *
 * Created by MurphySL on 2017/8/7.
 */
class ChooseReceiverActivity : AppCompatActivity(){
    private val TAG : String = this.javaClass.simpleName

    private val receivers = HashMap<String, ScanResult>()

    private val REQUEST_WRITE_SETTINGS_CODE = 0x00

    private var datagramSocket : DatagramSocket? = null

    private lateinit var options : OptionsPickerView<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_receiver)

        initView()

        requestPermissions()

    }

    override fun onDestroy() {
        super.onDestroy()
        closeSocket()
    }

    private fun initView() {
        val build = OptionsPickerView.Builder(this, OptionsPickerView.OnOptionsSelectListener {
            options1, _, _, _ ->
            val ssid = receivers.keys.toList()[options1]
            receiver.text = ssid
        })
        options  = OptionsPickerView<String>(build)

        receiver.setOnClickListener {
            updateScanResults()
        }

        next.setOnClickListener {
            if(receiver.text.isNotEmpty()){
                val ssid = receiver.text.toString()
                val status =
                        WifiUtil.getInstance(ChooseReceiverActivity@this)
                        .connectWifi(WifiUtil.getInstance(ChooseReceiverActivity@this).createWifiCfg(ssid, null, WifiUtil.WIFICIPHER_NOPASS))
                if(status){
                    // 通知接收方准备
                    val task = createClientRunnable(WifiUtil.getInstance(this).ipAddressFromHotspot)
                    MyApplication.instance.mainExecutor.execute(task)
                }else{
                    Log.i(TAG, "连接失败")
                    Snackbar.make(next, "连接失败", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun createClientRunnable(ip : String) : Runnable =
    Runnable {
        startSenderServer(ip, Config.DEFAULT_SERVER_PORT)
    }

    private fun startSenderServer(ip : String, port : Int){

        if(!WifiUtil.getInstance(this).isWiFiEnabled){
            WifiUtil.getInstance(this).openWiFi()
        }

        var time = 0
        var receiverIp = ip

        //确保获取 IP 地址
        while(receiverIp == Config.DEFAULT_UNKNOW_IP && time < Config.DEFAULT_TRY_TIME){
            Thread.sleep(1000)
            receiverIp = WifiUtil.getInstance(this).ipAddressFromHotspot
            Log.i(TAG, "IP address : $receiverIp")
            time ++
        }
        if(time == Config.DEFAULT_TRY_TIME && receiverIp == Config.DEFAULT_UNKNOW_IP){
            Snackbar.make(next, "无法获取正确 IP 地址", Snackbar.LENGTH_LONG).show()
            Log.i(TAG, "无法获取正确 IP 地址")
            return
        }
        Log.i(TAG, "接收方 IP 地址：$receiverIp")

        //验证是否可连接
        time = 0
        while(!NetworkUtil.pingIpAddress(receiverIp) && time < Config.DEFAULT_TRY_TIME){
            Thread.sleep(1000)
            Log.i(TAG, "ping --> $time")
            time ++
        }
        if(time == Config.DEFAULT_TRY_TIME){
            Snackbar.make(next, "无法连接", Snackbar.LENGTH_LONG).show()
            Log.i(TAG, "无法连接")
            return
        }
        Log.i(TAG, "接收方地址可联通")

        datagramSocket = DatagramSocket(port)
        val receiverData = ByteArray(1024)
        val ipAddress = InetAddress.getByName(receiverIp)

        //发送文件
        sendFileInfo(ipAddress, port)

        //发送初始化信息
        val sendData = Config.MSG_SEND_INIT.toByteArray(Charsets.UTF_8)
        val sendPacket = DatagramPacket(sendData, sendData.size, ipAddress, port)
        try {
            datagramSocket!!.send(sendPacket)
            Log.i(TAG, "sendFileInfoInit success")
        }catch (e : Exception){
            Log.i(TAG, "sendFileInfoInit fail")
            e.printStackTrace()
        }

        //接收接收方初始化反馈
        while(true){
            val receiverPacket = DatagramPacket(receiverData, receiverData.size, ipAddress, port)
            datagramSocket!!.receive(receiverPacket)
            val response = String(receiverPacket.data, Charsets.UTF_8).trim()

            if(response.isNotEmpty() && response == Config.MSG_RECEIVER_INIT_SUCCESS){
                Log.i(TAG, "get msg from receiver : $response")
                // 通知即将发送

                startActivity(Intent(ChooseReceiverActivity@this, SendFileActivity::class.java))
            }
        }

    }

    /**
     * 发送文件列表
     */
    private fun sendFileInfo(ipAddress : InetAddress, port: Int) {
        val fileInfoList = MyApplication.instance.fileInfoMap.values.toList()
        fileInfoList.forEach {
            val fileInfoJson = it.toJsonString()
            Log.i(TAG, fileInfoJson)
            Log.i(TAG, "${fileInfoJson.toByteArray().size}")

            val sendPacket : DatagramPacket = DatagramPacket(fileInfoJson.toByteArray(), fileInfoJson.toByteArray().size, ipAddress, port)
            try {
                datagramSocket!!.send(sendPacket)
                Log.i(TAG, "sendFileInfo success: ${it.name}")
            }catch (e : Exception){
                Log.i(TAG, "sendFileInfo fail: ${it.name}")
                e.printStackTrace()
            }
        }
    }

    private fun requestPermissions() {
        val rxPermissions = RxPermissions(this)
        //需要定位权限
        rxPermissions.request(
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe {
                    granted ->
                    if(granted){
                        WifiUtil.getInstance(this).openWiFi()
                        updateScanResults()
                    }else{
                        Log.i(TAG, "获取权限失败")
                        Snackbar.make(next, "没有权限", Snackbar.LENGTH_LONG).show()
                    }
                }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_WRITE_SETTINGS_CODE && Settings.System.canWrite(this)){
            requestPermissions()
        }
    }

    // WA : wifi 更改
    private fun updateScanResults() {

        WifiUtil.getInstance(this).scanResultWithNoPassword.forEach {
            Log.i(TAG, "${it.SSID} ${it.capabilities}")
            receivers.put(it.SSID, it)
        }

        options.setPicker(receivers.keys.toList())
        options.show()

        //handle.sendMessageDelayed(handle.obtainMessage(MSG_UPDATE_SCAN_RESULT), 1000)
    }

    private fun closeSocket()  =
            datagramSocket?.let {
                it.disconnect()
                it.close()
            }
}