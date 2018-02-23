package com.bring.dat.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bring.dat.R;
import com.bring.dat.model.pojo.OrderDetails;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OrderLogAdapter extends RecyclerView.Adapter<OrderLogAdapter.ViewHolder>{

    private Context mContext;
    private List<OrderDetails.OrderLog> mListLog;

    public OrderLogAdapter(Context mContext, List<OrderDetails.OrderLog> mListLog) {
        this.mContext = mContext;
        this.mListLog = mListLog;
    }

    @Override
    public OrderLogAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_order_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OrderLogAdapter.ViewHolder holder, int position) {
        OrderDetails.OrderLog mLog = mListLog.get(position);
        holder.tvActionType.setText(mLog.actionType);
        holder.tvActionDate.setText(mLog.addedDate);
    }

    @Override
    public int getItemCount() {
        return mListLog.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.tvActionType)
        TextView tvActionType;

        @BindView(R.id.tvActionDate)
        TextView tvActionDate;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
