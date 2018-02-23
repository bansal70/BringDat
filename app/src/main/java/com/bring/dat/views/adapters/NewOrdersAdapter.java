package com.bring.dat.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bring.dat.R;
import com.bring.dat.model.Utils;
import com.bring.dat.model.pojo.Order;
import com.bring.dat.views.OrderDetailsActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewOrdersAdapter extends RecyclerView.Adapter<NewOrdersAdapter.ViewHolder>{
    private Context mContext;
    private List<Order> mListOrderDetails;

    public NewOrdersAdapter(Context mContext, List<Order> mListOrderDetails) {
        this.mContext = mContext;
        this.mListOrderDetails = mListOrderDetails;
    }

    @Override
    public NewOrdersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_new_orders, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NewOrdersAdapter.ViewHolder holder, int position) {
        Order mOrder = mListOrderDetails.get(position);
        if (mOrder.deliverytype.equalsIgnoreCase("delivery")) {
            holder.tvDeliveryType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delivery, 0,0,0);
            holder.ordersLL.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        } else {
            holder.tvDeliveryType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pickup, 0,0,0);
            holder.ordersLL.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorLightYellow));
        }

        if (mOrder.deliverytime.equalsIgnoreCase("ASAP")) {
            if (mOrder.deliverytype.equalsIgnoreCase("delivery")) {
                holder.tvDeliveryType.setText(String.format("%s %s", mOrder.deliverytime, mContext.getString(R.string.prompt_delivery_order)));
            } else {
                holder.tvDeliveryType.setText(String.format("%s %s", mOrder.deliverytime, mContext.getString(R.string.prompt_pickup_order)));
            }
        } else {
            String time;
            if (Utils.getToday().equals(mOrder.deliverydate)) {
                time = mContext.getString(R.string.prompt_time) + " " + mContext.getString(R.string.prompt_today) + " " +
                        Utils.parseTimeToAMPM(mOrder.deliverytime);
            } else {
                time = mContext.getString(R.string.prompt_time) + " " + Utils.parseDateToMMdd(mOrder.deliverydate) + " " +
                        Utils.parseTimeToAMPM(mOrder.deliverytime);
            }
            holder.tvDeliveryType.setText(time);
            holder.tvDeliveryType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_clock, 0,0,0);
            holder.ordersLL.setBackgroundColor(ContextCompat.getColor(mContext, R.color.redColor));
        }

        holder.tvOrderNumber.setText(mOrder.orderid);
        holder.tvOrderTime.setText(String.format("%s %s", Utils.parseDateToMMddYY(mOrder.deliverydate), mOrder.orderdate));
    }

    @Override
    public int getItemCount() {
        return mListOrderDetails.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.tvDeliveryType)
        TextView tvDeliveryType;

        @BindView(R.id.tvOrderNumber)
        TextView tvOrderNumber;

        @BindView(R.id.tvOrderTime)
        TextView tvOrderTime;

        @BindView(R.id.ordersLL)
        LinearLayout ordersLL;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(view -> {
                Order mOrder = mListOrderDetails.get(getAdapterPosition());

                mContext.startActivity(new Intent(mContext, OrderDetailsActivity.class)
                        .putExtra("orderId", mOrder.orderid));
            });
        }
    }
}
