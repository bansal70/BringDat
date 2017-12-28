package com.bring.dat.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bring.dat.R;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.Operations;
import com.bring.dat.model.pojo.Order;
import com.bring.dat.model.pojo.OrderDetails;
import com.bring.dat.views.AppBaseActivity;
import com.bring.dat.views.PrintActivity;
import com.zj.btsdk.BluetoothService;

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
    //private String header = "", msg = "";

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

        if (position == mListOrderDetails.size() - 1) {
            holder.progressBar.setVisibility(View.GONE);
        } else {
            holder.progressBar.setVisibility(View.GONE);
        }

        Order mOrderDetails = mListOrderDetails.get(position);
        holder.tvName.setText(String.format("%s %s", mOrderDetails.customername, mOrderDetails.customerlastname));
        holder.tvContact.setText(mOrderDetails.customercellphone);
        holder.tvApartment.setText(mOrderDetails.deliverystreet);
        holder.tvAddress.setText(String.format("%s, %s, %s", mOrderDetails.cityName, mOrderDetails.deliverystate, mOrderDetails.deliveryzip));

        int color = mOrderDetails.status.contains("complete") || mOrderDetails.status.contains("pending") || mOrderDetails.status.contains("process") ?
                ContextCompat.getColor(mContext, R.color.colorGreen) :
                ContextCompat.getColor(mContext, R.color.colorRed);

        String mOrderStatus = mOrderDetails.status.substring(0, 1).toUpperCase() + mOrderDetails.status.substring(1);
        holder.tvDeliveryStatus.setText(mOrderStatus);
        holder.tvDeliveryStatus.setBackgroundColor(color);
        holder.tvAmount.setText(String.format("%s%s", Constants.CURRENCY, mOrderDetails.ordertotalprice));
    }

    @Override
    public int getItemCount() {
        return mListOrderDetails.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvName)
        TextView tvName;

        @BindView(R.id.tvContact)
        TextView tvContact;

        @BindView(R.id.tvApartment)
        TextView tvApartment;

        @BindView(R.id.tvAddress)
        TextView tvAddress;

        @BindView(R.id.tvDeliveryStatus)
        TextView tvDeliveryStatus;

        @BindView(R.id.tvAmount)
        TextView tvAmount;

        @BindView(R.id.progressBar)
        ProgressBar progressBar;

        private ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(view -> {
                Order mOrder = mListOrderDetails.get(getAdapterPosition());
                mContext.startActivity(new Intent(mContext, PrintActivity.class)
                        .putExtra("orderId", mOrder.orderid));
            });
        }

        @OnClick(R.id.btReprint)
        public void printButton() {
            if (mActivity.mService != null && !mActivity.mService.isBTopen()) {
                mActivity.enableBluetooth();
                return;
            }
            Order mOrder = mListOrderDetails.get(getAdapterPosition());
            getDetails(mOrder.orderid);
            /*mContext.startActivity(new Intent(mContext, PrintActivity.class)
                    .putExtra("orderId", mOrder.orderid));*/
        }
    }

    private void getDetails(String orderID) {
        String token = BDPreferences.readString(mContext, Constants.KEY_TOKEN);
        mActivity.showDialog();

        mActivity.apiService.getOrderDetails(Operations.ordersDetailsParams(orderID, token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {
                    mActivity.serverError();
                })
                .doOnNext(this::orderDetails)
                .doOnError(mActivity::serverError)
                .subscribe();
    }

    private void orderDetails(OrderDetails mOrderDetails) {
        mActivity.dismissDialog();

        if (mOrderDetails.success) {
            //header = AppUtils.headerOrderReceipt(mOrderDetails);
            //msg = AppUtils.makeOrderReceipt(mOrderDetails);
            if (mActivity.mService.getState() != BluetoothService.STATE_CONNECTED) {
                mActivity.showToast(mContext.getString(R.string.prompt_connect_printer));
                return;
            }

            mActivity.printOrderReceipt(mOrderDetails);
        }
    }

    /*private void printReceipt() {
      byte[] cmd = new byte[3];
        cmd[0] = 0x1b;
        cmd[1] = 0x21;
        cmd[2] |= 0x10;
        mActivity.mService.write(cmd);
        mActivity.mService.sendMessage(header, "GBK");
        cmd[2] &= 0xEF;
        mActivity.mService.write(cmd);
        mActivity.mService.sendMessage(msg, "GBK");
    }*/
}