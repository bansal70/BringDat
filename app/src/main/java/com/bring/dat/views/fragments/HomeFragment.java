package com.bring.dat.views.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bring.dat.R;
import com.bring.dat.model.AppUtils;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.Operations;
import com.bring.dat.model.Utils;
import com.bring.dat.model.pojo.Order;
import com.bring.dat.model.pojo.OrdersResponse;
import com.bring.dat.views.adapters.HomeAdapter;
import com.bring.dat.views.adapters.NewOrdersAdapter;
import com.bring.dat.views.services.AlarmService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import retrofit2.Response;
import timber.log.Timber;

public class HomeFragment extends AppBaseFragment {

    Unbinder unbinder;

    View view;

    @BindView(R.id.llHome)
    LinearLayout llHome;

    @BindView(R.id.cardOrders)
    CardView cardOrders;

    @BindView(R.id.recyclerOrders)
    RecyclerView mRecyclerOrders;

    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    @BindView(R.id.tvPendingOrders)
    TextView tvPendingOrders;

    @BindView(R.id.tvCompletedOrders)
    TextView tvCompletedOrders;

    @BindView(R.id.tvNoOrders)
    TextView tvNoOrders;

    @BindView(R.id.swipeToRefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.viewPending)
    View viewPending;

    @BindView(R.id.viewWorking)
    View viewWorking;

    /*@BindView(R.id.tvNewOrders)
    TextView tvNewOrders;

    @BindView(R.id.tvWorkingOrders)
    TextView tvWorkingOrders;*/

    SwipeRefreshLayout.OnRefreshListener onRefreshListener;

    private List<Order> mPendingOrdersList, mWorkingOrdersList;
    private HomeAdapter mHomeAdapter;
    private NewOrdersAdapter mNewOrderAdapter;

    String token, restId;
    boolean isNewOrders = true, isPendingOrders = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view != null) {
            return view;
        }
        view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);

        initViews();
        return view;
    }

    private void initViews() {
        mRecyclerOrders.setLayoutManager(new LinearLayoutManager(mContext));

        /* String lastPrinter = BDPreferences.readString(mContext, Constants.LAST_PRINTER_CONNECTED);

        switch (lastPrinter) {
            case Constants.PRINTER_BLUETOOTH:
                if (!mActivity.isServiceRunning(BTService.class)) {
                    Intent btIntent = new Intent(mContext, BTService.class);
                    mActivity.startService(btIntent);
                }
                break;
            case Constants.PRINTER_WIFI:
                if (!mActivity.isServiceRunning(WFService.class)) {
                    Intent btIntent = new Intent(mContext, WFService.class);
                    mActivity.startService(btIntent);
                }
                break;
        } */

        cardOrders.setVisibility(BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER) ? View.GONE : View.VISIBLE);

        mPendingOrdersList = new ArrayList<>();
        mWorkingOrdersList = new ArrayList<>();

        mRecyclerOrders.setLayoutManager(new LinearLayoutManager(mContext));

        mNewOrderAdapter = new NewOrdersAdapter(mContext, mPendingOrdersList);
        mHomeAdapter = new HomeAdapter(mContext, mWorkingOrdersList);

        mRecyclerOrders.setAdapter(mNewOrderAdapter);

        restId = BDPreferences.readString(mContext, Constants.KEY_RESTAURANT_ID);
        token = BDPreferences.readString(mContext, Constants.KEY_TOKEN);

        onRefreshListener = this::getPendingOrders;

        mSwipeRefreshLayout.setOnRefreshListener(onRefreshListener);

        if (!mActivity.isInternetActive()) {
            connectionAlert();
            return;
        }
        getPendingOrders();
    }


    private void getPendingOrders() {
        mActivity.showDialog();
        apiService.getNewOrders(Operations.newOrdersParams(restId, token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {
                    serverError();
                })
                .doOnNext(this::setPendingOrders)
                .doOnError(this::serverError)
                .subscribe();
    }

    private void setPendingOrders(OrdersResponse mOrdersResponse) {
        if (mOrdersResponse.mAuthentication.equals(Constants.ERROR_AUTHENTICATION)) {
            AppUtils.openMain(mActivity);
            showToast(getString(R.string.error_session_expired));
            return;
        }

        BDPreferences.putString(mContext, Constants.KEY_RESTAURANT_NAME, mOrdersResponse.restaurantName);
        BDPreferences.putString(mContext, Constants.KEY_RESTAURANT_IMAGE, mOrdersResponse.restaurantLogo);

        mPendingOrdersList.clear();
        OrdersResponse.Data mOrdersData = mOrdersResponse.data;
        mPendingOrdersList.addAll(mOrdersData.orderList);
        mSwipeRefreshLayout.setRefreshing(false);

        getWorkingOrders();
    }

    private void getWorkingOrders() {
        apiService.getWorkingOrders(Operations.workingOrdersParams(restId, token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {
                    serverError();
                })
                .doOnNext(this::setWorkingOrders)
                .doOnError(this::serverError)
                .subscribe();
    }

    private void setWorkingOrders(OrdersResponse mOrdersResponse) {
        mSwipeRefreshLayout.setRefreshing(false);
        if (mOrdersResponse.mAuthentication.equals(Constants.ERROR_AUTHENTICATION)) {
            AppUtils.openMain(mActivity);
            showToast(getString(R.string.error_session_expired));
            return;
        }

        mProgressBar.setVisibility(View.GONE);
        mActivity.hideProgressBar();
        mActivity.dismissDialog();
        llHome.setVisibility(View.VISIBLE);

        if (!Utils.isServiceRunning(mContext, AlarmService.class)) {
            Intent alarmIntent = new Intent(mContext, AlarmService.class);
            mContext.startService(alarmIntent);
        }

        mWorkingOrdersList.clear();
        OrdersResponse.Data mOrdersData = mOrdersResponse.data;
        mWorkingOrdersList.addAll(mOrdersData.orderList);

        if (isNewOrders) {
            mNewOrderAdapter = new NewOrdersAdapter(mContext, mPendingOrdersList);
            mRecyclerOrders.setAdapter(mNewOrderAdapter);
            if (mPendingOrdersList.size() == 0) {
                tvNoOrders.setVisibility(View.VISIBLE);
                tvNoOrders.setText(getString(R.string.prompt_empty_pending_orders));
            } else {
                tvNoOrders.setVisibility(View.GONE);
            }
        }

        if (isPendingOrders) {
            mHomeAdapter = new HomeAdapter(mContext, mWorkingOrdersList);
            mRecyclerOrders.setAdapter(mHomeAdapter);

            if (mWorkingOrdersList.size() == 0) {
                tvNoOrders.setVisibility(View.VISIBLE);
                tvNoOrders.setText(getString(R.string.prompt_empty_working_orders));
            } else {
                tvNoOrders.setVisibility(View.GONE);
            }
        }

        tvPendingOrders.setText(String.valueOf(mPendingOrdersList.size()));
        tvCompletedOrders.setText(String.valueOf(mWorkingOrdersList.size()));

        /*if (!mOrdersResponse.success) {
            mHomeAdapter.notifyDataSetChanged();
        }*/
    }

    @OnClick(R.id.llNewOrders)
    public void newOrders() {
        isNewOrders = true;
        isPendingOrders = false;
       // tvNewOrders.setTextColor(ContextCompat.getColor(mContext, R.color.colorWhite));
       // tvNewOrders.setBackgroundColor(ContextCompat.getColor(mContext, R.color.orange));
       // tvWorkingOrders.setTextColor(ContextCompat.getColor(mContext, R.color.black));
       // tvWorkingOrders.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorWhite));
        viewPending.setVisibility(View.VISIBLE);
        viewWorking.setVisibility(View.GONE);

        mNewOrderAdapter = new NewOrdersAdapter(mContext, mPendingOrdersList);
        mRecyclerOrders.setAdapter(mNewOrderAdapter);

        tvNoOrders.setVisibility(mPendingOrdersList.size() == 0 ? View.VISIBLE : View.GONE);
        tvNoOrders.setText(getString(R.string.prompt_empty_pending_orders));
    }

    @OnClick(R.id.llWorkingOrders)
    public void workingOrders() {
        isNewOrders = false;
        isPendingOrders = true;

        viewPending.setVisibility(View.GONE);
        viewWorking.setVisibility(View.VISIBLE);
      //  tvNewOrders.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorWhite));
       // tvNewOrders.setTextColor(ContextCompat.getColor(mContext, R.color.black));
      //  tvWorkingOrders.setTextColor(ContextCompat.getColor(mContext, R.color.colorWhite));
      //  tvWorkingOrders.setBackgroundColor(ContextCompat.getColor(mContext, R.color.orange));

        mHomeAdapter = new HomeAdapter(mContext, mWorkingOrdersList);
        mRecyclerOrders.setAdapter(mHomeAdapter);

        tvNoOrders.setVisibility(mWorkingOrdersList.size() == 0 ? View.VISIBLE : View.GONE);
        tvNoOrders.setText(getString(R.string.prompt_empty_working_orders));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
       // super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 101) {
            if (!mActivity.isInternetActive()) {
                connectionAlert();
                return;
            }
            getPendingOrders();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        /*if (!mActivity.isInternetActive()) {
            connectionAlert();
            return;
        }
        getPendingOrders();*/
    }

    private void connectionAlert() {
        AlertDialog alert = Utils.createAlert(mActivity, getString(R.string.error_connection_down), getString(R.string.error_internet_disconnected));

        alert.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.prompt_retry), (dialogInterface, i) -> {
            if (!mActivity.isInternetActive()) {
                connectionAlert();
                return;
            }

            mSwipeRefreshLayout.setRefreshing(true);
            getPendingOrders();

        });
        alert.show();
    }

    public void serverError(Throwable throwable) {
        mActivity.dismissDialog();
        mActivity.hideProgressBar();
        mSwipeRefreshLayout.setRefreshing(false);
        showToast(getString(R.string.error_server));
        if (throwable instanceof HttpException) {
            Response<?> response = ((HttpException) throwable).response();
            Timber.e(response.message());
        }
    }

    public void serverError() {
        mActivity.dismissDialog();
        mActivity.hideProgressBar();
        mSwipeRefreshLayout.setRefreshing(false);
        showToast(getString(R.string.error_server));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbinder.unbind();
    }
}
