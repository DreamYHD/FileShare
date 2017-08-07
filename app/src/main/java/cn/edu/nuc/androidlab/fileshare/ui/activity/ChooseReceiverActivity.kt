package cn.edu.nuc.androidlab.fileshare.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.ScanResult
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import cn.edu.nuc.androidlab.fileshare.R
import cn.edu.nuc.androidlab.fileshare.bean.FileInfo
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
 * Created by MurphySL on 2017/8/7.
 */
class ChooseReceiverActivity : AppCompatActivity(){
    private val TAG : String = this.javaClass.simpleName

    private val receivers = HashMap<String, ScanResult>()

    private val REQUEST_CODE_WRITE_SETTINGS = 0x00

    private lateinit var datagramSocket : DatagramSocket

    private lateinit var fileInfos : HashMap<String, FileInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_receiver)

        fileInfos = intent.extras.getSerializable("fileInfos") as HashMap<String, FileInfo>

        getPermission()
    }

    private fun init() {

        val rxPermissions = RxPermissions(this)
        //需要定位权限。。。
        rxPermissions.request(
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe {
                    granted ->
                    if(granted){

                       /* ApUtil.configApState(this, "FileReceiver")
                        Log.i(TAG, ApUtil.isApEnabled(this).toString())*/

                        initData()

                        next.setOnClickListener {
                            createClientRunnable(WifiUtil.getInstance(this).ipAddressFromHotspot)
                        }
                    }else{
                        Log.i(TAG, "NO GRANTED")
                        Snackbar.make(next, "没有权限", Snackbar.LENGTH_LONG).show()
                    }
                }

    }

    private fun createClientRunnable(serverIP : String) : Runnable =
    Runnable {
        fileSendServer(serverIP, Config.DEFAULT_SERVER_PORT)
    }

    private fun fileSendServer(serverIP: String, port : Int) : Runnable{
        var time = 0
        var ipAddress = serverIP

        //确保获取 IP 地址
        while(ipAddress == Config.DEFAULT_UNKNOW_IP && time < Config.DEFAULT_TRY_TIME){
            Thread.sleep(500)
            ipAddress = WifiUtil.getInstance(this).ipAddressFromHotspot
            time ++
        }

        time = 0
        while(!NetworkUtil.pingIpAddress(ipAddress) && time < DEFAULT_BUFFER_SIZE){
            Thread.sleep(500)
            time ++
        }

        datagramSocket = DatagramSocket(port)
        val receiverData = ByteArray(1024)
        val address = InetAddress.getByName(ipAddress)

        //发送文件封装
        sendFileInfos(port, address)

        //
        val sendData = Config.MSG_SEND_INIT.toByteArray(Charsets.UTF_8)
        val sendPacket = DatagramPacket(sendData, sendData.size, address, port)
        datagramSocket.send(sendPacket)

        while(true){
            val receiverPacket = DatagramPacket(receiverData, receiverData.size, address, port)
            datagramSocket.receive(receiverPacket)
            val response = String(receiverPacket.data, Charsets.UTF_8).trim()

            if(response.isNotEmpty() && response == Config.MSG_RECEIVER_INIT_SUCCESS){

            }
        }

    }

    private fun sendFileInfos(port: Int, adress: InetAddress) {
        val fileInfoList = fileInfos.values.toList()
        fileInfoList.forEach {
            val fileInfoJson = it.toJosnString()
            val sendPacket : DatagramPacket = DatagramPacket(fileInfoJson.toByteArray(), fileInfoJson.toByteArray().size, adress, port)
            datagramSocket.send(sendPacket)
        }
    }

    private fun getPermission() {
        val granted : Boolean
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            granted = Settings.System.canWrite(this)
        }else{
            granted = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED)
        }

        if(granted){
            init()
        }else{
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:" + this.packageName)
                this.startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_SETTINGS), REQUEST_CODE_WRITE_SETTINGS)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_WRITE_SETTINGS && Settings.System.canWrite(this)){
            init()
        }
    }

    private fun initData() {
        WifiUtil.getInstance(this).scanResultWithNoPassword.forEach {
            Log.i(TAG, "${it.SSID} ${it.capabilities}")
            receivers.put(it.SSID, it)
        }
        val options  = OptionsPickerView.Builder(this, OptionsPickerView.OnOptionsSelectListener {
            options1, _, _, _ -> receiver.text = receivers.keys.toList()[options1]
        }).build()

        options.setPicker(receivers.keys.toList())
        options.show()
    }
}