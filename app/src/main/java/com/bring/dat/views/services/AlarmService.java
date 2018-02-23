package com.bring.dat.views.services;

/*
 * Created by rishav on 2/2/2018.
 */

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.bring.dat.R;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.Operations;
import com.bring.dat.model.Utils;
import com.bring.dat.model.network.APIClient;
import com.bring.dat.model.network.ApiService;
import com.bring.dat.model.pojo.OrdersResponse;

import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AlarmService extends Service{

    private Timer mTimer;
    private Handler mHandler = new Handler();
    private Context mContext;

    private static final int TIMER_INTERVAL = 60 * 1000; // 1 Minute
    private static final int TIMER_DELAY = 0;
    private ApiService apiService;
    private String restId, token;
    private Ringtone ringtoneSound;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();

        apiService = APIClient.getClient().create(ApiService.class);
        restId = BDPreferences.readString(mContext, Constants.KEY_RESTAURANT_ID);
        token = BDPreferences.readString(mContext, Constants.KEY_TOKEN);

        if (mTimer != null)
            mTimer = null;

        // Create new Timer
        mTimer = new Timer();

        // Required to Schedule DisplayToastTimerTask for repeated execution with an interval of `1 min`
        mTimer.scheduleAtFixedRate(new DisplayToastTimerTask(), TIMER_DELAY, TIMER_INTERVAL);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private class DisplayToastTimerTask extends TimerTask {
        @Override
        public void run() {
            mHandler.post(AlarmService.this::getPendingOrders);
        }
    }

    private void getPendingOrders() {
        apiService.getNewOrders(Operations.newOrdersParams(restId, token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {})
                .doOnNext(this::displayPending)
                .subscribe();
    }

    private void displayPending(OrdersResponse mOrderResponse) {
        if (mOrderResponse.success) {
            OrdersResponse.Data mOrdersData = mOrderResponse.data;
            if (mOrdersData.orderList.size() != 0) {
                Utils.showToast(mContext, getString(R.string.error_pending_order_incomplete));
                playSound();
            }
        }
    }

    private void playSound() {
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

        ringtoneSound = RingtoneManager.getRingtone(getApplicationContext(), alarmSound);
        if (ringtoneSound != null) {
            ringtoneSound.play();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (ringtoneSound != null) {
            ringtoneSound.stop();
        }

        mTimer.cancel();
    }
}
