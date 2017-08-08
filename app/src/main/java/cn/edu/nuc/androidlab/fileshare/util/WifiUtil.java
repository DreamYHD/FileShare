package cn.edu.nuc.androidlab.fileshare.util;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * WifiUtil
 * Created by MurphySL on 2017/7/3.
 */

public class WifiUtil {
    private final static String TAG = WifiUtil.class.getSimpleName();

    private static WifiUtil wifiUtil;
    private Context context;
    private WifiManager wifiManager;
    private WifiConfiguration wifiConfiguration;
    private WifiInfo wifiInfo;

    public static final int WIFICIPHER_NOPASS = 1;
    public static final int WIFICIPHER_WEP = 2;
    public static final int WIFICIPHER_WPA = 3;

    private String NO_PASSWORD = "[ESS]";
    private String NO_PASSWORD_WPS = "[WPS][ESS]";

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
     * connect Wi-Fi
     */
    public boolean connectWifi(WifiConfiguration wf){
        if(!disconnectWifi()){
            openWiFi();
        }
        int netId = wifiManager.addNetwork(wf);
        Log.i(TAG, netId + "");
        return wifiManager.enableNetwork(netId, true);
    }

    /**
     * disconnect Wi-Fi
     * @return false -> wifi is not enabled
     */
    public boolean disconnectWifi(){
        if(wifiManager != null && wifiManager.isWifiEnabled()){
            int netId = wifiManager.getConnectionInfo().getNetworkId();
            wifiManager.disableNetwork(netId);
            return wifiManager.disconnect();
        }
        return false;
    }

    public WifiConfiguration createWifiCfg(String ssid, String password, int type){
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();

        config.SSID = "\"" + ssid + "\"";

        if(type == WIFICIPHER_NOPASS){
//            config.wepKeys[0] = "";
//            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;

//            无密码连接WIFI时，连接不上wifi，需要注释两行代码
//            config.wepKeys[0] = "";
//            config.wepTxKeyIndex = 0;
        }else if(type == WIFICIPHER_WEP){
            config.hiddenSSID = true;
            config.wepKeys[0]= "\""+password+"\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }else if(type == WIFICIPHER_WPA){
            config.preSharedKey = "\""+password+"\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.status = WifiConfiguration.Status.ENABLED;
        }

        return config;
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

    public List<ScanResult> getScanResultWithNoPassword(){
        List<ScanResult> origin = getScanResult();
        List<ScanResult> results = new ArrayList<>();
        if(origin.size() > 0){
            for(ScanResult result : origin){
                if(result.capabilities.equals(NO_PASSWORD)
                        || result.capabilities.equals(NO_PASSWORD_WPS)){
                    results.add(result);
                }
            }
        }

        return results;
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

    public String getIpAddressFromHotspot(){
        String ipAddress = "192.168.43.1";
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        int address = dhcpInfo.gateway;
        ipAddress = ((address & 0xFF)
                + "." + ((address >> 8) & 0xFF)
                + "." + ((address >> 16) & 0xFF)
                + "." + ((address >> 24) & 0xFF));
        return ipAddress;
    }

    public String getLocalIpAddress(){
        // WifiAP ip address is hardcoded in Android.
        /* IP/netmask: 192.168.43.1/255.255.255.0 */
        String ipAddress = "192.168.43.1";
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        int address = dhcpInfo.serverAddress;
        ipAddress = ((address & 0xFF)
                + "." + ((address >> 8) & 0xFF)
                + "." + ((address >> 16) & 0xFF)
                + "." + ((address >> 24) & 0xFF));
        return ipAddress;
    }


}