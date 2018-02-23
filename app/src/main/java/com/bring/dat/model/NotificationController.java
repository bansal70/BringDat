package com.bring.dat.model;

import android.app.Activity;
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
import com.bring.dat.views.services.BTService;
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

        //   if (BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER)) {
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
        //   }

        sendNotification();
    }

    private void orderDetails(OrderDetails mOrderDetails) {
        startMain(mOrderDetails);

        if (BDPreferences.readBoolean(mContext, Constants.AUTO_PRINT_TYPE)) {
            //PrintReceipt.printOrderReceipt(mContext, mOrderDetails);
            if (!BDPreferences.readString(mContext, Constants.KEY_IP_ADDRESS).isEmpty()) {
                NetworkPrinting networkPrinting = new NetworkPrinting((Activity) mContext);
                networkPrinting.printData((Activity)mContext, mOrderDetails);
            } else if (Utils.isServiceRunning(mContext, BTService.class)) {
                PrintReceipt.printOrderReceipt(mContext, mOrderDetails);
            } else {
                Utils.showToast(mContext, mContext.getString(R.string.error_printer_unavailable));
            }
        }
        //String printingOptions =  BDPreferences.readString(mContext, Constants.KEY_PRINTING_OPTION);
        //String printingType = BDPreferences.readString(mContext, Constants.KEY_PRINTING_TYPE);

        //OrderDetails.Data data = mOrderDetails.data;
        //Order mOrder = data.order.get(0);


        /*if (printingOptions.equals("1")) {
            switch (printingType) {
                case Constants.PRINTING_COD:
                    if (mOrder.paymentType.equalsIgnoreCase(Constants.PAYMENT_COD)) {
                        PrintReceipt.printOrderReceipt(mContext, mOrderDetails);
                    }
                    break;

                case Constants.PRINTING_PREPAID:
                    if (mOrder.paymentType.equalsIgnoreCase(Constants.PAYMENT_PREPAID)) {
                        PrintReceipt.printOrderReceipt(mContext, mOrderDetails);
                    }
                    break;

                case Constants.PRINTING_BOTH:
                    PrintReceipt.printOrderReceipt(mContext, mOrderDetails);
                    break;
            }
        }*/
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
        Intent intent;

        if (BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER))
            intent = new Intent(mContext, HomeActivity.class);
        else
            intent = new Intent();

        intent.putExtra("order", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String soundType = BDPreferences.readString(mContext, Constants.KEY_SOUND_TYPE);
        switch (soundType) {
            case Constants.SOUND_RINGING:
                soundType = "pending_alert";
                break;
            case Constants.SOUND_BUZZER:
                soundType = "flurry";
                break;
            case Constants.SOUND_EXPLO:
                soundType = "explo";
                break;
            case Constants.SOUND_OLD_SCHOOL:
                soundType = "oldschoolclock";
                break;
        }
        Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + getPackageName() + "/raw/" + soundType);

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