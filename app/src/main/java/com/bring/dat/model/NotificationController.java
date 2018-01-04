package com.bring.dat.model;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.bring.dat.R;
import com.bring.dat.model.network.APIClient;
import com.bring.dat.model.network.ApiService;
import com.bring.dat.model.pojo.OrderDetails;
import com.bring.dat.views.OrdersListActivity;
import com.bring.dat.views.PrintActivity;
import com.bring.dat.views.services.BTService;
import com.bring.dat.views.services.BluetoothService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/*
 * Created by win 10 on 12/25/2017.
 */

public class NotificationController extends FirebaseMessagingService {

    ApiService apiService;
    BluetoothService mService = null;
    private String orderId = "";
    Context mContext;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Timber.e("Notification :: %s", remoteMessage.getData().toString());

        mContext = getApplicationContext();
        orderId = remoteMessage.getData().get("orderId");
        String token = BDPreferences.readString(getApplicationContext(), Constants.KEY_TOKEN);

        mService = BTService.mService;

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

        sendNotification();
        startMain();
    }

    private void orderDetails(OrderDetails mOrderDetails) {
        PrintReceipt.printOrderReceipt(mContext, mOrderDetails);
    }

    private void startMain() {
        Intent intent = new Intent(getApplicationContext(), PrintActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("orderId", orderId);
        startActivity(intent);
    }

    private void sendNotification() {
        Intent intent = new Intent(mContext, OrdersListActivity.class);
        intent.putExtra("order", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_bring_logo_round)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("You have got new order")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        assert notificationManager != null;
        notificationManager.notify(0, notificationBuilder.build());
    }

}