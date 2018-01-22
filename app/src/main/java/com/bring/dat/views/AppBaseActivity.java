package com.bring.dat.views;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.bring.dat.R;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.ProgressBarHandler;
import com.bring.dat.model.Utils;
import com.bring.dat.model.network.APIClient;
import com.bring.dat.model.network.ApiService;
import com.bring.dat.views.services.BTService;
import com.bring.dat.views.services.BluetoothService;

import java.lang.reflect.Method;

import retrofit2.HttpException;
import retrofit2.Response;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.content.pm.PackageManager.GET_META_DATA;

public abstract class AppBaseActivity extends AppCompatActivity {

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
    BluetoothDevice con_dev = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mContext = AppBaseActivity.this;

        mProgressBarHandler = new ProgressBarHandler(this);
        dialog = Utils.showDialog(mContext);

        apiService = APIClient.getClient().create(ApiService.class);

        resetTitles();
        fetchUUID();

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

    public void showSnackBar(View layout, String msg) {
        Snackbar.make(layout, msg, Snackbar.LENGTH_LONG).show();
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

    @SuppressLint("PrivateApi")
    public void fetchUUID() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        try {
            Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);

            ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(adapter, null);

            for (ParcelUuid uuid : uuids) {
                Timber.e("UUID: %s", uuid.getUuid().toString());
                BDPreferences.putString(mContext, "UUID", uuid.getUuid().toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                mService = BTService.mService;
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BDPreferences.putString(mContext, Constants.KEY_BT_ADDRESS, address);
                    mService.stop();
                    /*con_dev = mService.getDevByMac(address);

                    mService.connect(con_dev);*/
                }
                break;

            case REQUEST_PRINT_RECEIPT:
                if (resultCode == Activity.RESULT_OK) {
                    showToast("Receipt printed");
                } else {
                    showToast("Failed to print the receipt");
                }
                break;
        }
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
        transaction.commit();
    }

    public void  goToHomeFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack("Home").commit();
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


}