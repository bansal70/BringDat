package com.bring.dat.model;

/*
 * Created by rishav on 1/3/2018.
 */

import android.content.Context;
import android.content.Intent;

import com.bring.dat.model.network.APIClient;
import com.bring.dat.model.network.ApiService;
import com.bring.dat.model.pojo.Cart;
import com.bring.dat.model.pojo.Order;
import com.bring.dat.model.pojo.OrderDetails;
import com.bring.dat.views.OrdersListActivity;
import com.bring.dat.views.services.BTService;
import com.bring.dat.views.services.BluetoothService;
import com.google.gson.Gson;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import retrofit2.Response;
import timber.log.Timber;

public class PrintReceipt {

    private static void printLeft(String txt) {
        BluetoothService mService = BTService.mService;
        byte[] format = new byte[]{0x1B, 'a', 0x00};
        mService.write(format);
        mService.sendMessage(txt,"GBK");
    }

    private static void printCenter(String txt) {
        BluetoothService mService = BTService.mService;
        byte[] format = new byte[]{0x1B, 'a', 0x01};
        mService.write(format);
        mService.sendMessage(txt,"GBK");
    }

    private static void printRight(String txt) {
        BluetoothService mService = BTService.mService;
        byte[] format = new byte[]{0x1B, 'a', 0x02};
        mService.write(format);
        mService.sendMessage(txt,"GBK");
    }

    private static void writeWithFormat(String message, byte[] buffer, final byte[] pFormat, final byte[] pAlignment) {
        BluetoothService mService = BTService.mService;
        try {
            // Notify printer it should be printed with given alignment:
            mService.write(pAlignment);
            // Notify printer it should be printed in the given format:
            mService.write(pFormat);
            // Write the actual data:
            mService.sendMessage(message,"GBK");
            //mService.write(buffer);

        } catch (Exception e) {
            Timber.e(e, "Exception during write");
        }
    }

    public static void printOrderReceipt(Context mContext, OrderDetails mOrderDetails) {
        String msg;
        String DIVIDER = "--------------------------------";
        String DIVIDER_DOUBLE = "================================";
        String BREAK = "\n";
        String SPACE5 = "     ";
        String SPACE4 = "    ";

        OrderDetails.Data data = mOrderDetails.data;
        Order mOrder = data.order.get(0);
        List<Cart> mCartList = data.cart;

        msg = mOrder.merchantName + BREAK;
        writeWithFormat(msg, msg.getBytes(), new Formatter().bold().height().get(), Formatter.centerAlign());

        msg = mOrder.merchantAddress + BREAK;
        writeWithFormat(msg, msg.getBytes(), new Formatter().bold().height().get(), Formatter.centerAlign());

        msg = mOrder.merchantPhone + BREAK;
        writeWithFormat(msg, msg.getBytes(), new Formatter().bold().height().get(), Formatter.centerAlign());

        msg = DIVIDER + BREAK;
        writeWithFormat(msg, msg.getBytes(), new Formatter().bold().get(), Formatter.centerAlign());

        msg = "DELIVERY" + BREAK + DIVIDER + BREAK;
        writeWithFormat(msg, msg.getBytes(), new Formatter().bold().get(), Formatter.centerAlign());

        msg = mOrder.ordergenerateid + SPACE5 + mOrder.deliverydate + " " + mOrder.orderdate + BREAK;
        writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.centerAlign());

        msg = mOrder.orderdeliverydate + BREAK + DIVIDER_DOUBLE + BREAK;
        writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.centerAlign());

        msg = mOrder.customername + " " + mOrder.customerlastname + BREAK;
        writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.centerAlign());

        msg = AppUtils.userAddress(mOrder) + "," + BREAK;
        writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.centerAlign());

        msg = mOrder.customercellphone + BREAK + DIVIDER_DOUBLE;
        writeWithFormat(msg, msg.getBytes(), new Formatter().bold().get(), Formatter.centerAlign());

        msg = "Qty" + SPACE4 + "Item";
        printLeft(msg);
        //writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.leftAlign());

        msg = SPACE5 + "Price" + BREAK;
        printRight(msg);
        //writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.rightAlign());

        for (int i = 0; i < mCartList.size(); i++) {
            Cart mCart = mCartList.get(i);
            msg = " " + mCart.qty + "   " + mCart.item;
            printLeft(msg);
            //writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.leftAlign());

            msg = Constants.CURRENCY + mCart.price + BREAK;
            printRight(msg);
            //writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.rightAlign());
        }

        msg = BREAK + "Subtotal:";
        printLeft(msg);
        //writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.leftAlign());

        msg = SPACE5 + Constants.CURRENCY + mOrder.ordersubtotal + BREAK;
        printRight(msg);
        //writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.rightAlign());

        msg = "Tax(" + mOrder.taxvalue + "%):";
        printLeft(msg);
        //writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.leftAlign());

        msg = SPACE5 + Constants.CURRENCY + mOrder.taxamount  + BREAK;
        printRight(msg);
        // writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.rightAlign());

        msg = "Delivery Charge:";
        printLeft(msg);
        //writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.leftAlign());

        msg = SPACE5 + Constants.CURRENCY + mOrder.deliveryamount + BREAK;
        printRight(msg);
        //writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.rightAlign());

        msg = "Convenience Fee:";
        printLeft(msg);
        //writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.leftAlign());

        msg = SPACE5 + Constants.CURRENCY + mOrder.convenienceFee + BREAK;
        printRight(msg);
        //writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.rightAlign());

        if (!mOrder.siteDiscountAmount.isEmpty() || !mOrder.siteDiscountAmount.equals("0")) {
            msg = "Discount(" + mOrder.siteDiscountPercent + "%):";
            printLeft(msg);
            //writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.leftAlign());

            msg = SPACE5 + Constants.CURRENCY + mOrder.siteDiscountAmount + BREAK;
            printRight(msg);
            // writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.rightAlign());
        }

        msg = "Tip:";
        printLeft(msg);
        //writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.leftAlign());

        msg = SPACE5 + Constants.CURRENCY + mOrder.tipamount + BREAK;
        printRight(msg);
        // writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.rightAlign());

        msg = "Total:";
        printLeft(msg);
        //writeWithFormat(msg, msg.getBytes(), new Formatter().bold().get(), Formatter.leftAlign());

        msg = SPACE5 + Constants.CURRENCY + mOrder.ordertotalprice + BREAK + DIVIDER_DOUBLE;
        printRight(msg);
        //writeWithFormat(msg, msg.getBytes(), new Formatter().bold().get(), Formatter.rightAlign());

        msg = BREAK + BREAK + BREAK + mOrder.paymentType + BREAK + BREAK + BREAK + BREAK + DIVIDER_DOUBLE + BREAK + BREAK + BREAK;
        writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.centerAlign());

       receiptPrinted(mContext, mOrder.orderid);
       //startMain(mContext, mOrderDetails);
    }

    private static void receiptPrinted(Context mContext, String orderID) {
        ApiService apiService = APIClient.getClient().create(ApiService.class);
         String token = BDPreferences.readString(mContext, Constants.KEY_TOKEN);

        apiService.getReceipt(Operations.receiptPrintParams(orderID, token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {})
                .doOnNext(orderDetails -> {

                })
                .doOnError(PrintReceipt::serverError)
                .subscribe();
    }

    private static void serverError(Throwable throwable) {
        if (throwable instanceof HttpException) {
            Response<?> response = ((HttpException) throwable).response();
            Timber.e(response.message());
        }
    }

    private static void startMain(Context mContext, OrderDetails mOrderDetails) {
        Intent intent = new Intent(mContext, OrdersListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("orderDetails", new Gson().toJson(mOrderDetails));
        mContext.startActivity(intent);
    }
}
