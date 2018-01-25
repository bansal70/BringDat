package com.bring.dat.views.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.bring.dat.model.pojo.Order;
import com.bring.dat.model.pojo.OrdersResponse;
import com.bring.dat.views.adapters.HomeAdapter;
import com.bring.dat.views.adapters.NewOrdersAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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

        /*if (!mActivity.isServiceRunning(BTService.class)) {
            Intent btIntent = new Intent(mContext, BTService.class);
            mActivity.startService(btIntent);
        }*/

        cardOrders.setVisibility(BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER) ? View.GONE : View.VISIBLE);

        mPendingOrdersList = new ArrayList<>();
        mWorkingOrdersList = new ArrayList<>();

        mRecyclerOrders.setLayoutManager(new LinearLayoutManager(mContext));

        mNewOrderAdapter = new NewOrdersAdapter(mContext, mPendingOrdersList);
        mHomeAdapter = new HomeAdapter(mContext, mWorkingOrdersList);

        mRecyclerOrders.setAdapter(mNewOrderAdapter);

        restId = BDPreferences.readString(mContext, Constants.KEY_RESTAURANT_ID);
        token = BDPreferences.readString(mContext, Constants.KEY_TOKEN);

        getPendingOrders();

        onRefreshListener = this::getPendingOrders;

        mSwipeRefreshLayout.setOnRefreshListener(onRefreshListener);
    }


    private void getPendingOrders() {
        mActivity.showProgressBar();
        apiService.getNewOrders(Operations.newOrdersParams(restId, token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {
                    mActivity.serverError();
                })
                .doOnNext(this::setPendingOrders)
                .doOnError(mActivity::serverError)
                .subscribe();
    }

    private void setPendingOrders(OrdersResponse mOrdersResponse) {
        if (mOrdersResponse.success) {
            mPendingOrdersList.clear();
            //llHome.setVisibility(View.VISIBLE);
            OrdersResponse.Data mOrdersData = mOrdersResponse.data;
            mPendingOrdersList.addAll(mOrdersData.orderList);
            mNewOrderAdapter.notifyDataSetChanged();
            getWorkingOrders();
        } else {
            mActivity.hideProgressBar();
            showToast(mOrdersResponse.msg);
            mSwipeRefreshLayout.setRefreshing(false);
        }
        //mActivity.dismissDialog();
    }

    private void getWorkingOrders() {
        apiService.getWorkingOrders(Operations.workingOrdersParams(restId, token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {
                    mActivity.serverError();
                })
                .doOnNext(this::setWorkingOrders)
                .doOnError(mActivity::serverError)
                .subscribe();
    }

    private void setWorkingOrders(OrdersResponse mOrdersResponse) {
        if (!mOrdersResponse.success) {
            showToast(mOrdersResponse.msg);
            return;
        }
        llHome.setVisibility(View.VISIBLE);
        mSwipeRefreshLayout.setRefreshing(false);
        mWorkingOrdersList.clear();
        OrdersResponse.Data mOrdersData = mOrdersResponse.data;
        mWorkingOrdersList.addAll(mOrdersData.orderList);
        mHomeAdapter.notifyDataSetChanged();

        mProgressBar.setVisibility(View.GONE);
        mActivity.hideProgressBar();

        tvPendingOrders.setText(String.valueOf(mPendingOrdersList.size()));
        tvCompletedOrders.setText(String.valueOf(mWorkingOrdersList.size()));
    }

    @OnClick(R.id.tvNewOrders)
    public void newOrders() {
        tvNewOrders.setTextColor(ContextCompat.getColor(mContext, R.color.colorWhite));
        tvNewOrders.setBackgroundColor(ContextCompat.getColor(mContext, R.color.orange));
        tvWorkingOrders.setTextColor(ContextCompat.getColor(mContext, R.color.black));
        tvWorkingOrders.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorWhite));

        //mHomeAdapter = new HomeAdapter(mContext, mPendingOrdersList);
        mRecyclerOrders.setAdapter(mNewOrderAdapter);
    }

    @OnClick(R.id.tvWorkingOrders)
    public void workingOrders() {
        tvNewOrders.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorWhite));
        tvNewOrders.setTextColor(ContextCompat.getColor(mContext, R.color.black));
        tvWorkingOrders.setTextColor(ContextCompat.getColor(mContext, R.color.colorWhite));
        tvWorkingOrders.setBackgroundColor(ContextCompat.getColor(mContext, R.color.orange));

        //mHomeAdapter = new HomeAdapter(mContext, mWorkingOrdersList);
        mRecyclerOrders.setAdapter(mHomeAdapter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbinder.unbind();
    }
}
