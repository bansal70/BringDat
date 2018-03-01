package com.bring.dat.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bring.dat.BuildConfig;
import com.bring.dat.R;
import com.bring.dat.model.AppUtils;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.NetworkPrinting;
import com.bring.dat.model.PrintReceipt;
import com.bring.dat.model.Utils;
import com.bring.dat.model.pojo.OrderDetails;
import com.bring.dat.views.fragments.HistoryFragment;
import com.bring.dat.views.fragments.HomeFragment;
import com.bring.dat.views.fragments.ReportsFragment;
import com.bring.dat.views.fragments.SettingsFragment;
import com.bring.dat.views.services.BTService;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends AppBaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbarTitle)
    TextView tvToolbar;

    @BindView(R.id.tvAppVersion)
    TextView tvAppVersion;

    NetworkPrinting networkPrinting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        networkPrinting = new NetworkPrinting(this);

        initNav();

        if (BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER))
            goToHomeFragment(new HistoryFragment());
        else
            goToHomeFragment(new HomeFragment());
    }

    public class HeaderViewHolder {
        @BindView(R.id.ivLogo)
        ImageView imgLogo;

        @BindView(R.id.tvRestaurant)
        TextView tvRestaurant;

        HeaderViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    private void initNav() {
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        toggle.setDrawerIndicatorEnabled(false);
        toggle.setHomeAsUpIndicator(R.drawable.ic_menu);
        toggle.setToolbarNavigationClickListener(view -> {
            drawerLayout.openDrawer(GravityCompat.START);
            setMenuData();
        });

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER)) {
            navigationView.getMenu().findItem(R.id.nav_home).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_setting).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_reports).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_history).setTitle(R.string.prompt_home);
            tvToolbar.setText(getString(R.string.prompt_logger));
            navigationView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            toolbar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        } else {
            navigationView.getMenu().findItem(R.id.nav_switch).setVisible(false);
            tvToolbar.setText(getString(R.string.prompt_admin));
            navigationView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorAccent));
            toolbar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        }

        tvAppVersion.setText(String.format("%s %s %s", getString(R.string.prompt_version), BuildConfig.VERSION_NAME, getString(R.string.prompt_version_date)));

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setMenuData() {
        View header = navigationView.getHeaderView(0);
        HeaderViewHolder headerViewHolder = new HeaderViewHolder(header);
        String restaurant = BDPreferences.readString(mContext, Constants.KEY_RESTAURANT_NAME);
        String image = BDPreferences.readString(mContext, Constants.KEY_RESTAURANT_IMAGE);
        if (!TextUtils.isEmpty(restaurant)) {
            headerViewHolder.tvRestaurant.setText(restaurant);
            Utils.loadImage(mContext, Constants.IMAGE_BASE_URL+image, headerViewHolder.imgLogo);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment mFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_home:
                if (BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER)) {
                    tvToolbar.setText(getString(R.string.prompt_logger));
                } else {
                    tvToolbar.setText(getString(R.string.prompt_admin));
                }

                if (mFragment instanceof HomeFragment) {
                    break;
                } else {
                    //goToHomeFragment(new HomeFragment());
                    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        getSupportFragmentManager().popBackStack("", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    }
                }
                break;

            case R.id.nav_history:
                if (mFragment instanceof HistoryFragment) {
                    break;
                } else {
                    if (BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER))
                        tvToolbar.setText(getString(R.string.prompt_home));
                    else
                        tvToolbar.setText(getString(R.string.prompt_history));
                        goToFragment(new HistoryFragment());
                }
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

            case R.id.nav_switch:
                if (BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER)) {
                    BDPreferences.putString(mContext, Constants.KEY_LOGIN_TYPE, Constants.LOGIN_ADMIN);
                } else {
                    BDPreferences.putString(mContext, Constants.KEY_LOGIN_TYPE, Constants.LOGIN_LOGGER);
                }

                Intent intent = new Intent(mContext, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                break;

            case R.id.nav_logout:
                AppUtils.logoutAlert(this);
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        setMenuData();
    }

    @OnClick(R.id.btLogout)
    public void logoutUser() {
        drawerLayout.closeDrawer(GravityCompat.START);
        AppUtils.logoutAlert(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String json = intent.getStringExtra(Constants.ORDER_DETAILS);
        if (json != null) {
            OrderDetails mOrderDetails = new Gson().fromJson(json, OrderDetails.class);
            if (BDPreferences.readBoolean(mContext, Constants.AUTO_PRINT_TYPE)) {
                if (!BDPreferences.readString(mContext, Constants.KEY_IP_ADDRESS).isEmpty()) {
                    if (!networkPrinting.isPrinted) {
                        Utils.showToast(mContext, getString(R.string.processing_last_receipt));
                        return;
                    }
                    networkPrinting.printData(this, mOrderDetails);
                } else if (Utils.isServiceRunning(mContext, BTService.class)) {
                    PrintReceipt.printOrderReceipt(mContext, mOrderDetails);
                } else {
                    Utils.showToast(mContext, mContext.getString(R.string.error_printer_unavailable));
                }
            }
        }

        if (BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER)) {
            goToHomeFragment(new HistoryFragment());
        } else {
            goToHomeFragment(new HomeFragment());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment mFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (mFragment != null) {
            mFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        Fragment mFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER)) {
            if (mFragment instanceof HistoryFragment) {
                exitApp();
            }
        }
        else if (mFragment instanceof HomeFragment) {
            exitApp();
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack("", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }
    }

    private int count = 0;

    private void exitApp() {
        new Handler().postDelayed(() -> count = 0, 2000);
        if (count == 0) {
            showToast(getString(R.string.prompt_back_again_exit));
            count++;
        } else if (count == 1) {
            super.onBackPressed();
        }
    }

}
