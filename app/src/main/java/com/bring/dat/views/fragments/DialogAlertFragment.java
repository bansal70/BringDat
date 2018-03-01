package com.bring.dat.views.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

public class DialogAlertFragment extends DialogBaseFragment{
    private OnAcceptListener onAcceptListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
        alertBuilder.setMessage(getTag());
        alertBuilder.setPositiveButton("Yes", (dialogInterface, i) -> {
            if (onAcceptListener != null) {
                onAcceptListener.onAccept();
            }
            dialogInterface.dismiss();
        });

        alertBuilder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
        return alertBuilder.create();
    }

    public interface OnAcceptListener {
        void onAccept();
    }

    public void setOnAcceptListener(OnAcceptListener onAcceptListener) {
        this.onAcceptListener = onAcceptListener;
    }

}
