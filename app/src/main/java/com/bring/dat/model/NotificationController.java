package com.bring.dat.model;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.bring.dat.R;
import com.bring.dat.model.network.APIClient;
import com.bring.dat.model.network.ApiService;
import com.bring.dat.model.pojo.OrderDetails;
import com.bring.dat.views.HomeActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/*
 * Created by rishav on 12/25/2017.
 */

public class NotificationController extends FirebaseMessagingService {

    ApiService apiService;
    private String orderId = "";
    Context mContext;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Timber.e("Notification :: %s", remoteMessage.getData().toString());

        mContext = getApplicationContext();
        orderId = remoteMessage.getData().get("orderId");
        String token = BDPreferences.readString(getApplicationContext(), Constants.KEY_TOKEN);

        if (BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER)) {
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

        sendNotification();
    }

    private void orderDetails(OrderDetails mOrderDetails) {
        PrintReceipt.printOrderReceipt(mContext, mOrderDetails);
        startMain(mOrderDetails);
    }

    private void startMain(OrderDetails mOrderDetails) {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("orderId", orderId);
        intent.putExtra("orderDetails", new Gson().toJson(mOrderDetails));
        startActivity(intent);
    }

    @SuppressWarnings("deprecation")
    private void sendNotification() {
        Intent intent = new Intent(mContext, HomeActivity.class);
        intent.putExtra("order", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + getPackageName() + "/raw/flurry");

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_bring_logo_round)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_content))
                .setAutoCancel(true)
                .setSound(alarmSound)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        assert notificationManager != null;
        notificationManager.notify(0, notificationBuilder.build());
    }

}