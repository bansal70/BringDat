package com.bring.dat.views;

import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bring.dat.R;
import com.bring.dat.model.AppUtils;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.NetworkPrinting;
import com.bring.dat.model.Operations;
import com.bring.dat.model.PrintReceipt;
import com.bring.dat.model.Utils;
import com.bring.dat.model.pojo.AdjustReasons;
import com.bring.dat.model.pojo.Cart;
import com.bring.dat.model.pojo.Order;
import com.bring.dat.model.pojo.OrderDetails;
import com.bring.dat.views.adapters.ItemsAdapter;
import com.bring.dat.views.adapters.OrderLogAdapter;
import com.bring.dat.views.fragments.AdjustSaleDialogFragment;
import com.bring.dat.views.fragments.UpdateStatusDialogFragment;
import com.bring.dat.views.fragments.VoidSaleDialogFragment;
import com.bring.dat.views.services.BTService;
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

    @BindView(R.id.tvEmail)
    TextView tvEmail;

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

    @BindView(R.id.couponsLL)
    LinearLayout couponsLL;

    @BindView(R.id.tvOfferName)
    TextView tvOfferName;

    @BindView(R.id.tvOfferAmount)
    TextView tvOfferAmount;

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

    @BindView(R.id.cardInstructions)
    CardView cardInstructions;

    @BindView(R.id.tvOrderInstructions)
    TextView tvOrderInstructions;

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

    @BindView(R.id.cardLog)
    CardView cardLog;

    @BindView(R.id.recyclerLog)
    RecyclerView recyclerLog;

    OrderDetails mOrderDetails = null;

    ItemsAdapter itemsAdapter;

    AdjustReasons mAdjustReasons;

    NetworkPrinting networkPrinting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        ButterKnife.bind(this);
        networkPrinting = new NetworkPrinting(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle.setText(getString(R.string.title_activity_order_details));
        toolbar.setNavigationIcon(R.drawable.ic_back_image);

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

        tvOrderTime.setText(String.format("%s %s", Utils.parseDateToMMddYY(mOrder.deliverydate), mOrder.orderdate));
        tvPersonName.setText(String.format("%s %s", mOrder.customername, mOrder.customerlastname));
        tvEmail.setText(mOrder.customeremail);
        tvOrderPhone.setText(mOrder.customercellphone);
        tvPaymentType.setText(mOrder.paymentType);
        tvOrderNumber.setText(mOrder.orderid);
        tvAddress.setText(AppUtils.userAddress(mOrder));

        if (mOrder.deliverytime.equalsIgnoreCase("ASAP")) {
            tvDate.setText(mOrder.deliverytime);
        } else {
            tvDate.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorBloodRed));
            tvDate.setTextColor(ContextCompat.getColor(mContext, R.color.colorWhite));
            tvDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_clock_white, 0,0,0);
            String time;
            if (Utils.getToday().equals(mOrder.deliverydate)) {
                time = getString(R.string.prompt_today) + " " + Utils.parseTimeToAMPM(mOrder.deliverytime);
            } else {
                time = Utils.parseDateToMMdd(mOrder.deliverydate) + " " + mOrder.deliverytime;
            }
            tvDate.setText(time);
        }

        String mDeliveryType = mOrder.deliverytype.substring(0, 1).toUpperCase() + mOrder.deliverytype.substring(1);
        if (mOrder.deliverytype.equalsIgnoreCase("delivery")) {
            tvDeliveryType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delivery, 0,0,0);
        } else {
            tvDeliveryType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pickup, 0,0,0);
        }

        tvDeliveryType.setText(mDeliveryType);

        int color = mOrder.status.contains("cancel") || mOrder.status.contains("decline") ?
                ContextCompat.getColor(mContext, R.color.colorRed) :
                ContextCompat.getColor(mContext, R.color.colorGreen);

        String mOrderStatus = mOrder.status.substring(0, 1).toUpperCase() + mOrder.status.substring(1);
        tvOrderStatus.setText(mOrderStatus);
        tvOrderStatus.setTextColor(color);
        tvOrderPrice.setText(String.format("%s%s", Constants.CURRENCY, mOrder.ordertotalprice));
        tvSubtotal.setText(String.format("%s%s", Constants.CURRENCY, mOrder.ordersubtotal));
        if (!mOrder.offerId.isEmpty() && !mOrder.offeramount.equals("0.00")) {
            couponsLL.setVisibility(View.VISIBLE);
            tvOfferName.setText(String.format("(%s)", mOrder.offerName));
            tvOfferAmount.setText(String.format("-%s%s", Constants.CURRENCY, mOrder.offeramount));
        }
        tvSalesTax.setText(String.format("%s%s", Constants.CURRENCY, mOrder.taxamount));
        tvTip.setText(String.format("%s%s", Constants.CURRENCY, mOrder.tipamount));
        tvDeliveryPrice.setText(String.format("%s%s", Constants.CURRENCY, mOrder.deliveryamount));
        tvConvenienceTax.setText(String.format("%s%s", Constants.CURRENCY, mOrder.convenienceFee));
        tvDiscount.setText(String.format("-%s%s", Constants.CURRENCY, mOrder.siteDiscountAmount));
        tvDiscountValue.setText(String.format("(%s%%)", mOrder.siteDiscountPercent));
        tvTaxValue.setText(String.format("(%s%%)", mOrder.taxvalue));
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
                    if (!mOrder.paymentType.equalsIgnoreCase(Constants.PAYMENT_COD)) {
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
                    btPrintOrder.setVisibility(View.VISIBLE);
                    break;
            }
        } else {
            btPrintOrder.setVisibility(View.GONE);
        }

        if (!mOrder.instructions.isEmpty()) {
            cardInstructions.setVisibility(View.VISIBLE);
            tvOrderInstructions.setText(mOrder.instructions);
        }

        List<Cart> mListCart = mOrderDetails.data.cart;
        itemsAdapter = new ItemsAdapter(mContext, mListCart);
        recyclerItems.setNestedScrollingEnabled(false);
        recyclerItems.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerItems.setAdapter(itemsAdapter);

        checkInterval(mOrder);
        setStatus(mOrder);
        setPaymentDetails(mOrderDetails);
        setOrderLog(mOrderDetails);
    }

    private void setPaymentDetails(OrderDetails mOrderDetails) {
        OrderDetails.Data data = mOrderDetails.data;
        Order mOrder = data.order.get(0);

        List<List<String>> listCard = data.ccDetails;
        if (listCard.size() == 0)
            return;

        List<String> card = listCard.get(0);
        if (card.size() > 4 && (mOrder.paymentType.equalsIgnoreCase(Constants.PAYMENT_CC) || mOrder.paymentType.equalsIgnoreCase(Constants.PAYMENT_PREPAID))) {
            if (card.get(0).isEmpty() && card.get(1).isEmpty()) {
                return;
            }

            cardPaymentType.setVisibility(View.VISIBLE);
            tvCardType.append(" " + card.get(0).toUpperCase());
            tvCardName.append(" " + card.get(1).toUpperCase());
            tvCardNumber.append(" " + card.get(2).toUpperCase());
            tvExpiry.append(" " + card.get(3).toUpperCase());
            tvCVC.append(" " + card.get(4).toUpperCase());
        }
    }

    private void setOrderLog(OrderDetails mOrderDetails) {
        OrderDetails.Data data = mOrderDetails.data;
        List<OrderDetails.OrderLog> mListLog = data.orderLog;
        if (mListLog.size() == 0) {
            return;
        }

        cardLog.setVisibility(View.VISIBLE);
        OrderLogAdapter mLogAdapter = new OrderLogAdapter(mContext, mListLog);
        recyclerLog.setNestedScrollingEnabled(false);
        recyclerLog.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerLog.setAdapter(mLogAdapter);
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
        if (!BDPreferences.readString(mContext, Constants.KEY_IP_ADDRESS).isEmpty()) {

            if (!networkPrinting.isPrinted) {
                Utils.showToast(mContext, "Please wait while we are processing your last receipt");
                return;
            }
            networkPrinting.printData((Activity)mContext, mOrderDetails);
        } else if (Utils.isServiceRunning(mContext, BTService.class)) {
            PrintReceipt.printOrderReceipt(mContext, mOrderDetails);
        } else {
            Utils.showToast(mContext, mContext.getString(R.string.error_printer_unavailable));
        }
        //PrintReceipt.printOrderReceipt(mContext, mOrderDetails);
        btPrintOrder.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorRed));
        btPrintOrder.setText(getString(R.string.prompt_reprint));
    }

    @OnClick(R.id.btChangeOrderStatus)
    public void changeOrderStatus() {
        OrderDetails.Data data = mOrderDetails.data;
        Order mOrder = data.order.get(0);

        Bundle bundle = new Bundle();
        bundle.putString(Constants.ORDER_DATA, new Gson().toJson(mOrder));
        UpdateStatusDialogFragment statusDialogFragment = new UpdateStatusDialogFragment();
        statusDialogFragment.setArguments(bundle);
        statusDialogFragment.show(getSupportFragmentManager(), statusDialogFragment.getTag());
        statusDialogFragment.setOnDataChangeListener(this::setResults);
    }

    public void setResults() {
        Intent i = new Intent();
        i.putExtra(Constants.ORDER_DATA, true);
        setResult(RESULT_OK);
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

        //getDetails();
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
