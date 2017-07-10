package cn.edu.nuc.androidlab.fileshare.util;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import java.util.List;

/**
 * WifiUtil
 * Created by MurphySL on 2017/7/3.
 */

public class WifiUtil {

    private static WifiUtil wifiUtil;
    private Context context;
    private WifiManager wifiManager;
    private WifiConfiguration wifiConfiguration;
    private WifiInfo wifiInfo;

    public static WifiUtil getInstance(Context context){
        if(wifiUtil == null){
            synchronized (WifiUtil.class){
                if(wifiUtil == null){
                    wifiUtil = new WifiUtil(context);
                }
            }
        }
        return wifiUtil;
    }


    private WifiUtil(Context context){
        this.context = context;

        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * open Wi-Fi
     */
    public boolean openWiFi(){
        if(!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);

        return wifiManager.isWifiEnabled();
    }

    /**
     * close Wi-Fi
     */
    public boolean closeWiFi(){
        if(wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(false);
        return !wifiManager.isWifiEnabled();
    }

    /**
     * Wi-Fi enabled
     */
    public boolean isWiFiEnabled(){
        return wifiManager.isWifiEnabled();
    }

    /**
     * get access point list
     */
    public List<ScanResult> getScanResult(){
        List<ScanResult> scanResults = null;
        if(wifiManager != null){
            scanResults = wifiManager.getScanResults();
        }
        return scanResults;
    }

    /**
     * get configured Wi-Fi network
     */
    public List<WifiConfiguration> getWiFiConfiguredNetworks(){
        return wifiManager.getConfiguredNetworks();
    }

    /**
     * get connection info
     * @return
     */
    public WifiInfo getConnectionInfo(){
        wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo;
    }


}