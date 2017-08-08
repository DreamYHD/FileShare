package cn.edu.nuc.androidlab.fileshare.ui.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import cn.edu.nuc.androidlab.fileshare.util.ApUtil

/**
 * Created by MurphySL on 2017/8/8.
 */
abstract class WifiBroadcastReceiver : BroadcastReceiver(){

    override fun onReceive(p0: Context?, p1: Intent?) {
        p1?.let {
            if(it.action == ApUtil.ACTION_WIFI_AP_STATE_CHANGED){
                val state = it.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)
                if(WifiManager.WIFI_STATE_ENABLED == state % 10){
                    onWifiEnabled()
                }
            }
        }
    }

    abstract fun onWifiEnabled()

}