package com.bring.dat.views;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.TextView;

import com.bring.dat.R;
import com.bring.dat.model.AppUtils;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.Operations;
import com.bring.dat.model.pojo.OrderDetails;
import com.zj.btsdk.BluetoothService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PrintActivity extends AppBaseActivity {

    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_CONNECT_DEVICE = 1;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbarTitle)
    TextView tvTitle;

    @BindView(R.id.btnSearch)
    Button btnSearch;

    @BindView(R.id.btnSend)
    Button btnSend;

    @BindView(R.id.txt_content)
    TextView tvContent;

   // BluetoothService mService = null;
    BluetoothDevice con_dev = null;

    String msg = "";
    String header = "";

    OrderDetails mOrderDetails = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle.setText(getString(R.string.title_activity_orders));

        /*mService = new BluetoothService(this, AppUtils.btHandler(mContext));

        if (!mService.isAvailable()) {
            Toast.makeText(this, R.string.error_bluetooth_unavailable, Toast.LENGTH_LONG).show();
        }*/

        getDetails();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mService != null && !mService.isBTopen()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    private void getDetails() {
        String token = BDPreferences.readString(mContext, Constants.KEY_TOKEN);
        String orderId = getIntent().getStringExtra("orderId");
        showDialog();

        apiService.getOrderDetails(Operations.ordersDetailsParams(orderId, token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {
                    serverError();
                })
                .doOnNext(this::orderDetails)
                .doOnError(this::serverError)
                .subscribe();
    }

    private void orderDetails(OrderDetails mOrderDetails) {
        dismissDialog();

        if (mOrderDetails.success) {
            header = AppUtils.headerOrderReceipt(mOrderDetails);
            msg = AppUtils.makeOrderReceipt(mOrderDetails);
            tvContent.setText(String.format("%s%s", header, msg));
        }

        this.mOrderDetails = mOrderDetails;
    }

    @OnClick(R.id.btnSearch)
    public void searchBT() {
        if (mService != null && !mService.isBTopen()) {
            enableBluetooth();
            return;
        }

        con_dev = mService.getDevByMac(Constants.PRINTER_MAC_ADDRESS);
        mService.connect(con_dev);

        /*Intent serverIntent = new Intent(mContext, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);*/
    }

    @OnClick(R.id.btnSend)
    public void printReceipt() {
        if (mService.getState() != BluetoothService.STATE_CONNECTED) {
            showToast(getString(R.string.prompt_connect_printer));
            return;
        }
        printOrderReceipt(mOrderDetails);

        /*byte[] cmd = new byte[3];
        cmd[0] = 0x1b;
        cmd[1] = 0x21;
        cmd[2] |= 0x10;
        mService.write(cmd);
        mService.sendMessage(header, "GBK");
        cmd[2] &= 0xEF;
        mService.write(cmd);
        mService.sendMessage(msg, "GBK");*/
    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        getDetails();
    }

}
