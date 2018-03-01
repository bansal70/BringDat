package com.bring.dat.views.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.EditText;

import com.bring.dat.R;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class DialogNetworkPrinter extends DialogBaseFragment {

    Unbinder unbinder;

    @BindView(R.id.editIPAddress)
    EditText editIPAddress;
    private OnWifiConnectedListener onWifiConnectedListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = Utils.createDialog(mContext, R.layout.dialog_wifi_printer);
        unbinder = ButterKnife.bind(this, dialog);
        return dialog;
    }

    @OnClick(R.id.btConnect)
    public void connectPrinter() {
        String ipAddress = editIPAddress.getText().toString().trim();
        if (TextUtils.isEmpty(ipAddress)) {
            showToast(getString(R.string.error_empty_ip_address));
            return;
        }

       /* if (mActivity.isServiceRunning(WFService.class)) {
            dismiss();
            return;
        }*/

        BDPreferences.putString(mContext, Constants.KEY_IP_ADDRESS, ipAddress);

        if (onWifiConnectedListener != null) {
            onWifiConnectedListener.onWifiConnected(ipAddress);
        }

       /* WFService wfService = new WFService();
        Intent intent = new Intent(mContext, WFService.class);
        mActivity.startService(intent);*/

       /* wfService.setOnWifiConnectedListener(() -> {
            if (onWifiConnectedListener != null) {
                onWifiConnectedListener.onWifiConnected();
            }
        });*/

        dismiss();
    }

    public interface OnWifiConnectedListener {
        void onWifiConnected(String address);
    }

    public void setOnWifiConnectedListener(OnWifiConnectedListener onWifiConnectedListener) {
        this.onWifiConnectedListener = onWifiConnectedListener;
    }
}
