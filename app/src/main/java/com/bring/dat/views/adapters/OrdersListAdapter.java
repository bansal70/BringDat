package com.bring.dat.views.adapters;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bring.dat.R;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.Operations;
import com.bring.dat.model.PrintReceipt;
import com.bring.dat.model.Utils;
import com.bring.dat.model.pojo.Order;
import com.bring.dat.model.pojo.OrderDetails;
import com.bring.dat.views.AppBaseActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class OrdersListAdapter extends RecyclerView.Adapter<OrdersListAdapter.ViewHolder> {

    private Context mContext;
    private List<Order> mListOrderDetails;
    private AppBaseActivity mActivity;

    public OrdersListAdapter(Context mContext, List<Order> mListOrderDetails) {
        this.mContext = mContext;
        this.mListOrderDetails = mListOrderDetails;
        mActivity = (AppBaseActivity) mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_orders, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Order mOrderDetails = mListOrderDetails.get(position);
        holder.tvDate.setText(String.format("%s %s", mOrderDetails.orderDate, mOrderDetails.orderdate));
        holder.tvName.setText(String.format("%s %s", mOrderDetails.customername, mOrderDetails.customerlastname));
        holder.tvContact.setText(mOrderDetails.customercellphone);
        holder.tvPaymentType.setText(mOrderDetails.paymentType);
        holder.tvApartment.setText(mOrderDetails.deliverystreet);
        holder.tvPriority.setText(mOrderDetails.deliverytime);
        holder.tvAddress.setText(String.format("%s, %s, %s", mOrderDetails.cityName, mOrderDetails.deliverystate, mOrderDetails.deliveryzip));

        int color = mOrderDetails.status.contains("cancel") || mOrderDetails.status.contains("decline") ?
                ContextCompat.getColor(mContext, R.color.colorRed) :
                ContextCompat.getColor(mContext, R.color.colorGreen);

        String mOrderStatus = mOrderDetails.status.substring(0, 1).toUpperCase() + mOrderDetails.status.substring(1);
        holder.tvDeliveryStatus.setText(mOrderStatus);
        holder.tvDeliveryStatus.setBackgroundColor(color);
        holder.tvAmount.setText(String.format("%s%s", Constants.CURRENCY, mOrderDetails.ordertotalprice));

        if (mOrderDetails.order_print_status.equals("0")) {
            holder.btReprint.setText(mContext.getString(R.string.prompt_print));
            holder.btReprint.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorYellow));
        } else {
            holder.btReprint.setText(mContext.getString(R.string.prompt_reprint));
            holder.btReprint.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorRed));
        }
    }

    @Override
    public int getItemCount() {
        return mListOrderDetails.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvDate)
        TextView tvDate;

        @BindView(R.id.tvName)
        TextView tvName;

        @BindView(R.id.tvContact)
        TextView tvContact;

        @BindView(R.id.tvPaymentType)
        TextView tvPaymentType;

        @BindView(R.id.tvApartment)
        TextView tvApartment;

        @BindView(R.id.tvPriority)
        TextView tvPriority;

        @BindView(R.id.tvAddress)
        TextView tvAddress;

        @BindView(R.id.tvDeliveryStatus)
        TextView tvDeliveryStatus;

        @BindView(R.id.tvAmount)
        TextView tvAmount;

        @BindView(R.id.btReprint)
        Button btReprint;

        private ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(view -> {
              //  Order mOrder = mListOrderDetails.get(getAdapterPosition());

                if (mActivity.isInternetActive())
                    alertPrint(getAdapterPosition());

                /* mContext.startActivity(new Intent(mContext, PrintActivity.class)
                        .putExtra("orderId", mOrder.orderid)); */
            });
        }

        @OnClick(R.id.btReprint)
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