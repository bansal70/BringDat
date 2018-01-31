package com.bring.dat.views.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.Operations;
import com.bring.dat.model.Utils;
import com.bring.dat.model.pojo.Order;
import com.bring.dat.model.pojo.OrdersResponse;
import com.bring.dat.views.adapters.HomeAdapter;
import com.bring.dat.views.adapters.NewOrdersAdapter;
import com.bring.dat.views.services.BTService;

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

    @BindView(R.id.tvNewOrders)
    TextView tvNewOrders;

    @BindView(R.id.tvWorkingOrders)
    TextView tvWorkingOrders;

    SwipeRefreshLayout.OnRefreshListener onRefreshListener;

    private List<Order> mPendingOrdersList, mWorkingOrdersList;
    private HomeAdapter mHomeAdapter;
    private NewOrdersAdapter mNewOrderAdapter;

    String token, restId;

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

        if (!mActivity.isServiceRunning(BTService.class)) {
            Intent btIntent = new Intent(mContext, BTService.class);
            mActivity.startService(btIntent);
        }

        cardOrders.setVisibility(BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER) ? View.GONE : View.VISIBLE);

        mPendingOrdersList = new ArrayList<>();
        mWorkingOrdersList = new ArrayList<>();

        mRecyclerOrders.setLayoutManager(new LinearLayoutManager(mContext));

        mNewOrderAdapter = new NewOrdersAdapter(mContext, mPendingOrdersList);
        mHomeAdapter = new HomeAdapter(mContext, mWorkingOrdersList);

        mRecyclerOrders.setAdapter(mNewOrderAdapter);

        restId = BDPreferences.readString(mContext, Constants.KEY_RESTAURANT_ID);
        token = BDPreferences.readString(mContext, Constants.KEY_TOKEN);

        //getPendingOrders();

        onRefreshListener = this::getPendingOrders;

        mSwipeRefreshLayout.setOnRefreshListener(onRefreshListener);
    }


    private void getPendingOrders() {
        //mActivity.showProgressBar();
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
        if (mOrdersResponse.success) {
            mPendingOrdersList.clear();
            //llHome.setVisibility(View.VISIBLE);
            OrdersResponse.Data mOrdersData = mOrdersResponse.data;
            mPendingOrdersList.addAll(mOrdersData.orderList);
            mNewOrderAdapter.notifyDataSetChanged();
        } else {
            showToast(mOrdersResponse.msg);
            mSwipeRefreshLayout.setRefreshing(false);
        }
        getWorkingOrders();
        //mActivity.dismissDialog();
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

    private void setWorkingOrders(OrdersResponse mOrdersResponse) {
        mSwipeRefreshLayout.setRefreshing(false);
        mProgressBar.setVisibility(View.GONE);
        mActivity.hideProgressBar();
        llHome.setVisibility(View.VISIBLE);

        if (!mOrdersResponse.success) {
            tvPendingOrders.setText(String.valueOf(mPendingOrdersList.size()));
            tvCompletedOrders.setText(String.valueOf(mWorkingOrdersList.size()));
            return;
        }

        mWorkingOrdersList.clear();
        OrdersResponse.Data mOrdersData = mOrdersResponse.data;
        mWorkingOrdersList.addAll(mOrdersData.orderList);
        mHomeAdapter.notifyDataSetChanged();

        tvPendingOrders.setText(String.valueOf(mPendingOrdersList.size()));
        tvCompletedOrders.setText(String.valueOf(mWorkingOrdersList.size()));

        if (mPendingOrdersList.size() == 0) {
            tvNoOrders.setVisibility(View.VISIBLE);
            tvNoOrders.setText(getString(R.string.prompt_empty_pending_orders));
        }

        if (!mOrdersResponse.success) {
            showToast(mOrdersResponse.msg);
        }
    }

    @OnClick(R.id.tvNewOrders)
    public void newOrders() {
        tvNewOrders.setTextColor(ContextCompat.getColor(mContext, R.color.colorWhite));
        tvNewOrders.setBackgroundColor(ContextCompat.getColor(mContext, R.color.orange));
        tvWorkingOrders.setTextColor(ContextCompat.getColor(mContext, R.color.black));
        tvWorkingOrders.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorWhite));

        //mHomeAdapter = new HomeAdapter(mContext, mPendingOrdersList);
        mRecyclerOrders.setAdapter(mNewOrderAdapter);

        tvNoOrders.setVisibility(mPendingOrdersList.size() == 0 ? View.VISIBLE : View.GONE);
        tvNoOrders.setText(getString(R.string.prompt_empty_pending_orders));
    }

    @OnClick(R.id.tvWorkingOrders)
    public void workingOrders() {
        tvNewOrders.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorWhite));
        tvNewOrders.setTextColor(ContextCompat.getColor(mContext, R.color.black));
        tvWorkingOrders.setTextColor(ContextCompat.getColor(mContext, R.color.colorWhite));
        tvWorkingOrders.setBackgroundColor(ContextCompat.getColor(mContext, R.color.orange));

        //mHomeAdapter = new HomeAdapter(mContext, mWorkingOrdersList);
        mRecyclerOrders.setAdapter(mHomeAdapter);

        tvNoOrders.setVisibility(mWorkingOrdersList.size() == 0 ? View.VISIBLE : View.GONE);
        tvNoOrders.setText(getString(R.string.prompt_empty_working_orders));
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mActivity.isInternetActive()) {
            connectionAlert();
            return;
        }
        mSwipeRefreshLayout.setRefreshing(true);
        getPendingOrders();
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbinder.unbind();
    }
}
