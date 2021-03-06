package com.bring.dat.views.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.bring.dat.R;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.Utils;

import java.lang.ref.WeakReference;

import timber.log.Timber;

public class BTService  extends Service {

    public static BluetoothService mService;
    BluetoothDevice con_dev = null;
    private Context mContext;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = getApplicationContext();

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        if (Utils.isServiceRunning(mContext, WFService.class)) {
            stopService(new Intent(mContext, WFService.class));
        }

        checkBT();

        return Service.START_STICKY;
    }

    public void checkBT() {
        mService = new BluetoothService(this, getHandler());

        if (!mService.isAvailable()) {
            Utils.showToast(mContext, getString(R.string.error_bluetooth_unavailable));
        }

        setBluetooth();
    }

    public void setBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (!isEnabled) {
            bluetoothAdapter.enable();
        } else {
            connectBT();
        }
    }

    public boolean isEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter.isEnabled();
    }

    private void connectBT() {
        String BTAddress = BDPreferences.readString(mContext, Constants.KEY_BT_ADDRESS);

        if (isEnabled()) {
            if (BTAddress.isEmpty()) {
                BTAddress = Constants.PRINTER_MAC_ADDRESS;
            }

            if (mService != null) {
                con_dev = mService.getDevByMac(BTAddress);
                mService.connect(con_dev);
            }
        }
    }

    /*@SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Utils.showToast(mContext, getString(R.string.success_connection));
                            break;
                        case BluetoothService.STATE_CONNECTING:
                       //     Timber.e("Bluetooth is connecting");
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                       //     Timber.e("Bluetooth state listen or none");
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST:
                    connectBT();
                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT:
                    connectBT();
                    break;
            }
        }
    };*/

    public static class IncomingHandler extends Handler {
        private final WeakReference<BTService> mService;

        private IncomingHandler(BTService service) {
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            BTService service = mService.get();
            if (service != null) {
                service.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case BluetoothService.MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                    case BluetoothService.STATE_CONNECTED:
                        Utils.showToast(mContext, getString(R.string.success_connection));
                        BDPreferences.putString(mContext, Constants.LAST_PRINTER_CONNECTED, Constants.PRINTER_BLUETOOTH);
                        break;
                    case BluetoothService.STATE_CONNECTING:
                        //     Timber.e("Bluetooth is connecting");
                        break;
                    case BluetoothService.STATE_LISTEN:
                             Timber.e("Bluetooth state listen ");
                    case BluetoothService.STATE_NONE:
                             Timber.e("Bluetooth state none ");
                        break;
                }
                break;
            case BluetoothService.MESSAGE_CONNECTION_LOST:
                //connectBT();
                break;
            case BluetoothService.MESSAGE_UNABLE_CONNECT:
                //connectBT();
                break;
        }
    }

    public Handler getHandler() {
        return new IncomingHandler(this);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            assert action != null;
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        connectBT();
                        break;
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);

        if (mService != null) {
            mService.stop();
            mService.cancelDiscovery();
        }

    }

}
