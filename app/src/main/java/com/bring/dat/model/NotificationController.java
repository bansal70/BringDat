package com.bring.dat.model;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;

import com.bring.dat.R;
import com.bring.dat.model.network.APIClient;
import com.bring.dat.model.network.ApiService;
import com.bring.dat.model.pojo.OrderDetails;
import com.bring.dat.views.PrintActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.zj.btsdk.BluetoothService;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/*
 * Created by win 10 on 12/25/2017.
 */

public class NotificationController extends FirebaseMessagingService {

    ApiService apiService;
    BluetoothService mService = null;
    BluetoothDevice con_dev = null;
    private String header = "", msg = "", orderId = "";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Timber.e("Notification :: %s", remoteMessage.getData().toString());

        orderId = remoteMessage.getData().get("orderId");
        String token = BDPreferences.readString(getApplicationContext(), Constants.KEY_TOKEN);

        apiService = APIClient.getClient().create(ApiService.class);

        apiService.getOrderDetails(Operations.ordersDetailsParams(orderId, token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {
                    Timber.e("Server error");
                })
                .doOnNext(this::orderDetails)
                .doOnError(AppUtils::serverError)
                .subscribe();
    }

    private void orderDetails(OrderDetails mOrderDetails) {
        header = AppUtils.headerOrderReceipt(mOrderDetails);
        msg = AppUtils.makeOrderReceipt(mOrderDetails);

        mService = new BluetoothService(this, mHandler);
        if (mService.getState() != BluetoothService.STATE_CONNECTED) {
            con_dev = mService.getDevByMac(Constants.PRINTER_MAC_ADDRESS);
            mService.connect(con_dev);
        } else {
            printReceipt();
        }
    }

    public void printReceipt() {
        byte[] cmd = new byte[3];
        cmd[0] = 0x1b;
        cmd[1] = 0x21;
        cmd[2] |= 0x10;
        mService.write(cmd);
        mService.sendMessage(header, "GBK");
        cmd[2] &= 0xEF;
        mService.write(cmd);
        mService.sendMessage(msg, "GBK");
    }

    private final Handler mHandler = new Handler(msg -> {
        switch (msg.what) {
            case BluetoothService.MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                    case BluetoothService.STATE_CONNECTED:
                        //Utils.showToast(getApplicationContext(), getApplicationContext().getString(R.string.success_connection));
                        printReceipt();
                        break;
                    case BluetoothService.STATE_CONNECTING:
                        Timber.e("Bluetooth is connecting");
                        break;
                    case BluetoothService.STATE_LISTEN:
                    case BluetoothService.STATE_NONE:
                        Timber.e("Bluetooth state listen or none");
                        break;
                }
                break;
            case BluetoothService.MESSAGE_CONNECTION_LOST:
                startMain();
                Utils.showToast(getApplicationContext(), getApplicationContext().getString(R.string.error_connection_lost));
                break;
            case BluetoothService.MESSAGE_UNABLE_CONNECT:
                startMain();
                Utils.showToast(getApplicationContext(), getApplicationContext().getString(R.string.error_connection_failed));
                break;
        }

        return false;
    });

    private void startMain() {
        Intent intent = new Intent(getApplicationContext(), PrintActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("orderId", orderId);
        startActivity(intent);
    }

}
