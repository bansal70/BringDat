package com.bring.dat.views;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bring.dat.R;
import com.bring.dat.model.AppUtils;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.Operations;
import com.bring.dat.model.PrintReceipt;
import com.bring.dat.model.Utils;
import com.bring.dat.model.pojo.Cart;
import com.bring.dat.model.pojo.Order;
import com.bring.dat.model.pojo.OrderDetails;
import com.bring.dat.views.adapters.ItemsAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class OrderDetailsActivity extends AppBaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbarTitle)
    TextView tvTitle;

    @BindView(R.id.scrollView)
    ScrollView scrollView;

    @BindView(R.id.tvPersonName)
    TextView tvPersonName;

    @BindView(R.id.tvOrderPrice)
    TextView tvOrderPrice;

    @BindView(R.id.tvOrderPhone)
    TextView tvOrderPhone;

    @BindView(R.id.tvOrderStatus)
    TextView tvOrderStatus;

    @BindView(R.id.tvAddress)
    TextView tvAddress;

    @BindView(R.id.tvPaymentType)
    TextView tvPaymentType;

    @BindView(R.id.tvDeliveryType)
    TextView tvDeliveryType;

    @BindView(R.id.tvOrderNumber)
    TextView tvOrderNumber;

    @BindView(R.id.tvOrderTime)
    TextView tvOrderTime;

    @BindView(R.id.tvDate)
    TextView tvDate;

    @BindView(R.id.tvSubtotal)
    TextView tvSubtotal;

    @BindView(R.id.tvTaxValue)
    TextView tvTaxValue;

    @BindView(R.id.tvSalesTax)
    TextView tvSalesTax;

    @BindView(R.id.tvTip)
    TextView tvTip;

    @BindView(R.id.tvDeliveryPrice)
    TextView tvDeliveryPrice;

    @BindView(R.id.tvConvenienceTax)
    TextView tvConvenienceTax;

    @BindView(R.id.tvDiscountValue)
    TextView tvDiscountValue;

    @BindView(R.id.tvDiscount)
    TextView tvDiscount;

    @BindView(R.id.tvTotal)
    TextView tvTotal;

    @BindView(R.id.btPrint)
    Button btPrint;

    @BindView(R.id.btChangeStatus)
    Button btChangeStatus;

    @BindView(R.id.recyclerItems)
    RecyclerView recyclerItems;

    @BindView(R.id.llOrderTime)
    LinearLayout llOrderTime;

    OrderDetails mOrderDetails = null;

    ItemsAdapter itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle.setText(getString(R.string.title_activity_order_details));

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
            scrollView.setVisibility(View.VISIBLE);
            llOrderTime.setVisibility(View.VISIBLE);

            this.mOrderDetails = mOrderDetails;
            setData(mOrderDetails);
        } else {
            showToast(mOrderDetails.msg);
        }
    }

    private void setData(OrderDetails mOrderDetails) {
        if (BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER)) {
            btChangeStatus.setVisibility(View.GONE);
        }

        OrderDetails.Data data = mOrderDetails.data;
        Order mOrder = data.order.get(0);
        tvOrderTime.setText(String.format("%s", mOrder.orderdate));
        tvPersonName.setText(String.format("%s %s", mOrder.customername, mOrder.customerlastname));
        tvOrderPhone.setText(mOrder.customercellphone);
        tvPaymentType.setText(mOrder.paymentType);
        tvOrderNumber.setText(mOrder.orderid);
        tvAddress.setText(AppUtils.userAddress(mOrder));
        String mDeliveryType = mOrder.deliverytype.substring(0, 1).toUpperCase() + mOrder.deliverytype.substring(1);
        tvDeliveryType.setText(mDeliveryType);
        tvDate.setText(mOrder.deliverytime);

        int color = mOrder.status.contains("cancel") || mOrder.status.contains("decline") ?
                ContextCompat.getColor(mContext, R.color.colorRed) :
                ContextCompat.getColor(mContext, R.color.colorGreen);

        String mOrderStatus = mOrder.status.substring(0, 1).toUpperCase() + mOrder.status.substring(1);
        tvOrderStatus.setText(mOrderStatus);
        tvOrderStatus.setTextColor(color);
        tvOrderPrice.setText(String.format("%s%s", Constants.CURRENCY, mOrder.ordertotalprice));

        tvSubtotal.setText(String.format("%s%s", Constants.CURRENCY, mOrder.ordersubtotal));
        tvSalesTax.setText(String.format("%s%s", Constants.CURRENCY, mOrder.taxamount));
        tvTip.setText(String.format("%s%s", Constants.CURRENCY, mOrder.tipamount));
        tvDeliveryPrice.setText(String.format("%s%s", Constants.CURRENCY, mOrder.deliveryamount));
        tvConvenienceTax.setText(String.format("%s%s", Constants.CURRENCY, mOrder.convenienceFee));
        tvDiscount.setText(String.format("%s%s", Constants.CURRENCY, mOrder.siteDiscountAmount));
        tvTaxValue.setText(String.format("(%s%%)", mOrder.taxvalue));
        tvDiscountValue.setText(String.format("(%s%%)", mOrder.siteDiscountPercent));
        tvTotal.setText(String.format("%s%s", Constants.CURRENCY, mOrder.ordertotalprice));

        if (mOrder.order_print_status.equals("0")) {
            btPrint.setText(mContext.getString(R.string.prompt_print));
            btPrint.setBackgroundResource(R.drawable.shape_rounded_yellow);
        } else {
            btPrint.setText(mContext.getString(R.string.prompt_reprint));
            btPrint.setBackgroundResource(R.drawable.shape_rounded_red);
        }

        List<Cart> mListCart = mOrderDetails.data.cart;
        itemsAdapter = new ItemsAdapter(mContext, mListCart);
        recyclerItems.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerItems.setAdapter(itemsAdapter);
    }

    @OnClick(R.id.btPrint)
    public void reprintReceipt() {
        if (isInternetActive()) {
            AlertDialog alert = Utils.createAlert(this, getString(R.string.prompt_print_receipt), getString(R.string.alert_reprint_receipt));
            alert.setButton(Dialog.BUTTON_POSITIVE, getString(android.R.string.yes),
                    (dialogInterface, i) -> print());
            alert.setButton(Dialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss());
            alert.show();
        }
    }

    private void print() {
        PrintReceipt.printOrderReceipt(mContext, mOrderDetails);
        btPrint.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorRed));
        btPrint.setText(getString(R.string.prompt_reprint));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        getDetails();
    }

}
