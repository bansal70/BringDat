package com.bring.dat.views.services;

/*
 * Created by rishav on 2/2/2018.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.bring.dat.model.Application;

public class NetworkChangeReceiver extends BroadcastReceiver{
    public static NetworkChangeReceiverListener connectivityReceiverListener;

    public NetworkChangeReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent arg1) {
        ConnectivityManager cm = (ConnectivityManager) Application.getInstance().getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();

        if (connectivityReceiverListener != null) {
            connectivityReceiverListener.onNetworkConnectionChanged(isConnected);
        }
    }

    public interface NetworkChangeReceiverListener {
        void onNetworkConnectionChanged(boolean isConnected);
    }

}
