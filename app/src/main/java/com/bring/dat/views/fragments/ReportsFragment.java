package com.bring.dat.views.fragments;

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bring.dat.R;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.Operations;
import com.bring.dat.model.Utils;
import com.bring.dat.model.pojo.Reports;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ReportsFragment extends AppBaseFragment{

    Unbinder unbinder;

    @BindView(R.id.reportsLL)
    LinearLayout reportsLL;

    @BindView(R.id.tvDateRange)
    TextView tvDateRange;

    @BindView(R.id.tvCashSales)
    TextView tvCashSales;

    @BindView(R.id.tvCreditCardSales)
    TextView tvCreditCardSales;

    @BindView(R.id.tvTips)
    TextView tvTips;

    @BindView(R.id.tvDeliveryCharges)
    TextView tvDeliveryCharges;

    @BindView(R.id.tvTax)
    TextView tvTax;

    @BindView(R.id.tvPositiveAdjustments)
    TextView tvPositiveAdjustments;

    @BindView(R.id.tvNegativeAdjustments)
    TextView tvNegativeAdjustments;

    @BindView(R.id.tvCancelOrders)
    TextView tvCancelOrders;

    @BindView(R.id.tvTotalOrders)
    TextView tvTotalOrders;

    @BindView(R.id.tvGrossTotal)
    TextView tvGrossTotal;

    String endDate = "", startDate, restId, token;

    TextView tvToday, tvYesterday, tvLastWeek, tvLast30Days, tvThisMonth, tvPreviousMonth, tvCustomRange;
    PopupWindow popupWindow;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        unbinder = ButterKnife.bind(this, view);

        initDefault();

        return view;
    }

    private void initDefault() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        endDate = sdf.format(c.getTime());

        startDate = Utils.getDateByRange(6);
        getReports(startDate, endDate);
    }

    private void getReports(String startDate, String endDate) {
        restId = BDPreferences.readString(mContext, Constants.KEY_RESTAURANT_ID);
        token = BDPreferences.readString(mContext, Constants.KEY_TOKEN);

        mActivity.showProgressBar();
        apiService.getReports(Operations.reportParams(restId, startDate, endDate, token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {
                    mActivity.serverError();
                })
                .doOnNext(this::setReportsData)
                .doOnError(mActivity::serverError)
                .subscribe();
    }

    private void setReportsData(Reports reports) {
        mActivity.hideProgressBar();

        if (!reports.success) {
            showToast(getString(R.string.prompt_no_reports));
            return;
        }

        reportsLL.setVisibility(View.VISIBLE);
        Reports.Data data = reports.data;
        tvCashSales.setText(String.format("%s%s", Constants.CURRENCY, data.codTotal));
        tvCreditCardSales.setText(String.format("%s%s", Constants.CURRENCY, data.creditCardTotal));
        tvTips.setText(String.format("%s%s", Constants.CURRENCY, data.tipAmount));
        tvDeliveryCharges.setText(String.format("%s%s", Constants.CURRENCY, data.deliveryCharge));
        tvTax.setText(String.format("%s%s", Constants.CURRENCY, data.taxAmount));
        tvPositiveAdjustments.setText(String.format("%s%s", Constants.CURRENCY, data.adjustPositive));
        tvNegativeAdjustments.setText(String.format("%s%s", Constants.CURRENCY, data.adjustNegative));
        tvCancelOrders.setText(String.format("%s%s", Constants.CURRENCY, data.cancelOrder));
        tvTotalOrders.setText(String.format("%s%s", Constants.CURRENCY, data.totalOrders));
        tvGrossTotal.setText(String.format("%s%s", Constants.CURRENCY, data.grossTotal));
    }

    @OnClick(R.id.tvDateRange)
    public void pickDate() {
        View layout = LayoutInflater.from(mContext).inflate(R.layout.view_range_picker, null);

        tvToday = layout.findViewById(R.id.tvToday);
        tvYesterday = layout.findViewById(R.id.tvYesterday);
        tvLastWeek = layout.findViewById(R.id.tvLastWeek);
        tvLast30Days = layout.findViewById(R.id.tvLast30Days);
        tvThisMonth = layout.findViewById(R.id.tvThisMonth);
        tvPreviousMonth = layout.findViewById(R.id.tvPreviousMonth);
        tvCustomRange = layout.findViewById(R.id.tvCustomRange);

        popupWindow = new PopupWindow(layout, tvDateRange.getWidth() - 20, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Point point = getPointOfView(tvDateRange);
        popupWindow.showAtLocation(tvDateRange, Gravity.NO_GRAVITY, point.x, point.y + tvDateRange.getHeight());

        try {
            setListeners();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void setListeners() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        String defaultDate = sdf.format(calendar.getTime());
        Date date = sdf.parse(defaultDate);
        calendar.setTime(date);

        endDate = sdf.format(calendar.getTime());

        tvToday.setOnClickListener(view -> {
            startDate = Utils.getDateByRange(0);
            getReports(startDate, endDate);
            popupWindow.dismiss();
        });

        tvYesterday.setOnClickListener(view -> {
            startDate = Utils.getDateByRange(1);
            getReports(startDate, endDate);
            popupWindow.dismiss();
        });

        tvLastWeek.setOnClickListener(view -> {
            startDate = Utils.getDateByRange(6);
            getReports(startDate, endDate);
            popupWindow.dismiss();
        });

        tvLast30Days.setOnClickListener(view -> {
            startDate = Utils.getDaysAgo(-30);
            getReports(startDate, endDate);
            popupWindow.dismiss();
        });

        tvThisMonth.setOnClickListener(view -> {
            try {
                startDate = Utils.getFirstDateOfMonth();
                endDate = Utils.getLastDateOfMonth();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            getReports(startDate, endDate);
            popupWindow.dismiss();
        });

        tvPreviousMonth.setOnClickListener(view -> {
            startDate = Utils.getFirstDateOfPreviousMonth();
            endDate = Utils.getLastDateOfPreviousMonth();
            getReports(startDate, endDate);
            popupWindow.dismiss();
        });
    }

    private Point getPointOfView(View view) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        return new Point(location[0], location[1]);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbinder.unbind();
    }
}
