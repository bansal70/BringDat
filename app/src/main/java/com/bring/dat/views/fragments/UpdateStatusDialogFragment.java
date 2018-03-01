package com.bring.dat.views.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bring.dat.R;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.Operations;
import com.bring.dat.model.Utils;
import com.bring.dat.model.pojo.Order;
import com.bring.dat.model.pojo.Settings;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@SuppressWarnings("ConstantConditions")
public class UpdateStatusDialogFragment extends DialogBaseFragment{

    Unbinder unbinder;
    @BindView(R.id.btPending)
    Button btPending;

    @BindView(R.id.btWorkingTime)
    Button btWorkingTime;

    @BindView(R.id.tvOrderCurrentStatus)
    TextView tvOrderCurrentStatus;

    String orderStatus, mOrderTime = "", mOrderId;

    Dialog dialogTime;
    private OnDataChangeListener onDataChangeListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = Utils.createDialog(mContext, R.layout.dialog_order_status);

        unbinder = ButterKnife.bind(this, dialog);

        initView();
        return dialog;
    }

    public void initView() {
        Bundle bundle = getArguments();
        if (bundle == null)
            return;
        String json = bundle.getString(Constants.ORDER_DATA);
        Order mOrder = new Gson().fromJson(json, Order.class);
        mOrderId = mOrder.orderid;


        if (mOrder.status.contains("pending")) {
            btWorkingTime.setVisibility(View.VISIBLE);
            btPending.setVisibility(View.GONE);
        } else {
            btWorkingTime.setVisibility(View.GONE);
            btPending.setVisibility(View.VISIBLE);
        }
        tvOrderCurrentStatus.setText(mOrder.status);
    }

    @OnClick(R.id.btPending)
    public void makeOrderPending() {
        orderStatus = "1"; // pending order
        mOrderTime = "";

        String message = mContext.getString(R.string.alert_move_order_to_pending);
        DialogAlertFragment dialogAlertFragment = new DialogAlertFragment();
        dialogAlertFragment.show(mActivity.getSupportFragmentManager(), message);
        dialogAlertFragment.setOnAcceptListener(this::updateOrder);
    }

    @OnClick(R.id.btWorkingTime)
    public void changeOrderTime() {
        orderStatus = "2"; // processing order
        updateTime();
    }

    @OnClick(R.id.btComplete)
    public void completeOrder() {
        orderStatus = "3"; // completed order
        mOrderTime = "";

        String message = mContext.getString(R.string.alert_move_order_to_complete);
        DialogAlertFragment dialogAlertFragment = new DialogAlertFragment();
        dialogAlertFragment.show(mActivity.getSupportFragmentManager(), message);
        dialogAlertFragment.setOnAcceptListener(this::updateOrder);
    }

    @OnClick(R.id.btCancel)
    public void cancelOrder() {
        String message = mContext.getString(R.string.alert_cancel_order);
        DialogAlertFragment dialogAlertFragment = new DialogAlertFragment();
        dialogAlertFragment.show(mActivity.getSupportFragmentManager(), message);
        dialogAlertFragment.setOnAcceptListener(this::updateOrder);
    }

    private void updateOrder() {
        if (!mActivity.isInternetActive()) {
            return;
        }

        mActivity.showDialog();
        String restId = BDPreferences.readString(mContext, Constants.KEY_RESTAURANT_ID);
        String token = BDPreferences.readString(mContext, Constants.KEY_TOKEN);

        apiService.changeOrderStatus(Operations.updateOrderParams(restId, mOrderId, orderStatus, mOrderTime, token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {
                    mActivity.serverError();
                })
                .doOnNext(this::setOrderStatus)
                .doOnError(mActivity::serverError)
                .subscribe();
    }

    private void setOrderStatus(Settings mSettings) {
        mActivity.dismissDialog();
        if (!mSettings.success) {
            mActivity.showToast(mSettings.msg);
            return;
        }

        dismiss();

        if (onDataChangeListener != null) {
            onDataChangeListener.onDataChange();
        }
    }

    private void updateTime() {
        mOrderTime = "";
        dialogTime = Utils.createDialog(mContext, R.layout.dialog_working_time);
        EditText editTime = dialogTime.findViewById(R.id.editTime);

        dialogTime.findViewById(R.id.btApply).setOnClickListener(view -> {
            mOrderTime = editTime.getText().toString().trim();
            if (mOrderTime.isEmpty()) {
                Utils.showToast(mContext, mContext.getString(R.string.error_empty_time));
                return;
            }
            dialogTime.dismiss();
            orderStatus = "2"; // processing order
            updateOrder();
            dismiss();
        });

        dialogTime.findViewById(R.id.bt10min).setOnClickListener(view -> updateTime(10));
        dialogTime.findViewById(R.id.bt20min).setOnClickListener(view -> updateTime(20));
        dialogTime.findViewById(R.id.bt30min).setOnClickListener(view -> updateTime(30));
        dialogTime.findViewById(R.id.bt15min).setOnClickListener(view -> updateTime(15));
        dialogTime.findViewById(R.id.bt45min).setOnClickListener(view -> updateTime(45));
        dialogTime.findViewById(R.id.bt60min).setOnClickListener(view -> updateTime(60));
        dialogTime.findViewById(R.id.btCancel).setOnClickListener(view -> dialogTime.dismiss());

        dialogTime.show();
    }

    private void updateTime(int time) {
        orderStatus = "2"; // processing order
        mOrderTime = String.valueOf(time);
        updateOrder();
        dismiss();
        dialogTime.dismiss();
    }

    public interface OnDataChangeListener {
        void onDataChange();
    }

    public void setOnDataChangeListener(OnDataChangeListener onDataChangeListener) {
        this.onDataChangeListener = onDataChangeListener;
    }

    @OnClick(R.id.fabCancel)
    public void cancelDialog() {
        dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbinder.unbind();
    }
}
