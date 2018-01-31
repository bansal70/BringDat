package com.bring.dat.views.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.bring.dat.R;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.Operations;
import com.bring.dat.model.Utils;
import com.bring.dat.model.pojo.Settings;
import com.bring.dat.views.HomeActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTouch;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SettingsFragment extends AppBaseFragment{

    Unbinder unbinder;

    @BindView(R.id.parentLL)
    LinearLayout parentLL;

    @BindView(R.id.btChangeAlias)
    Button btChangeAlias;

    @BindView(R.id.switchOnlineOrdering)
    SwitchCompat switchOnlineOrdering;

    @BindView(R.id.switchPrinting)
    SwitchCompat switchPrinting;

    @BindView(R.id.cbCODOrder)
    CheckBox cbCOD;

    @BindView(R.id.cbPrepaidOrder)
    CheckBox cbPrepaid;

    @BindView(R.id.mFirstGroup)
    RadioGroup mFirstGroup;

    @BindView(R.id.mSecondGroup)
    RadioGroup mSecondGroup;

    @BindView(R.id.rbRinging)
    RadioButton rbRinging;

    @BindView(R.id.rbBuzzer)
    RadioButton rbBuzzer;

    @BindView(R.id.rbExplore)
    RadioButton rbExplore;

    @BindView(R.id.rbClock)
    RadioButton rbClock;

    String restId, token, soundType = "", onlineOrder, printingStatus, printingOptions;
    int mCheckedId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        unbinder = ButterKnife.bind(this, view);

        initValues();

        return view;
    }

    private void initValues() {
        restId = BDPreferences.readString(mContext, Constants.KEY_RESTAURANT_ID);
        token = BDPreferences.readString(mContext, Constants.KEY_TOKEN);

       /* // checking notification sound status
        soundType = BDPreferences.readString(mContext, Constants.KEY_SOUND_TYPE);

        switch (soundType) {
            case Constants.SOUND_RINGING:
                rbRinging.setChecked(true);
                mCheckedId = R.id.rbRinging;
                break;
            case Constants.SOUND_BUZZER:
                rbBuzzer.setChecked(true);
                mCheckedId = R.id.rbBuzzer;
                break;
            case Constants.SOUND_EXPLO:
                rbExplore.setChecked(true);
                mCheckedId = R.id.rbExplore;
                break;
            case Constants.SOUND_OLD_SCHOOL:
                rbClock.setChecked(true);
                mCheckedId = R.id.rbClock;
                break;
        }

        // Checking online order status
        onlineOrder = BDPreferences.readString(mContext, Constants.KEY_ONLINE_ORDER);
        switchOnlineOrdering.setChecked(onlineOrder.equals("0"));

        // Checking printing status
        printingStatus = BDPreferences.readString(mContext, Constants.KEY_PRINTING_TYPE);
        setPrintingType();*/

        if (BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER)) {
            btChangeAlias.setText(getString(R.string.prompt_switch_admin));
        } else {
            btChangeAlias.setText(getString(R.string.prompt_switch_logger));
        }

        if (!mActivity.isInternetActive()) {
            connectionAlert();
            return;
        }
        getSettings();
    }

    private void getSettings() {
        mActivity.showDialog();
        apiService.getSettings(Operations.settingsParams(restId, token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {
                    mActivity.serverError();
                })
                .doOnNext(this::setData)
                .doOnError(mActivity::serverError)
                .subscribe();
    }

    private void setData(Settings mSettings) {
        mActivity.dismissDialog();

        if (!mSettings.success) {
            showToast(mSettings.msg);
            return;
        }
        parentLL.setVisibility(View.VISIBLE);

        Settings.Data mData = mSettings.data;

        switchOnlineOrdering.setChecked(mData.onlineOrder.equals("0"));

        switch (mData.soundType) {
            case Constants.SOUND_RINGING:
                rbRinging.setChecked(true);
                mCheckedId = R.id.rbRinging;
                break;
            case Constants.SOUND_BUZZER:
                rbBuzzer.setChecked(true);
                mCheckedId = R.id.rbBuzzer;
                break;
            case Constants.SOUND_EXPLO:
                rbExplore.setChecked(true);
                mCheckedId = R.id.rbExplore;
                break;
            case Constants.SOUND_OLD_SCHOOL:
                rbClock.setChecked(true);
                mCheckedId = R.id.rbClock;
                break;
        }

        switchPrinting.setChecked(mData.printingOption.equals("1"));

        printingStatus = mData.printingType;
        printingOptions = mData.printingOption;
        setPrintingType();

        setSoundListeners();
    }


    @OnCheckedChanged(R.id.switchOnlineOrdering)
    public void onlineOrder(boolean checked) {
        if (checked) {
            onlineOrder = "0";
            updateOnlineOrder();
        } else {
            onlineOrder = "1";
            updateOnlineOrder();
        }
    }

    private void updateOnlineOrder() {
        if (!mActivity.isInternetActive()) {
            return;
        }
        mActivity.showDialog();
        apiService.updateOrderStatus(Operations.orderStatusParams(restId, onlineOrder, token))
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
        if (mSettings.success) {
            BDPreferences.putString(mContext, Constants.KEY_ONLINE_ORDER, mSettings.mOrderStatus);
        } else {
            showToast(mSettings.msg);
        }
    }

    @OnTouch(R.id.switchPrinting)
    public boolean printingStatus() {
        if (!mActivity.isInternetActive()) {
            return false;
        }

        printingOptions = switchPrinting.isChecked() ? Constants.PRINTING_OFF : Constants.PRINTING_ON;

        updatePrintingStatus(printingOptions);

        return false;
    }

    @OnTouch(R.id.cbCODOrder)
    boolean codOrders() {
        if (!mActivity.isInternetActive()) {
            return false;
        }

        if (cbPrepaid.isChecked())
            printingStatus = !cbCOD.isChecked() ? Constants.PRINTING_BOTH : Constants.PRINTING_PREPAID;
         else
            printingStatus = cbCOD.isChecked() ? Constants.PRINTING_DISABLE : Constants.PRINTING_COD;

        updatePrintingStatus(printingStatus);

        return false;
    }

    @OnTouch(R.id.cbPrepaidOrder)
    boolean prepaidOrders() {
        if (!mActivity.isInternetActive()) {
            return false;
        }

        if (cbCOD.isChecked())
            printingStatus = !cbPrepaid.isChecked() ? Constants.PRINTING_BOTH : Constants.PRINTING_COD;
         else
            printingStatus = cbPrepaid.isChecked() ? Constants.PRINTING_DISABLE : Constants.PRINTING_PREPAID;

        updatePrintingStatus(printingStatus);

        return false;
    }

    private void updatePrintingStatus(String printingMethod) {
        mActivity.showDialog();
        apiService.updatePrintingStatus(Operations.printingStatusParams(restId, printingMethod, token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {
                    mActivity.serverError();
                })
                .doOnNext(this::setPrintingStatus)
                .doOnError(mActivity::serverError)
                .subscribe();
    }

    private void setPrintingStatus(Settings mSettings) {
        mActivity.dismissDialog();
        if (mSettings.success) {
            BDPreferences.putString(mContext, Constants.KEY_PRINTING_TYPE, printingStatus);
            BDPreferences.putString(mContext, Constants.KEY_PRINTING_OPTION, mSettings.printingOption);
            switchPrinting.setChecked(mSettings.printingOption.equals("1"));
            setPrintingType();
        } else {
            showToast(mSettings.msg);
        }
    }

    private void setPrintingType() {
        switch (printingStatus) {
            case Constants.PRINTING_COD:
                cbCOD.setChecked(true);
                cbPrepaid.setChecked(false);
                break;
            case Constants.PRINTING_PREPAID:
                cbCOD.setChecked(false);
                cbPrepaid.setChecked(true);
                break;
            case Constants.PRINTING_BOTH:
                cbCOD.setChecked(true);
                cbPrepaid.setChecked(true);
                break;
            case Constants.PRINTING_DISABLE:
                cbCOD.setChecked(false);
                cbPrepaid.setChecked(false);
                break;
        }
    }

    private void setSoundListeners() {
        mFirstGroup.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            if (checkedId != -1) {
                mSecondGroup.clearCheck();
                mCheckedId = checkedId;
                mFirstGroup.check(mCheckedId);
                updateSounds();
            }
        });

        mSecondGroup.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            if (checkedId != -1) {
                mFirstGroup.clearCheck();
                mCheckedId = checkedId;
                mSecondGroup.check(mCheckedId);
                updateSounds();
            }
        });
    }

    private void updateSounds() {
        if (!mActivity.isInternetActive()) {
            return;
        }

        switch (mCheckedId) {
            case R.id.rbRinging:
                soundType = Constants.SOUND_RINGING;
                break;
            case R.id.rbBuzzer:
                soundType = Constants.SOUND_BUZZER;
                break;
            case R.id.rbExplore:
                soundType = Constants.SOUND_EXPLO;
                break;
            case R.id.rbClock:
                soundType = Constants.SOUND_OLD_SCHOOL;
                break;
        }

        mActivity.showDialog();
        apiService.updateSound(Operations.soundParams(restId, soundType, token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {
                    mActivity.serverError();
                })
                .doOnNext(this::setSound)
                .doOnError(mActivity::serverError)
                .subscribe();
    }

    private void setSound(Settings mSettings) {
        mActivity.dismissDialog();
        if (mSettings.success) {
            BDPreferences.putString(mContext, Constants.KEY_SOUND_TYPE, soundType);
        }  else {
            showToast(mSettings.msg);
        }
    }

    @OnClick(R.id.btChangeAlias)
    public void changeAlias() {
        if (!mActivity.isInternetActive()) {
            return;
        }

        if (BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER)) {
            BDPreferences.putString(mContext, Constants.KEY_LOGIN_TYPE, Constants.LOGIN_ADMIN);
        } else {
            BDPreferences.putString(mContext, Constants.KEY_LOGIN_TYPE, Constants.LOGIN_LOGGER);
        }

        Intent intent = new Intent(mContext, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbinder.unbind();
    }

    private void connectionAlert() {
        AlertDialog alert = Utils.createAlert(mActivity, getString(R.string.error_connection_down), getString(R.string.error_internet_disconnected));

        alert.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.prompt_retry), (dialogInterface, i) -> {
            if (!mActivity.isInternetActive()) {
                connectionAlert();
                return;
            }

            getSettings();

        });
        alert.show();
    }
}
