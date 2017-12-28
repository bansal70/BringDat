package com.bring.dat.views;

import android.app.Activity;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.bring.dat.R;
import com.bring.dat.model.AppUtils;
import com.bring.dat.model.Constants;
import com.bring.dat.model.ProgressBarHandler;
import com.bring.dat.model.Utils;
import com.bring.dat.model.network.APIClient;
import com.bring.dat.model.network.ApiService;
import com.bring.dat.model.pojo.Cart;
import com.bring.dat.model.pojo.Order;
import com.bring.dat.model.pojo.OrderDetails;
import com.zj.btsdk.BluetoothService;

import java.util.List;

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
    public ApiService apiService;

    public static final int REQUEST_ENABLE_BT = 2;
    public static final int REQUEST_CONNECT_DEVICE = 1;
    public BluetoothService mService = null;
    BluetoothDevice con_dev = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mContext = AppBaseActivity.this;

        mProgressBarHandler = new ProgressBarHandler(this);
        dialog = Utils.showDialog(mContext);
        //dialog.setCancelable(false);
        apiService = APIClient.getClient().create(ApiService.class);

        resetTitles();
        checkBT();
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
        showToast(getString(R.string.error_server));
        if (throwable instanceof HttpException) {
            Response<?> response = ((HttpException) throwable).response();
            Timber.e(response.message());
        }
    }

    public void serverError() {
        dismissDialog();
        showToast(getString(R.string.error_server));
    }

    public void checkBT() {
        mService = new BluetoothService(this, AppUtils.btHandler(mContext));

        if (!mService.isAvailable()) {
            Toast.makeText(this, R.string.error_bluetooth_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    public void enableBluetooth() {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }

    public void connectBT() {
        if (isBluetoothEnabled()) {
            con_dev = mService.getDevByMac(Constants.PRINTER_MAC_ADDRESS);
            mService.connect(con_dev);
        } else {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    public boolean isBluetoothEnabled() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter.isEnabled();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    showToast(getString(R.string.prompt_bluetooth_enabled));
                    con_dev = mService.getDevByMac(Constants.PRINTER_MAC_ADDRESS);
                    mService.connect(con_dev);
                } else {
                    showToast(getString(R.string.error_bt_canceled));
                }
                break;
        }
    }

    public void printText(String txt, char type){
        byte[] format = { 27, 33, 0 };
        byte[] arrayOfByte1 = { 27, 33, 0 };

        if (type == 'b') {
            format[2] = ((byte) (0x8 | arrayOfByte1[2])); //BOLD
        }
        if (type == 'h') {
            format[2] = ((byte) (0x10 | arrayOfByte1[2])); //HEIGHT
        }
        if (type == 'w') {
            format[2] = ((byte) (0x20 | arrayOfByte1[2])); //WIDTH
        }
        if (type == 'u') {
            format[2] = ((byte) (0x80 | arrayOfByte1[2])); //UNDERLINE
        }
        if (type == 's') {
            format[2] = ((byte) (0x1 | arrayOfByte1[2])); //SMALL
        }

        mService.write(format);
        mService.sendMessage(txt,"GBK");
    }

    public void printLeft(String txt) {
        byte[] format = new byte[]{0x1B, 'a', 0x00};
        mService.write(format);
        mService.sendMessage(txt,"GBK");
    }

    public void printCenter(String txt) {
        byte[] format = new byte[]{0x1B, 'a', 0x01};
        mService.write(format);
        mService.sendMessage(txt,"GBK");
    }

    public void printRight(String txt) {
        byte[] format = new byte[]{0x1B, 'a', 0x02};
        mService.write(format);
        mService.sendMessage(txt,"GBK");
    }

    public void printMultiAlign(byte[] align, String msg){
        try {
            mService.write(align);
            StringBuilder space = new StringBuilder("   ");
            int l = msg.length();
            if(l < 31){
                for(int x = 31-l; x >= 0; x--) {
                    space.append(" ");
                }
            }
            msg = msg.replace(" : ", space.toString());
            mService.write( msg.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printOrderReceipt(OrderDetails mOrderDetails) {
        String msg = "";
        String DIVIDER = "--------------------------------";
        String DIVIDER_DOUBLE = "================================";
        String BREAK = "\n";
        String SPACE5 = "     ";
        String SPACE4 = "    ";

        OrderDetails.Data data = mOrderDetails.data;
        Order mOrder = data.order.get(0);
        List<Cart> mCartList = data.cart;

        msg = DIVIDER + BREAK;
        printCenter(msg);

        msg = "DELIVERY" + BREAK + DIVIDER + BREAK;
        printCenter(msg);

        msg = mOrder.ordergenerateid + SPACE5 + mOrder.deliverydate + " " + mOrder.orderdate + BREAK;
        printCenter(msg);

        msg = mOrder.orderdeliverydate + BREAK + DIVIDER_DOUBLE + BREAK;
        printCenter(msg);

        msg = mOrder.customername + " " + mOrder.customerlastname + BREAK;
        printCenter(msg);

        msg = String.format("%s, %s, %s", mOrder.cityName, mOrder.deliverystate, mOrder.deliveryzip) + "," + BREAK + BREAK;
        printCenter(msg);

        msg = mOrder.customercellphone + BREAK + BREAK + DIVIDER_DOUBLE + BREAK;
        printCenter(msg);

        msg = "Qty" + SPACE4 + "Item";
        printLeft(msg);

        msg = "Price" + BREAK;
        printRight(msg);

        for (int i = 0; i < mCartList.size(); i++) {
            Cart mCart = mCartList.get(i);
            msg = mCart.qty + "   " + mCart.item;
            printLeft(msg);

            msg = Constants.CURRENCY + mCart.price + BREAK;
            printRight(msg);
        }

        msg = BREAK + BREAK + BREAK;
        msg += "Subtotal:";
        printLeft(msg);

        msg = Constants.CURRENCY + mOrder.ordersubtotal + BREAK;
        printRight(msg);

        msg = "Tax(" + mOrder.taxvalue + "%):";
        printLeft(msg);

        msg = Constants.CURRENCY + mOrder.taxamount + BREAK;
        printRight(msg);

        msg = "Delivery Charge:";
        printLeft(msg);

        msg = Constants.CURRENCY + mOrder.deliveryamount + BREAK;
        printRight(msg);

        msg = "Convenience Fee:";
        printLeft(msg);

        msg = Constants.CURRENCY + mOrder.convenienceFee + BREAK;
        printRight(msg);


        if (!mOrder.siteDiscountAmount.isEmpty() || !mOrder.siteDiscountAmount.equals("0")) {
            msg = "bringDat Discount Yea! (" + mOrder.siteDiscountPercent + "% :)";
            printLeft(msg);

            msg = mOrder.siteDiscountAmount + BREAK;
            printRight(msg);
        }

        msg = "Tip:";
        printLeft(msg);

        msg = Constants.CURRENCY + mOrder.tipamount + BREAK;
        printRight(msg);

        msg = "Total:";
        printLeft(msg);

        msg = Constants.CURRENCY + mOrder.ordertotalprice + BREAK + BREAK;
        printRight(msg);

        msg = mOrder.paymentType + BREAK + BREAK + BREAK + DIVIDER;
        printCenter(msg);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

     /*   if (requestCode == PERMISSION_REQUEST_CODE && hasAllPermissionsGranted(grantResults)) {
        } else {
            Toast.makeText(this, R.string.grant_permissions, Toast.LENGTH_SHORT).show();
        }*/

    }

    public boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
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