package com.bring.dat.views;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.bring.dat.R;
import com.bring.dat.model.Application;
import com.bring.dat.model.ProgressBarHandler;
import com.bring.dat.model.Utils;
import com.bring.dat.model.network.APIClient;
import com.bring.dat.model.network.ApiService;
import com.bring.dat.views.services.BTService;
import com.bring.dat.views.services.BluetoothService;
import com.bring.dat.views.services.NetworkChangeReceiver;

import retrofit2.HttpException;
import retrofit2.Response;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.content.pm.PackageManager.GET_META_DATA;

public abstract class AppBaseActivity extends AppCompatActivity implements NetworkChangeReceiver.NetworkChangeReceiverListener{

    public Context mContext;
    private Dialog dialog;
    private ProgressBarHandler mProgressBarHandler;
    public final int PERMISSION_REQUEST_CODE = 1001;
    public final int REQUEST_IMAGE_CAPTURE = 1;
    public final int PERMISSION_LOCATION_CODE = 1021;
    public final int REQUEST_PRINT_RECEIPT = 3;
    public ApiService apiService;

    public static final int REQUEST_CONNECT_DEVICE = 1;
    public BluetoothService mService;
    NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mContext = AppBaseActivity.this;

        mProgressBarHandler = new ProgressBarHandler(this);
        dialog = Utils.showDialog(mContext);

        apiService = APIClient.getClient().create(ApiService.class);

        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(networkChangeReceiver, intentFilter);

        resetTitles();
        Application.getInstance().setConnectivityListener(this);

        mService = BTService.mService;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void showDialog() {
        dialog.show();
    }

    public void dismissDialog() {
        dialog.dismiss();
    }

    public void showProgressBar() {
        mProgressBarHandler.show();
    }

    public void hideProgressBar() {
        mProgressBarHandler.hide();
    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public boolean isInternetActive() {
        ConnectivityManager conMgr = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        assert conMgr != null;
        NetworkInfo info = conMgr.getActiveNetworkInfo();

        if (info != null && info.isConnected() && info.isAvailable()) {
            return true;
        } else {
            showToast(getString(R.string.error_internet_connection));
            return false;
        }
    }

    public void serverError(Throwable throwable) {
        dismissDialog();
        hideProgressBar();
        showToast(getString(R.string.error_server));
        if (throwable instanceof HttpException) {
            Response<?> response = ((HttpException) throwable).response();
            Timber.e(response.message());
        }
    }

    public void serverError() {
        dismissDialog();
        hideProgressBar();
        showToast(getString(R.string.error_server));
    }

    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void locationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != PERMISSION_REQUEST_CODE || !hasAllPermissionsGranted(grantResults)) {
            showToast(getString(R.string.error_permissions_denied));
        }

    }

    public boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    public void  goToFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack("");
        transaction.commitAllowingStateLoss();
    }

    public void  goToHomeFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack("Home").commitAllowingStateLoss();
    }

    private void resetTitles() {
        try {
            ActivityInfo info = getPackageManager().getActivityInfo(getComponentName(), GET_META_DATA);
            if (info.labelRes != 0) {
                setTitle(info.labelRes);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Utils.gotoPreviousActivityAnimation(mContext);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mItem) {
        switch (mItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(mItem);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Dialog dialog = Utils.createDialog(mContext, R.layout.dialog_internet);

        if (!isConnected) {
            if (!isFinishing() && !dialog.isShowing()) {
                dialog.show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(networkChangeReceiver);
    }
}