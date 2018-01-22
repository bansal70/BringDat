package com.bring.dat.views.adapters;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bring.dat.R;
import com.bring.dat.model.Utils;
import com.bring.dat.model.pojo.DatesBy;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReportsDateAdapter extends RecyclerView.Adapter<ReportsDateAdapter.ViewHolder> {
    private List<DatesBy> listDays;
    private Context mContext;
    private PopupWindow mPopupWindow;
    private OnDataChangeListener mOnDataChangeListener;
    private Dialog dialog;

    @BindView(R.id.tvFromDate)
    TextView tvFromDate;

    @BindView(R.id.tvToDate)
    TextView tvToDate;

    public ReportsDateAdapter(Context mContext, PopupWindow mPopupWindow) {
        this.mContext = mContext;
        this.mPopupWindow = mPopupWindow;
        listDays = new ArrayList<>();
        dialog = Utils.createDialog(mContext, R.layout.dialog_reports_custom_range);

        ButterKnife.bind(this, dialog);

        addReportsBy();
    }

    private void addReportsBy() {
        listDays.add(new DatesBy(mContext.getString(R.string.prompt_today), true));
        listDays.add(new DatesBy(mContext.getString(R.string.prompt_yesterday), false));
        listDays.add(new DatesBy(mContext.getString(R.string.prompt_last_week), false));
        listDays.add(new DatesBy(mContext.getString(R.string.prompt_last_month), false));
        listDays.add(new DatesBy(mContext.getString(R.string.prompt_this_month), false));
        listDays.add(new DatesBy(mContext.getString(R.string.prompt_previous_month), false));
        listDays.add(new DatesBy(mContext.getString(R.string.prompt_custom_range), false));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_report_date, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DatesBy datesBy = listDays.get(position);
        holder.tvDay.setText(datesBy.getDateBy());

        if (datesBy.ismSelected()) {
            holder.tvDay.setTextColor(ContextCompat.getColor(mContext, R.color.colorRed));
        } else {
            holder.tvDay.setTextColor(ContextCompat.getColor(mContext, R.color.black));
        }
    }

    @Override
    public int getItemCount() {
        return listDays.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvDay)
        TextView tvDay;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.tvDay)
        public void selectDay() {
            int position = getAdapterPosition();
            String startDate = Utils.getCurrentDate();
            String endDate = Utils.getCurrentDate();

            switch (position) {
                case 0:
                    startDate = Utils.getDaysAgo(0);
                    break;

                case 1:
                    startDate = Utils.getDaysAgo(-1);
                    endDate = Utils.getDaysAgo(-1);
                    break;

                case 2:
                    startDate = Utils.getDaysAgo(-6);
                    break;

                case 3:
                    startDate = Utils.getDaysAgo(-29);
                    break;

                case 4:
                    startDate = Utils.getFirstDateOfMonth();
                    endDate = Utils.getLastDateOfMonth();
                    break;

                case 5:
                    startDate = Utils.getFirstDateOfPreviousMonth();
                    endDate = Utils.getLastDateOfPreviousMonth();
                    break;

                case 6:
                    dialog.show();
                    break;
            }

            if (position != 6) {
                mPopupWindow.dismiss();

                for (int i = 0; i < listDays.size(); i++) {
                    if (i == position) {
                        listDays.set(i, new DatesBy(listDays.get(i).getDateBy(), true));
                    } else {
                        listDays.set(i, new DatesBy(listDays.get(i).getDateBy(), false));
                    }
                }

                notifyDataSetChanged();

                if (mOnDataChangeListener != null) {
                    mOnDataChangeListener.onDataChange(startDate, endDate);
                }
            }
        }
    }

    public interface OnDataChangeListener {
        void onDataChange(String startDate, String endDate);
    }

    public void setOnDataChangeListener(OnDataChangeListener onDataChangeListener){
        mOnDataChangeListener = onDataChangeListener;
    }

    @OnClick(R.id.tvFromDate)
    public void setFromDate() {
        Utils.setDatePicker(mContext, tvFromDate);
    }

    @OnClick(R.id.tvToDate)
    public void setToDate() {
        Utils.setDatePicker(mContext, tvToDate);
    }

    @OnClick(R.id.btApply)
    public void applyTime() {
        if (tvToDate.getText().toString().isEmpty() || tvFromDate.getText().toString().isEmpty()) {
            Utils.showToast(mContext, mContext.getString(R.string.error_empty_dates));
            return;
        }

        if(mOnDataChangeListener != null){
            mOnDataChangeListener.onDataChange(tvFromDate.getText().toString(), tvToDate.getText().toString());
        }

        mPopupWindow.dismiss();
        dialog.dismiss();

        for (int i = 0; i < listDays.size(); i++) {
            if (i == listDays.size() - 1) {
                listDays.set(i, new DatesBy(listDays.get(i).getDateBy(), true));
            } else {
                listDays.set(i, new DatesBy(listDays.get(i).getDateBy(), false));
            }
        }
        notifyDataSetChanged();
    }

    @OnClick(R.id.btCancel)
    public void cancelTime() {
        dialog.dismiss();
    }

}
