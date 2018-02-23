package com.bring.dat.views.fragments;

import android.app.Dialog;
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
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.Operations;
import com.bring.dat.model.RecyclerPagination;
import com.bring.dat.model.Utils;
import com.bring.dat.model.pojo.Order;
import com.bring.dat.model.pojo.OrdersResponse;
import com.bring.dat.views.adapters.HomeAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import retrofit2.Response;
import timber.log.Timber;

public class HistoryFragment extends AppBaseFragment {

    Unbinder unbinder;

    View view;

    @BindView(R.id.llHome)
    LinearLayout llHome;

    @BindView(R.id.cardOrders)
    CardView cardOrders;

    @BindView(R.id.cardOrders2)
    CardView cardOrders2;

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

    SwipeRefreshLayout.OnRefreshListener onRefreshListener;

    private List<Order> mOrdersList;
    private HomeAdapter mHomeAdapter;
    private int page = 0;

    private PublishProcessor<Integer> pagination;
    private CompositeDisposable compositeDisposable;
    public boolean requestOnWay = false;
    String token, restId;

    AlertDialog alert;

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

        /*String lastPrinter = BDPreferences.readString(mContext, Constants.LAST_PRINTER_CONNECTED);

        switch (lastPrinter) {
            case Constants.PRINTER_BLUETOOTH:
                if (!mActivity.isServiceRunning(BTService.class)) {
                    Intent btIntent = new Intent(mContext, BTService.class);
                    mActivity.startService(btIntent);
                }
                break;
            case Constants.PRINTER_WIFI:
                if (!mActivity.isServiceRunning(WFService.class)) {
                    Intent wfIntent = new Intent(mContext, WFService.class);
                    mActivity.startService(wfIntent);
                }
                break;
        }*/

        cardOrders.setVisibility(View.GONE);
        cardOrders2.setVisibility(View.GONE);

        mOrdersList = new ArrayList<>();

        mRecyclerOrders.setLayoutManager(new LinearLayoutManager(mContext));
        mHomeAdapter = new HomeAdapter(mContext, mOrdersList);
        mRecyclerOrders.setAdapter(mHomeAdapter);

        restId = BDPreferences.readString(mContext, Constants.KEY_RESTAURANT_ID);
        token = BDPreferences.readString(mContext, Constants.KEY_TOKEN);

        pagination = PublishProcessor.create();
        compositeDisposable = new CompositeDisposable();

        mSwipeRefreshLayout.setOnRefreshListener(onRefreshListener);

        mRecyclerOrders.addOnScrollListener(new RecyclerPagination(mRecyclerOrders.getLayoutManager()) {
            @Override
            public void onLoadMore(int currentPage, int totalItemCount, View view) {
                if (!requestOnWay) {
                    if (mActivity.isInternetActive()) {
                        pagination.onNext(page);
                        mProgressBar.setVisibility(View.VISIBLE);
                    } else {
                        connectionAlert(false);
                    }
                }
            }
        });

        fetchData();

        onRefreshListener = () -> {
            pagination = PublishProcessor.create();
            compositeDisposable = new CompositeDisposable();
            mOrdersList.clear();
            mHomeAdapter.notifyDataSetChanged();
            page = 0;
            fetchData();
        };

        mSwipeRefreshLayout.setOnRefreshListener(onRefreshListener);
    }

    private void connectionAlert(boolean isFirstTime) {
        alert = Utils.createAlert(mActivity, getString(R.string.error_connection_down), getString(R.string.error_internet_disconnected));

        alert.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.prompt_retry), (dialogInterface, i) -> {
            if (isFirstTime) {
                fetchData();
            } else {
                if (!mActivity.isInternetActive()) {
                    connectionAlert(false);
                    return;
                }
                pagination.onNext(page);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });
        alert.show();
    }

    private void setOrdersList(OrdersResponse mOrdersResponse) {
        if (mOrdersResponse.success) {
            llHome.setVisibility(View.VISIBLE);
            OrdersResponse.Data mOrdersData = mOrdersResponse.data;
            if (BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER)) {
                mOrdersList.addAll(mOrdersData.orderList);
            } else {
                for (Order order : mOrdersData.orderList) {
                    if (!(order.status.equals("pending") || order.status.contains("process"))) {
                        Timber.e(order.status);
                        mOrdersList.add(order);
                    }
                }
            }

            if (mOrdersList.size() == 0) {
                pagination.onNext(page);
                mActivity.showDialog();
            }

            mHomeAdapter.notifyDataSetChanged();

            int mPendingOrders = 0;
            if (!mOrdersResponse.totalPending.isEmpty()) {
                mPendingOrders = Integer.parseInt(mOrdersResponse.totalPending);
            }
            if (!mOrdersResponse.totalWorking.isEmpty()) {
                mPendingOrders += Integer.parseInt(mOrdersResponse.totalWorking);
            }

            tvPendingOrders.setText(String.valueOf(mPendingOrders));
            tvCompletedOrders.setText(mOrdersResponse.totalCompleted);
        } else if (page > 1) {
            showToast(getString(R.string.error_no_more_orders));
        } else {
            showToast(mOrdersResponse.msg);
        }
        requestOnWay = false;

        mProgressBar.setVisibility(View.GONE);

        mActivity.dismissDialog();
    }

    private void fetchData() {
        if (!mActivity.isInternetActive()) {
            connectionAlert(true);
            return;
        }

        mActivity.showDialog();

        Disposable disposable = pagination.onBackpressureDrop()
                .doOnNext(page -> requestOnWay = true)
                .concatMap(integer -> getOrders())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {
                    responseError();
                })
                .doOnNext(this::setOrdersList)
                .doOnError(this::responseError)
                .subscribe();

        compositeDisposable.add(disposable);
        pagination.onNext(page);
    }

    private Flowable<OrdersResponse> getOrders() {
        mSwipeRefreshLayout.setRefreshing(false);
        return apiService.getOrders(Operations.ordersListParams(restId, page++, token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    void responseError(Throwable throwable) {
        mActivity.dismissDialog();
        showToast(getString(R.string.error_server));
        mSwipeRefreshLayout.setRefreshing(false);
        mProgressBar.setVisibility(View.GONE);
        if (throwable instanceof HttpException) {
            Response<?> response = ((HttpException) throwable).response();
            Timber.e(response.message());
        }
    }

    void responseError() {
        mActivity.dismissDialog();
        showToast(getString(R.string.error_server));
        mSwipeRefreshLayout.setRefreshing(false);
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mActivity.dismissDialog();
        unbinder.unbind();

        compositeDisposable.dispose();
    }
}
