package com.bring.dat.views;

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
import com.bring.dat.model.PrintReceipt;
import com.bring.dat.model.pojo.OrderDetails;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PrintActivity extends AppBaseActivity {

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

        getDetails();
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
            String header = AppUtils.headerOrderReceipt(mOrderDetails);
            // String msg = AppUtils.receiptDetails(mOrderDetails);
            tvContent.setText(header);
        }

        this.mOrderDetails = mOrderDetails;
    }

    @OnClick(R.id.btnSearch)
    public void searchBT() {
        Intent serverIntent = new Intent(mContext, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    @OnClick(R.id.btnSend)
    public void printReceipt() {
        if (isInternetActive())
            PrintReceipt.printOrderReceipt(mContext, mOrderDetails);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        getDetails();
    }

}
