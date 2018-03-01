package com.bring.dat.views.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.TextView;

import com.bring.dat.BuildConfig;
import com.bring.dat.R;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.Operations;
import com.bring.dat.model.Utils;
import com.bring.dat.model.pojo.Settings;
import com.bring.dat.views.DeviceListActivity;
import com.bring.dat.views.HomeActivity;
import com.bring.dat.views.services.BTService;
import com.bring.dat.views.services.BluetoothService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTouch;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.bring.dat.views.AppBaseActivity.REQUEST_CONNECT_DEVICE;

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

    @BindView(R.id.switchBtPrinter)
    SwitchCompat switchBtPrinter;

    @BindView(R.id.switchNwPrinter)
    SwitchCompat switchNwPrinter;

    @BindView(R.id.switchAutoPrint)
    SwitchCompat switchAutoPrint;

    @BindView(R.id.tvAppVersion)
    TextView tvAppVersion;

    @BindView(R.id.tvConnectedAddress)
    TextView tvConnectedAddress;

    String restId, token, soundType = "", onlineOrder, printingStatus, printingOptions;
    int mCheckedId;
//    private WifiManager mWifiManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        unbinder = ButterKnife.bind(this, view);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        mActivity.registerReceiver(mReceiver, filter);
      //  mWifiManager = (WifiManager)mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

      /*  if (Utils.isServiceRunning(mContext, WFService.class) && WFService.isWifi()) {
            switchNwPrinter.setChecked(true);
            String ipAddress = BDPreferences.readString(mContext, Constants.KEY_IP_ADDRESS);
            tvConnectedAddress.setText(String.format("%s %s", getString(R.string.prompt_ip_address), ipAddress));
            tvConnectedAddress.setVisibility(View.VISIBLE);
        }*/

        if (!BDPreferences.readString(mContext, Constants.KEY_IP_ADDRESS).isEmpty()) {
            String ipAddress = BDPreferences.readString(mContext, Constants.KEY_IP_ADDRESS);
            tvConnectedAddress.setText(String.format("%s %s", getString(R.string.prompt_ip_address), ipAddress));
            tvConnectedAddress.setVisibility(View.VISIBLE);
            switchNwPrinter.setChecked(true);
        }

        initValues();

        return view;
    }

    private void initValues() {
        restId = BDPreferences.readString(mContext, Constants.KEY_RESTAURANT_ID);
        token = BDPreferences.readString(mContext, Constants.KEY_TOKEN);

        tvAppVersion.setText(BuildConfig.VERSION_NAME);

        if (BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER)) {
            btChangeAlias.setText(getString(R.string.prompt_switch_admin));
        } else {
            btChangeAlias.setText(getString(R.string.prompt_switch_logger));
        }

        if (BTService.mService != null && BTService.mService.getState() == BluetoothService.STATE_CONNECTED) {
            switchBtPrinter.setChecked(true);
        }

        if (BDPreferences.readBoolean(mContext, Constants.AUTO_PRINT_TYPE)) {
            switchAutoPrint.setChecked(true);
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

        switchAutoPrint.setChecked(mData.autoPrint.equals("1"));
        switchPrinting.setChecked(mData.printingOption.equals("1"));
        BDPreferences.putBoolean(mContext, Constants.AUTO_PRINT_TYPE, mData.autoPrint.equals("1"));

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

    @OnTouch(R.id.switchAutoPrint)
    public boolean autoPrinting() {
        String mStatus;
        if (switchAutoPrint.isChecked()) {
            mStatus = "0";
        } else {
            mStatus = "1";
        }

        updateAutoPrintStatusApi(mStatus);

        return false;
    }

    private void updateAutoPrintStatusApi(String status) {
        mActivity.showDialog();
        apiService.updateAutoPrinting(Operations.updateAutoPrintStatus(restId, status, token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {
                    mActivity.serverError();
                })
                .doOnNext(this::setAutoPrintingStatus)
                .doOnError(mActivity::serverError)
                .subscribe();
    }

    private void setAutoPrintingStatus(Settings mSettings) {
        mActivity.dismissDialog();
        if (mSettings.success) {
            switchAutoPrint.setChecked(mSettings.mAutoPrint.equals("1"));
            BDPreferences.putBoolean(mContext, Constants.AUTO_PRINT_TYPE, mSettings.mAutoPrint.equals("1"));
        } else {
            showToast(mSettings.msg);
        }
    }


    @OnTouch(R.id.switchNwPrinter)
    boolean networkPrinter() {
        if (switchNwPrinter.isChecked()) {
            switchNwPrinter.setChecked(false);
            BDPreferences.removeKey(mContext, Constants.KEY_IP_ADDRESS);
          /*  if (Utils.isServiceRunning(mContext, WFService.class)) {
                mActivity.stopService(new Intent(mContext, WFService.class));
            }*/
        }
       /* if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }*/

        DialogNetworkPrinter dialogNetworkPrinter = new DialogNetworkPrinter();
        dialogNetworkPrinter.show(getChildFragmentManager(), dialogNetworkPrinter.getTag());
        dialogNetworkPrinter.setOnWifiConnectedListener(address -> {
            switchNwPrinter.setChecked(true);
            tvConnectedAddress.setVisibility(View.VISIBLE);
            tvConnectedAddress.setText(String.format("%s %s", getString(R.string.prompt_ip_address), address));
        });

        /*dialogNetworkPrinter.setOnWifiConnectedListener(() -> {
            switchNwPrinter.setChecked(true);
            tvConnectedAddress.setVisibility(View.VISIBLE);
            String ipAddress = BDPreferences.readString(mContext, Constants.KEY_IP_ADDRESS);
            tvConnectedAddress.setText(String.format("%s %s", getString(R.string.prompt_ip_address), ipAddress));
        });*/

        return false;
    }

    @OnTouch(R.id.switchBtPrinter)
    boolean bluetoothPrinter() {
        if (switchBtPrinter.isChecked()) {
            Intent i = new Intent(mContext, BTService.class);
            mActivity.stopService(i);
            switchBtPrinter.setChecked(false);
        } else {
            Intent intent = new Intent(mContext, DeviceListActivity.class);
            startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    if (data.getExtras() == null) {
                        return;
                    }
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BDPreferences.putString(mContext, Constants.KEY_BT_ADDRESS, address);

                    Intent intent = new Intent(mContext, BTService.class);
                    mActivity.startService(intent);
                }
                break;
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                switchBtPrinter.setChecked(true);
                tvConnectedAddress.setVisibility(View.VISIBLE);
                String btAddress = BDPreferences.readString(mContext, Constants.KEY_BT_ADDRESS);
                tvConnectedAddress.setText(String.format("%s %s", getString(R.string.prompt_bluetooth_address), btAddress));
                switchNwPrinter.setChecked(false);
                BDPreferences.removeKey(mContext, Constants.KEY_IP_ADDRESS);
            }
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                switchBtPrinter.setChecked(false);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbinder.unbind();
        mActivity.unregisterReceiver(mReceiver);
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
