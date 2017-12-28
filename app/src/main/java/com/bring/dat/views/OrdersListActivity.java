package com.bring.dat.views;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bring.dat.R;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.Operations;
import com.bring.dat.model.RecyclerPagination;
import com.bring.dat.model.Utils;
import com.bring.dat.model.pojo.Order;
import com.bring.dat.model.pojo.OrdersResponse;
import com.bring.dat.views.adapters.OrdersListAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;

public class OrdersListActivity extends AppBaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbarTitle)
    TextView tvTitle;

    @BindView(R.id.listOrders)
    RecyclerView mListOrders;

    private List<Order> mOrdersList;
    private OrdersListAdapter mOrdersListAdapter;
    private int page = 0;

    private PublishProcessor<Integer> pagination;
    private CompositeDisposable compositeDisposable;
    public boolean requestOnWay = false;
    String token, restId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_list);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle.setText(getString(R.string.title_activity_orders));

        initViews();
        //checkBT();
    }

    private void initViews() {
        mOrdersList = new ArrayList<>();
        mListOrders.setLayoutManager(new LinearLayoutManager(mContext));
        mOrdersListAdapter = new OrdersListAdapter(mContext, mOrdersList);
        mListOrders.setAdapter(mOrdersListAdapter);

        restId = BDPreferences.readString(mContext, Constants.KEY_RESTAURANT_ID);
        token = BDPreferences.readString(mContext, Constants.KEY_TOKEN);

        pagination = PublishProcessor.create();
        compositeDisposable = new CompositeDisposable();

        mListOrders.addOnScrollListener(new RecyclerPagination(mListOrders.getLayoutManager()) {
            @Override
            public void onLoadMore(int currentPage, int totalItemCount, View view) {
                if (!requestOnWay) {
                    if (isInternetActive()) {
                        pagination.onNext(page);
                    }
                }
            }
        });

        fetchData();
    }

    private void setOrdersList(OrdersResponse ordersResponse) {
        if (ordersResponse.success) {
            OrdersResponse.Data mOrdersData = ordersResponse.data;
            mOrdersList.addAll(mOrdersData.orderList);
            mOrdersListAdapter.notifyDataSetChanged();
        } else if (page > 1){
            showToast(getString(R.string.error_no_more_orders));
        } else {
            showToast(ordersResponse.msg);
        }

        requestOnWay = false;
        dismissDialog();
    }

    private void fetchData() {
        if (!isInternetActive()) {
            return;
        }
        showDialog();

        Disposable disposable = pagination.onBackpressureDrop()
                .doOnNext(page -> requestOnWay = true)
                .concatMap(integer -> getOrders())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {
                   serverError();
                })
                .doOnNext(this::setOrdersList)
                .doOnError(this::serverError)
                .subscribe();

        compositeDisposable.add(disposable);
        pagination.onNext(page);
    }

    private Flowable<OrdersResponse> getOrders() {
        return apiService.getOrders(Operations.ordersListParams(restId, page++, token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logout, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mItem) {
        switch (mItem.getItemId()) {
            case R.id.logout:
                Utils.logoutAlert(this);
                break;
        }
        return super.onOptionsItemSelected(mItem);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mService != null && !mService.isBTopen()) {
            enableBluetooth();
        } else {
            connectBT();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}
