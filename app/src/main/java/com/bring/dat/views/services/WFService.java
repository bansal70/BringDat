package com.bring.dat.views.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.bring.dat.R;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.Utils;
import com.zj.wfsdk.WifiCommunication;

public class WFService extends Service{

    public static WifiCommunication wfComm;
    private Context mContext;
    static boolean isConnected = true;
    private OnWifiConnectedListener onWifiConnectedListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        String address = BDPreferences.readString(mContext, Constants.KEY_IP_ADDRESS);

        wfComm = new WifiCommunication(mHandler);

        WifiManager mWifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);

        if (mWifiManager != null && !mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }

        wfComm.initSocket(address, Constants.WIFI_PORT);

        if (Utils.isServiceRunning(mContext, BTService.class)) {
            stopService(new Intent(mContext, BTService.class));
        }
    }

    /*@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = getApplicationContext();
        String address = BDPreferences.readString(mContext, Constants.KEY_IP_ADDRESS);

        wfComm = new WifiCommunication(mHandler);

        WifiManager mWifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);

        if (mWifiManager != null && !mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }

        wfComm.initSocket(address, Constants.WIFI_PORT);

        if (Utils.isServiceRunning(mContext, BTService.class)) {
            stopService(new Intent(mContext, BTService.class));
        }

        return Service.START_STICKY;
    }*/

    public static boolean isWifi() {
        return isConnected;
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WifiCommunication.WFPRINTER_CONNECTED:
                    if (Utils.isServiceRunning(mContext, BTService.class)) {
                        stopService(new Intent(mContext, BTService.class));
                    }
                    isConnected = true;
                    if (onWifiConnectedListener != null) {
                        onWifiConnectedListener.onWifiConnected();
                    }
                    BDPreferences.putString(mContext, Constants.LAST_PRINTER_CONNECTED, Constants.PRINTER_WIFI);
                    Utils.showToast(mContext, getString(R.string.success_printer_connected));
                    break;

                case WifiCommunication.WFPRINTER_DISCONNECTED:
                    isConnected = false;
                    Utils.showToast(mContext, getString(R.string.error_printer_disconnected));
                    break;

                case WifiCommunication.SEND_FAILED:
                    isConnected = false;
                    Utils.showToast(mContext, getString(R.string.error_printing_failed));
                    reconnectPrinter();
                    break;

                case WifiCommunication.WFPRINTER_CONNECTEDERR:
                    isConnected = false;
                    reconnectPrinter();
                    Utils.showToast(mContext, getString(R.string.error_connection_fails));
                    break;

                case WifiCommunication.WFPRINTER_REVMSG:
                    isConnected = true;
                    Utils.showToast(mContext, getString(R.string.success_receipt_printed));
                    break;
            }
        }
    };

    private void reconnectPrinter() {
        WifiManager mWifiManager = (WifiManager)mContext.getApplicationContext().getSystemService(WIFI_SERVICE);

        if (mWifiManager != null && !mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }

        wfComm.close();
        String ipAddress = BDPreferences.readString(mContext, Constants.KEY_IP_ADDRESS);
        wfComm.initSocket(ipAddress, Constants.WIFI_PORT);
    }

    public interface OnWifiConnectedListener {
        void onWifiConnected();
    }

    public void setOnWifiConnectedListener(OnWifiConnectedListener onWifiConnectedListener) {
        this.onWifiConnectedListener = onWifiConnectedListener;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (wfComm != null)
            wfComm.close();
    }
}
