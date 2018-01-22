package com.bring.dat.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bring.dat.R;
import com.bring.dat.model.AppUtils;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.Utils;
import com.bring.dat.views.fragments.HomeFragment;
import com.bring.dat.views.fragments.ReportsFragment;
import com.bring.dat.views.fragments.SettingsFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends AppBaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbarTitle)
    TextView tvToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

       initNav();

       goToFragment(new HomeFragment());
    }

    private void initNav() {
        tvToolbar.setText(R.string.prompt_home);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Utils.hideKeyboard(mContext, getCurrentFocus());
            }
        };
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER)) {
            navigationView.getMenu().findItem(R.id.nav_setting).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_reports).setVisible(false);
        }

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment mFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_home:
                tvToolbar.setText(R.string.prompt_home);
                if (mFragment instanceof HomeFragment) {
                    break;
                }
                else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                }
                /*else if (!(mFragment instanceof HomeFragment)) {
                    goToFragment(new HomeFragment());
                }*/
                break;

            case R.id.nav_reports:
                if (mFragment instanceof ReportsFragment) {
                    break;
                } else {
                    tvToolbar.setText(getString(R.string.prompt_reports));
                    goToFragment(new ReportsFragment());
                }
                break;

            case R.id.nav_setting:
                if (mFragment instanceof SettingsFragment) {
                    break;
                } else {
                    tvToolbar.setText(getString(R.string.prompt_settings));
                    goToFragment(new SettingsFragment());
                }
                break;

            case R.id.nav_logout:
                AppUtils.logoutAlert(this);
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        //Utils.gotoNextActivityAnimation(mContext);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        boolean order = intent.getBooleanExtra("order", false);

        String details = intent.getStringExtra("orderDetails");
        goToFragment(new HomeFragment());
        /*OrderDetails mOrderDetails = new Gson().fromJson(details, OrderDetails.class);
        OrderDetails.Data data = mOrderDetails.data;
        Order mOrder = data.order.get(0);

        mOrdersList.set(0, mOrder);
        mOrder.order_print_status = "1";
        mHomeAdapter.notifyDataSetChanged();

        if (order) {

            fetchData();
            page = 0;
        }*/
    }

    private int count = 0;

    @Override
    public void onBackPressed() {
        Fragment mFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (mFragment instanceof HomeFragment) {
            new Handler().postDelayed(() -> count = 0, 2000);
            if (count == 0) {
                showToast(getString(R.string.prompt_back_again_exit));
                count++;
            } else if (count == 1) {
                super.onBackPressed();
            }
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //compositeDisposable.dispose();
    }
}