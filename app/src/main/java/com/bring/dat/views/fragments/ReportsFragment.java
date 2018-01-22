package com.bring.dat.views.fragments;

import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.bring.dat.views.adapters.ReportsDateAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ReportsFragment extends AppBaseFragment{

    Unbinder unbinder;

    @BindView(R.id.reportsLL)
    LinearLayout reportsLL;

    @BindView(R.id.cardDate)
    CardView cardDate;

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

    PopupWindow popupWindow;
    ReportsDateAdapter reportsDateAdapter;
    ViewGroup parent;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        unbinder = ButterKnife.bind(this, view);

        parent = container;
        initDefault();

        return view;
    }

    private void initDefault() {
        endDate = Utils.getCurrentDate();
        startDate = Utils.getDaysAgo(0);
        getReports(startDate, endDate);
        setReportsBy();
    }

    private void getReports(String startDate, String endDate) {
        tvDateRange.setText(String.format("%s - %s", Utils.getFullDate(startDate), Utils.getFullDate(endDate)));

        restId = BDPreferences.readString(mContext, Constants.KEY_RESTAURANT_ID);
        token = BDPreferences.readString(mContext, Constants.KEY_TOKEN);

        mActivity.showDialog();
        Disposable disposable = apiService.getReports(Operations.reportParams(restId, startDate, endDate, token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {
                    mActivity.serverError();
                })
                .doOnNext(this::setReportsData)
                .doOnError(mActivity::serverError)
                .subscribe();

        compositeDisposable.add(disposable);
    }

    private void setReportsData(Reports reports) {
        mActivity.dismissDialog();

        if (!reports.success) {
            showToast(reports.msg);
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
        tvCancelOrders.setText(data.cancelOrder);
        tvTotalOrders.setText(data.totalOrders);
        tvGrossTotal.setText(String.format("%s%s", Constants.CURRENCY, data.grossTotal));
    }

    @OnClick(R.id.cardDate)
    public void pickDate() {
        if (popupWindow != null) {
            Point point = Utils.getPointOfView(tvDateRange);
            popupWindow.setWidth(cardDate.getWidth() - 10);
            popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
            popupWindow.showAtLocation(parent, Gravity.NO_GRAVITY, point.x, point.y + tvDateRange.getHeight());
        }
    }

    private void setReportsBy() {
        View mLayout = LayoutInflater.from(mContext).inflate(R.layout.view_range_picker, parent, false);

        RecyclerView recyclerView = mLayout.findViewById(R.id.recyclerReports);
        CardView mCardReports = mLayout.findViewById(R.id.cardReports);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setHasFixedSize(true);
        mCardReports.setBackgroundResource(R.drawable.ic_tooltip);

        popupWindow = new PopupWindow(mContext);
        popupWindow.setContentView(mLayout);
        popupWindow.setFocusable(true);

        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        reportsDateAdapter = new ReportsDateAdapter(mContext, popupWindow);
        recyclerView.setAdapter(reportsDateAdapter);

        reportsDateAdapter.setOnDataChangeListener(this::getReports);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbinder.unbind();
        mActivity.hideProgressBar();
        compositeDisposable.dispose();
    }
}
