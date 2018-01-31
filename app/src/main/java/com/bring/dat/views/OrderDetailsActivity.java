package com.bring.dat.views;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.bring.dat.model.pojo.AdjustReasons;
import com.bring.dat.model.pojo.Cart;
import com.bring.dat.model.pojo.Order;
import com.bring.dat.model.pojo.OrderDetails;
import com.bring.dat.model.pojo.Settings;
import com.bring.dat.views.adapters.ItemsAdapter;
import com.bring.dat.views.fragments.AdjustSaleDialogFragment;
import com.bring.dat.views.fragments.VoidSaleDialogFragment;
import com.google.gson.Gson;

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

    @BindView(R.id.actionLL)
    LinearLayout actionLL;

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

    @BindView(R.id.orderButtonLL)
    LinearLayout orderButtonLL;

    @BindView(R.id.btPrintOrder)
    Button btPrintOrder;

    @BindView(R.id.btChangeOrderStatus)
    Button btChangeOrderStatus;

    @BindView(R.id.recyclerItems)
    RecyclerView recyclerItems;

    @BindView(R.id.llOrderTime)
    LinearLayout llOrderTime;

    @BindView(R.id.cancelOrderLL)
    LinearLayout cancelOrderLL;

    @BindView(R.id.btVoidSale)
    Button btVoidSale;

    @BindView(R.id.btAdjust)
    Button btAdjust;

    @BindView(R.id.imgPayment)
    ImageView imgPayment;

    @BindView(R.id.cardPaymentType)
    CardView cardPaymentType;

    @BindView(R.id.tvCardType)
    TextView tvCardType;

    @BindView(R.id.tvCardName)
    TextView tvCardName;

    @BindView(R.id.tvCardNumber)
    TextView tvCardNumber;

    @BindView(R.id.tvExpiry)
    TextView tvExpiry;

    @BindView(R.id.tvCVC)
    TextView tvCVC;

    OrderDetails mOrderDetails = null;

    ItemsAdapter itemsAdapter;

    private Dialog dialogOrder, dialogTime;
    private String mOrderId = "", mOrderStatus, mOrderTime;
    AdjustReasons mAdjustReasons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle.setText(getString(R.string.title_activity_order_details));

        if (BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER)) {
            toolbar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        } else {
            toolbar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        }

        if (!isInternetActive()) {
            connectionAlert();
            return;
        }
        getDetails();
    }

    private void getDetails() {
        orderButtonLL.setVisibility(View.GONE);
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

        getReasons();
    }

    private void orderDetails(OrderDetails mOrderDetails) {
        dismissDialog();

        if (mOrderDetails.success) {
            scrollView.setVisibility(View.VISIBLE);
            actionLL.setVisibility(View.VISIBLE);
            llOrderTime.setVisibility(View.VISIBLE);

            this.mOrderDetails = mOrderDetails;
            setData(mOrderDetails);
        } else {
            showToast(mOrderDetails.msg);
        }
    }

    private void setData(OrderDetails mOrderDetails) {
        OrderDetails.Data data = mOrderDetails.data;
        Order mOrder = data.order.get(0);
        mOrderId = mOrder.orderid;
        mOrderStatus = mOrder.status;

        tvOrderTime.setText(String.format("%s", mOrder.orderdate));
        tvPersonName.setText(String.format("%s %s", mOrder.customername, mOrder.customerlastname));
        tvOrderPhone.setText(mOrder.customercellphone);
        tvPaymentType.setText(mOrder.paymentType);
        tvOrderNumber.setText(mOrder.orderid);
        tvAddress.setText(AppUtils.userAddress(mOrder));
        String mDeliveryType = mOrder.deliverytype.substring(0, 1).toUpperCase() + mOrder.deliverytype.substring(1);
        if (mOrder.deliverytype.equalsIgnoreCase("delivery")) {
            tvDeliveryType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delivery, 0,0,0);
        } else {
            tvDeliveryType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pickup, 0,0,0);
        }
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
        double salesTax = Utils.roundTwoDecimals(Double.valueOf(mOrder.taxamount));
        tvSalesTax.setText(String.format("%s%s", Constants.CURRENCY, salesTax));
        tvTip.setText(String.format("%s%s", Constants.CURRENCY, mOrder.tipamount));
        tvDeliveryPrice.setText(String.format("%s%s", Constants.CURRENCY, mOrder.deliveryamount));
        tvConvenienceTax.setText(String.format("%s%s", Constants.CURRENCY, mOrder.convenienceFee));
        tvDiscount.setText(String.format("%s%s", Constants.CURRENCY, mOrder.siteDiscountAmount));
        tvTaxValue.setText(String.format("(%s%%)", mOrder.taxvalue));
        tvDiscountValue.setText(String.format("(%s%%)", mOrder.siteDiscountPercent));
        tvTotal.setText(String.format("%s%s", Constants.CURRENCY, mOrder.ordertotalprice));

        if (mOrder.order_print_status.equals("0")) {
            btPrintOrder.setText(mContext.getString(R.string.prompt_print));
            btPrintOrder.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorDarkGreen));
        } else {
            btPrintOrder.setText(mContext.getString(R.string.prompt_reprint));
            btPrintOrder.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorDarkRed));
        }

        if (mOrder.status.contains("cancel")) {
            imgPayment.setImageResource(R.mipmap.ic_canceled_img);
        } else if (mOrder.paymentType.equalsIgnoreCase(Constants.PAYMENT_COD)) {
            imgPayment.setImageResource(R.mipmap.ic_not_paid);
        } else {
            imgPayment.setImageResource(R.mipmap.ic_paid);
        }

        if (BDPreferences.readString(mContext, Constants.KEY_PRINTING_OPTION).equals("1")) {
            switch (BDPreferences.readString(mContext, Constants.KEY_PRINTING_TYPE)) {
                case Constants.PRINTING_PREPAID:
                    if (mOrder.paymentType.equalsIgnoreCase(Constants.PAYMENT_PREPAID)) {
                        btPrintOrder.setVisibility(View.VISIBLE);
                    } else {
                        btPrintOrder.setVisibility(View.GONE);
                    }
                    break;

                case Constants.PRINTING_COD:
                    if (mOrder.paymentType.equalsIgnoreCase(Constants.PAYMENT_COD)) {
                        btPrintOrder.setVisibility(View.VISIBLE);
                    } else {
                        btPrintOrder.setVisibility(View.GONE);
                    }
                    break;

                case Constants.PRINTING_BOTH:
                    if (mOrder.paymentType.equalsIgnoreCase(Constants.PAYMENT_PREPAID) || mOrder.paymentType.equalsIgnoreCase(Constants.PAYMENT_COD)) {
                        btPrintOrder.setVisibility(View.VISIBLE);
                    } else {
                        btPrintOrder.setVisibility(View.GONE);
                    }
                    break;
            }
        }

        List<Cart> mListCart = mOrderDetails.data.cart;
        itemsAdapter = new ItemsAdapter(mContext, mListCart);
        recyclerItems.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerItems.setAdapter(itemsAdapter);

        checkInterval(mOrder);
        setStatus(mOrder);
        setPaymentDetails(mOrderDetails);
    }

    private void setPaymentDetails(OrderDetails mOrderDetails) {
        OrderDetails.Data data = mOrderDetails.data;
        Order mOrder = data.order.get(0);

        List<List<String>> listCard = data.ccDetails;
        if (listCard.size() == 0)
            return;

        List<String> card = listCard.get(0);
        if (card.size() > 4 && (mOrder.paymentType.equalsIgnoreCase(Constants.PAYMENT_CC) || !mOrder.paymentType.equalsIgnoreCase(Constants.PAYMENT_COD))) {
            cardPaymentType.setVisibility(View.VISIBLE);

            tvCardType.append(" " + card.get(0).toUpperCase());
            tvCardName.append(" " + card.get(1).toUpperCase());
            tvCardNumber.append(" " + card.get(2).toUpperCase());
            tvExpiry.append(" " + card.get(3).toUpperCase());
            tvCVC.append(" " + card.get(4).toUpperCase());
        }
    }

    private void checkInterval(Order mOrder) {
        if (BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER)) {
            return;
        }

        if (mOrder.paymentType.equalsIgnoreCase(Constants.PAYMENT_PREPAID)) {
            if (mOrder.twoDaysInterval.equalsIgnoreCase("yes")) {
                cancelOrderLL.setVisibility(View.VISIBLE);
                btVoidSale.setVisibility(mOrder.applyVoid.equalsIgnoreCase("no") ? View.VISIBLE : View.GONE);
                btAdjust.setVisibility(mOrder.applyAdjust.equalsIgnoreCase("no") ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void setStatus(Order mOrder) {
        if (mOrder.status.contains("cancel") || mOrder.status.contains("decline") || mOrder.status.contains("complete")) {
            btChangeOrderStatus.setVisibility(View.GONE);
        } else {
            btChangeOrderStatus.setVisibility(View.VISIBLE);
        }

        int color = mOrder.status.contains("cancel") || mOrder.status.contains("decline") ?
                ContextCompat.getColor(mContext, R.color.colorRed) :
                ContextCompat.getColor(mContext, R.color.colorGreen);

        String mOrderStatus = mOrder.status.substring(0, 1).toUpperCase() + mOrder.status.substring(1);
        tvOrderStatus.setText(mOrderStatus);
        tvOrderStatus.setTextColor(color);

        if (BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER)) {
            btChangeOrderStatus.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.btPrintOrder)
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
        btPrintOrder.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorRed));
        btPrintOrder.setText(getString(R.string.prompt_reprint));
    }

    @OnClick(R.id.btChangeOrderStatus)
    public void changeOrderStatus() {
        dialogOrder = Utils.createDialog(mContext, R.layout.dialog_order_status);
        Button btPending = dialogOrder.findViewById(R.id.btPending);
        Button btWorkingTime = dialogOrder.findViewById(R.id.btWorkingTime);
        Button btComplete = dialogOrder.findViewById(R.id.btComplete);
        Button btCancel = dialogOrder.findViewById(R.id.btCancel);
        TextView tvOrderCurrentStatus = dialogOrder.findViewById(R.id.tvOrderCurrentStatus);

        OrderDetails.Data data = mOrderDetails.data;
        Order mOrder = data.order.get(0);
        tvOrderCurrentStatus.setText(mOrder.status);

        dialogOrder.findViewById(R.id.fabCancel).setOnClickListener(view -> dialogOrder.dismiss());

        btPending.setOnClickListener(view -> {
            mOrderStatus = "1"; // pending order
            mOrderTime = "";
            updateOrder();
        });

        btWorkingTime.setOnClickListener(view -> {
            mOrderStatus = "2"; // processing order
            updateTime();
        });

        btComplete.setOnClickListener(view -> {
            mOrderStatus = "3"; // completed order
            mOrderTime = "";
            updateOrder();
        });

        btCancel.setOnClickListener(view -> {
            AlertDialog alert = Utils.createAlert(this, "", getString(R.string.alert_cancel_order));
            alert.setButton(Dialog.BUTTON_POSITIVE, getString(android.R.string.yes),
                    (dialogInterface, i) -> {
                        mOrderStatus = "4"; // canceled order
                        mOrderTime = "";
                        updateOrder();
                    });
            alert.setButton(Dialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss());
            alert.show();
        });

        if (mOrder.status.contains("pending")) {
            btWorkingTime.setVisibility(View.VISIBLE);
            btPending.setVisibility(View.GONE);
        } else {
            btWorkingTime.setVisibility(View.GONE);
            btPending.setVisibility(View.VISIBLE);
        }

        dialogOrder.show();
    }

    private void updateTime() {
        dialogTime = Utils.createDialog(mContext, R.layout.dialog_working_time);
        EditText editTime = dialogTime.findViewById(R.id.editTime);

        dialogTime.findViewById(R.id.btApply).setOnClickListener(view -> {
            mOrderTime = editTime.getText().toString().trim();
            if (mOrderTime.isEmpty()) {
                Utils.showToast(mContext, mContext.getString(R.string.error_empty_time));
                return;
            }
            dialogTime.dismiss();
            mOrderStatus = "2"; // processing order
            updateOrder();
            dialogOrder.dismiss();
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
        mOrderStatus = "2"; // processing order
        mOrderTime = String.valueOf(time);
        updateOrder();
        dialogOrder.dismiss();
        dialogTime.dismiss();
    }

    private void updateOrder() {
        if (!isInternetActive()) {
            return;
        }

        showDialog();
        String restId = BDPreferences.readString(mContext, Constants.KEY_RESTAURANT_ID);
        String token = BDPreferences.readString(mContext, Constants.KEY_TOKEN);

        apiService.changeOrderStatus(Operations.updateOrderParams(restId, mOrderId, mOrderStatus, mOrderTime, token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {
                    serverError();
                })
                .doOnNext(this::setOrderStatus)
                .doOnError(this::serverError)
                .subscribe();

        dialogOrder.dismiss();
    }

    private void setOrderStatus(Settings mSettings) {
        dismissDialog();
        if (!mSettings.success) {
            showToast(mSettings.msg);
            return;
        }
        OrderDetails.Data data = mOrderDetails.data;
        Order mOrder = data.order.get(0);

        switch (mOrderStatus) {
            case "1":
                mOrder.status = "pending";
                break;
            case "2":
                mOrder.status = "processing";
                break;
            case "3":
                mOrder.status = "completed";
                break;
            case "4":
                mOrder.status = "canceled";
                break;
        }

        setStatus(mOrder);
        finish();
    }

    private void getReasons() {
        // mActivity.showDialog();
        apiService.getAdjustReasons()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {
                    serverError();
                })
                .doOnNext(this::setReasons)
                .doOnError(this::serverError)
                .subscribe();
    }

    private void setReasons(AdjustReasons mReasons) {
        if (mReasons.mSuccess) {
            mAdjustReasons = mReasons;
        }
    }

    @OnClick(R.id.btVoidSale)
    public void voidSale() {
        OrderDetails.Data data = mOrderDetails.data;
        Order mOrder = data.order.get(0);
        Bundle bundle = new Bundle();
        bundle.putString("orderData", new Gson().toJson(mOrder));

        VoidSaleDialogFragment voidFragment = new VoidSaleDialogFragment();
        voidFragment.setArguments(bundle);
        voidFragment.show(getSupportFragmentManager(), "Void Sale");

        voidFragment.setOnVoidSaleListener(this::checkInterval);
    }

    @OnClick(R.id.btAdjust)
    public void adjustSale() {
        OrderDetails.Data data = mOrderDetails.data;
        Order mOrder = data.order.get(0);
        Bundle bundle = new Bundle();
        bundle.putString("orderData", new Gson().toJson(mOrder));
        bundle.putString("adjustData", new Gson().toJson(mAdjustReasons));

        AdjustSaleDialogFragment adjustFragment = new AdjustSaleDialogFragment();
        adjustFragment.setArguments(bundle);
        adjustFragment.show(getSupportFragmentManager(), "Adjust Sale");

        adjustFragment.setOnAdjustSaleListener(this::checkInterval);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        getDetails();
    }

    private void connectionAlert() {
        AlertDialog alert = Utils.createAlert(this, getString(R.string.error_connection_down), getString(R.string.error_internet_disconnected));

        alert.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.prompt_retry), (dialogInterface, i) -> {
            if (!isInternetActive()) {
                connectionAlert();
                return;
            }

            getDetails();

        });
        alert.show();
    }

}
