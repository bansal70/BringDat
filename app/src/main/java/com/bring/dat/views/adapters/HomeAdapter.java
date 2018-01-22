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
import com.bring.dat.views.AppBaseActivity;
import com.bring.dat.views.OrderDetailsActivity;

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

    public HomeAdapter(Context mContext, List<Order> mListOrderDetails) {
        this.mContext = mContext;
        this.mListOrderDetails = mListOrderDetails;
        mActivity = (AppBaseActivity) mContext;
    }

    @Override
    public HomeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_order_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HomeAdapter.ViewHolder holder, int position) {
        if (BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER)) {
            holder.btChangeStatus.setVisibility(View.GONE);
        }

        Order mOrder = mListOrderDetails.get(position);
        holder.tvOrderTime.setText(String.format("%s", mOrder.orderdate));
        holder.tvPersonName.setText(String.format("%s %s", mOrder.customername, mOrder.customerlastname));
        holder.tvOrderPhone.setText(mOrder.customercellphone);
        holder.tvPaymentType.setText(mOrder.paymentType);
        holder.tvOrderNumber.setText(mOrder.orderid);
        holder.tvAddress.setText(AppUtils.userAddress(mOrder));
        String mDeliveryType = mOrder.deliverytype.substring(0, 1).toUpperCase() + mOrder.deliverytype.substring(1);
        holder.tvDeliveryType.setText(mDeliveryType);

        int color = mOrder.status.contains("cancel") || mOrder.status.contains("decline") ?
                ContextCompat.getColor(mContext, R.color.colorRed) :
                ContextCompat.getColor(mContext, R.color.colorGreen);

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
}
