package com.bring.dat.views.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bring.dat.R;
import com.bring.dat.model.AppUtils;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.Operations;
import com.bring.dat.model.PrintReceipt;
import com.bring.dat.model.Utils;
import com.bring.dat.model.pojo.Order;
import com.bring.dat.model.pojo.OrderDetails;
import com.bring.dat.model.pojo.Settings;
import com.bring.dat.views.AppBaseActivity;
import com.bring.dat.views.OrderDetailsActivity;
import com.bring.dat.views.fragments.HomeFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder>{

    private Context mContext;
    private List<Order> mListOrderDetails;
    private AppBaseActivity mActivity;
    private Dialog dialogOrder, dialogTime;
    private String mOrderId = "", orderStatus, mOrderTime;

    @BindView(R.id.btPending)
    Button btPending;

    @BindView(R.id.btWorkingTime)
    Button btWorkingTime;

    @BindView(R.id.tvOrderCurrentStatus)
    TextView tvOrderCurrentStatus;

    private int mPosition;

    public HomeAdapter(Context mContext, List<Order> mListOrderDetails) {
        this.mContext = mContext;
        this.mListOrderDetails = mListOrderDetails;
        mActivity = (AppBaseActivity) mContext;

        dialogOrder = Utils.createDialog(mContext, R.layout.dialog_order_status);
        ButterKnife.bind(this, dialogOrder);
    }

    @Override
    public HomeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_order_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HomeAdapter.ViewHolder holder, int position) {
        Order mOrder = mListOrderDetails.get(position);
        holder.tvOrderTime.setText(String.format("%s", mOrder.orderdate));
        holder.tvPersonName.setText(String.format("%s %s", mOrder.customername, mOrder.customerlastname));
        holder.tvOrderPhone.setText(mOrder.customercellphone);
        holder.tvPaymentType.setText(mOrder.paymentType);
        holder.tvOrderNumber.setText(mOrder.orderid);
        holder.tvAddress.setText(AppUtils.userAddress(mOrder));
        String mDeliveryType = mOrder.deliverytype.substring(0, 1).toUpperCase() + mOrder.deliverytype.substring(1);
        if (mOrder.deliverytype.equalsIgnoreCase("delivery")) {
            holder.tvDeliveryType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delivery, 0,0,0);
        } else {
            holder.tvDeliveryType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pickup, 0,0,0);
        }
        holder.tvDeliveryType.setText(mDeliveryType);

        int color = mOrder.status.contains("cancel") || mOrder.status.contains("decline") ?
                ContextCompat.getColor(mContext, R.color.colorRed) :
                ContextCompat.getColor(mContext, R.color.colorGreen);

        if (mOrder.status.contains("cancel") || mOrder.status.contains("decline") || mOrder.status.contains("complete")) {
            holder.btChangeStatus.setVisibility(View.GONE);
        } else {
            holder.btChangeStatus.setVisibility(View.VISIBLE);
        }

        String mOrderStatus = mOrder.status.substring(0, 1).toUpperCase() + mOrder.status.substring(1);
        holder.tvOrderStatus.setText(mOrderStatus);
        holder.tvOrderStatus.setTextColor(color);
        holder.tvOrderPrice.setText(String.format("%s%s", Constants.CURRENCY, mOrder.ordertotalprice));
        holder.tvDate.setText(mOrder.deliverytime);

        if (mOrder.order_print_status.equals("0")) {
            holder.btPrint.setText(mContext.getString(R.string.prompt_print));
            holder.btPrint.setBackgroundResource(R.drawable.shape_rounded_yellow);
        } else {
            holder.btPrint.setText(mContext.getString(R.string.prompt_reprint));
            holder.btPrint.setBackgroundResource(R.drawable.shape_rounded_red);
        }

        if (BDPreferences.readString(mContext, Constants.KEY_PRINTING_OPTION).equals("1")) {
            switch (BDPreferences.readString(mContext, Constants.KEY_PRINTING_TYPE)) {
                case Constants.PRINTING_PREPAID:
                    if (mOrder.paymentType.equalsIgnoreCase(Constants.PAYMENT_PREPAID)) {
                        holder.btPrint.setVisibility(View.VISIBLE);
                    } else {
                        holder.btPrint.setVisibility(View.GONE);
                    }
                    break;

                case Constants.PRINTING_COD:
                    if (mOrder.paymentType.equalsIgnoreCase(Constants.PAYMENT_COD)) {
                        holder.btPrint.setVisibility(View.VISIBLE);
                    } else {
                        holder.btPrint.setVisibility(View.GONE);
                    }
                    break;

                case Constants.PRINTING_BOTH:
                    if (mOrder.paymentType.equalsIgnoreCase(Constants.PAYMENT_PREPAID) || mOrder.paymentType.equalsIgnoreCase(Constants.PAYMENT_COD)) {
                        holder.btPrint.setVisibility(View.VISIBLE);
                    } else {
                        holder.btPrint.setVisibility(View.GONE);
                    }
                    break;
            }
        } else {
            holder.btPrint.setVisibility(View.GONE);
        }

        if (BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER)) {
            holder.btChangeStatus.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mListOrderDetails.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
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

        @BindView(R.id.btPrint)
        Button btPrint;

        @BindView(R.id.btChangeStatus)
        Button btChangeStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(view -> {
                Order mOrder = mListOrderDetails.get(getAdapterPosition());

                mContext.startActivity(new Intent(mContext, OrderDetailsActivity.class)
                        .putExtra("orderId", mOrder.orderid));
            });
        }

        @OnClick(R.id.btPrint)
        public void printButton() {
            if (mActivity.isInternetActive())
                alertPrint(getAdapterPosition());
        }

        @OnClick(R.id.btChangeStatus)
        public void changeOrderStatus() {
            Order mOrder = mListOrderDetails.get(getAdapterPosition());
            mOrderId = mOrder.orderid;
            mPosition = getAdapterPosition();

            if (mOrder.status.contains("pending")) {
                btWorkingTime.setVisibility(View.VISIBLE);
                btPending.setVisibility(View.GONE);
            } else {
                btWorkingTime.setVisibility(View.GONE);
                btPending.setVisibility(View.VISIBLE);
            }
            tvOrderCurrentStatus.setText(mOrder.status);
            dialogOrder.show();
        }
    }

    private void alertPrint(int position) {
        Order mOrder = mListOrderDetails.get(position);
        if (mOrder.order_print_status.equals("0")) {
            getDetails(mOrder.orderid, position);
            return;
        }

        AlertDialog alert = Utils.createAlert(mActivity, mContext.getString(R.string.prompt_print_receipt), mContext.getString(R.string.alert_reprint_receipt));
        alert.setButton(Dialog.BUTTON_POSITIVE, mContext.getString(android.R.string.yes),
                (dialogInterface, i) -> getDetails(mOrder.orderid, position));
        alert.setButton(Dialog.BUTTON_NEGATIVE, mContext.getString(android.R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss());
        alert.show();
    }

    private void getDetails(String orderID, int position) {
        String token = BDPreferences.readString(mContext, Constants.KEY_TOKEN);
        mActivity.showDialog();

        mActivity.apiService.getOrderDetails(Operations.ordersDetailsParams(orderID, token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {
                    mActivity.serverError();
                })
                .doOnNext(orderDetails -> orderDetails(orderDetails, position))
                .doOnError(mActivity::serverError)
                .subscribe();
    }

    private void orderDetails(OrderDetails mOrderDetails, int position) {
        mActivity.dismissDialog();

        Order mOrder = mListOrderDetails.get(position);
        mOrder.order_print_status = "1";
        notifyDataSetChanged();

        if (mOrderDetails.success) {
            PrintReceipt.printOrderReceipt(mContext, mOrderDetails);
        }
    }

    @OnClick(R.id.btPending)
    public void makeOrderPending() {
        orderStatus = "1"; // pending order
        mOrderTime = "";
        updateOrder();
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
        updateOrder();
    }

    @OnClick(R.id.btCancel)
    public void cancelOrder() {
        AlertDialog alert = Utils.createAlert(mActivity, "", mContext.getString(R.string.alert_cancel_order));
        alert.setButton(Dialog.BUTTON_POSITIVE, mContext.getString(android.R.string.yes),
                (dialogInterface, i) -> {
                    orderStatus = "4"; // canceled order
                    mOrderTime = "";
                    updateOrder();
                });
        alert.setButton(Dialog.BUTTON_NEGATIVE, mContext.getString(android.R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss());
        alert.show();
    }

    private void updateTime(int time) {
        orderStatus = "2"; // processing order
        mOrderTime = String.valueOf(time);
        updateOrder();
        dialogOrder.dismiss();
        dialogTime.dismiss();
    }

    @OnClick(R.id.fabCancel)
    public void cancelDialog() {
        dialogOrder.dismiss();
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

    private void updateOrder() {
        if (!mActivity.isInternetActive()) {
            return;
        }

        mActivity.showDialog();
        String restId = BDPreferences.readString(mContext, Constants.KEY_RESTAURANT_ID);
        String token = BDPreferences.readString(mContext, Constants.KEY_TOKEN);

        mActivity.apiService
                .changeOrderStatus(Operations.updateOrderParams(restId, mOrderId, orderStatus, mOrderTime, token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {
                    mActivity.serverError();
                })
                .doOnNext(this::setOrderStatus)
                .doOnError(mActivity::serverError)
                .subscribe();

        dialogOrder.dismiss();
    }

    private void setOrderStatus(Settings mSettings) {
        mActivity.dismissDialog();
        if (!mSettings.success) {
            mActivity.showToast(mSettings.msg);
            return;
        }

        Order mOrder = mListOrderDetails.get(mPosition);
        switch (orderStatus) {
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

        notifyItemChanged(mPosition);
        mActivity.goToFragment(new HomeFragment());
    }

}
