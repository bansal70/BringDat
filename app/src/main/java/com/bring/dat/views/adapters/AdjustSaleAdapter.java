package com.bring.dat.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.bring.dat.R;
import com.bring.dat.model.pojo.AdjustReasons;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class AdjustSaleAdapter extends RecyclerView.Adapter<AdjustSaleAdapter.ViewHolder>{

    private Context mContext;
    private List<AdjustReasons.Option> listOptions;

    public AdjustSaleAdapter(Context mContext, List<AdjustReasons.Option> listOptions) {
        this.mContext = mContext;
        this.listOptions = listOptions;
    }

    @Override
    public AdjustSaleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_adjust_reasons, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AdjustSaleAdapter.ViewHolder holder, int position) {
        AdjustReasons.Option mOptions = listOptions.get(position);

        RadioGroup radioGroup = new RadioGroup(mContext);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        holder.reasonLL.addView(radioGroup, p);

        RadioButton mRadioButton = new RadioButton(mContext);
        mRadioButton.setId(position);
        mRadioButton.setText(mOptions.mReason);
        radioGroup.addView(mRadioButton);

        radioGroup.setOnCheckedChangeListener((mRadioGroup, checkedId) -> {
            if (checkedId != -1) {
                RadioButton radioButton = mRadioGroup.findViewById(checkedId);
                radioButton.setChecked(true);
                String text = radioButton.getText().toString();
                Timber.e(text);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listOptions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.rgReasons)
        RadioGroup rgReasons;

        @BindView(R.id.reasonLL)
        LinearLayout reasonLL;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
