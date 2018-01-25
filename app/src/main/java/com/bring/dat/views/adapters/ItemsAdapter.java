package com.bring.dat.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bring.dat.R;
import com.bring.dat.model.pojo.Cart;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder>{
    private Context mContext;
    private List<Cart> mListCart;

    public ItemsAdapter(Context mContext, List<Cart> mListCart) {
        this.mContext = mContext;
        this.mListCart = mListCart;
    }

    @Override
    public ItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemsAdapter.ViewHolder holder, int position) {
        Cart mCart = mListCart.get(position);
        holder.tvItems.setText(mCart.item);
        holder.tvQuantity.setText(mCart.qty);
        float totalPrice = Float.parseFloat(mCart.qty) * mCart.price;
        //String price = mCart.qty + "*" + mCart.price + " = ";
        holder.tvItemPrice.setText(String.format("$%s", totalPrice));
        if (mCart.toppingsName.isEmpty() && mCart.description.isEmpty()) {
            holder.tvToppings.setVisibility(View.GONE);
        } else {
            if (mCart.toppingsName.isEmpty()) {
                holder.tvToppings.setText(mCart.description);
            }
            if (!mCart.full.isEmpty()) {
                holder.tvToppings.append(mCart.full + "\n");
            }
            if (!mCart.half1.isEmpty()) {
                holder.tvToppings.append(mContext.getString(R.string.prompt_half_one) + " " + mCart.half1 + "\n");
            }
            if (!mCart.half2.isEmpty()) {
                holder.tvToppings.append(mContext.getString(R.string.prompt_half_two) + " " + mCart.half2);
            }
        }

    }

    @Override
    public int getItemCount() {
        return mListCart.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.tvItems)
        TextView tvItems;

        @BindView(R.id.tvItemPrice)
        TextView tvItemPrice;

        @BindView(R.id.tvQuantity)
        TextView tvQuantity;

        @BindView(R.id.tvToppings)
        TextView tvToppings;

        private ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
