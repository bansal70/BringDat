package com.bring.dat.model;

/*
 * Created by rishav on 1/3/2018.
 */

import android.content.Context;

import com.bring.dat.model.network.APIClient;
import com.bring.dat.model.network.ApiService;
import com.bring.dat.model.pojo.Cart;
import com.bring.dat.model.pojo.Order;
import com.bring.dat.model.pojo.OrderDetails;
import com.bring.dat.views.services.BTService;
import com.bring.dat.views.services.BluetoothService;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import retrofit2.Response;
import timber.log.Timber;

public class PrintReceipt {

    private static void writeWithFormat(byte[] buffer, final byte[] pFormat, final byte[] pAlignment) {
        BluetoothService mService = BTService.mService;
        try {
            // Notify printer it should be printed with given alignment:
            mService.write(pAlignment);
            // Notify printer it should be printed in the given format:
            mService.write(pFormat);
            // Write the actual data:
            mService.write(buffer);

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
        writeWithFormat(msg.getBytes(), new Formatter().bold().height().get(), Formatter.centerAlign());

        msg = mOrder.merchantAddress + BREAK;
        writeWithFormat(msg.getBytes(), new Formatter().bold().height().get(), Formatter.centerAlign());

        msg = mOrder.merchantPhone + BREAK;
        writeWithFormat(msg.getBytes(), new Formatter().bold().height().get(), Formatter.centerAlign());

        msg = DIVIDER + BREAK;
        writeWithFormat(msg.getBytes(), new Formatter().bold().get(), Formatter.centerAlign());

        msg = "DELIVERY" + DIVIDER + BREAK;
        writeWithFormat(msg.getBytes(), new Formatter().bold().get(), Formatter.centerAlign());

        msg = mOrder.ordergenerateid + SPACE5 + mOrder.deliverydate + " " + mOrder.orderdate ;
        writeWithFormat(msg.getBytes(), new Formatter().get(), Formatter.centerAlign());

        msg = mOrder.orderdeliverydate + BREAK + DIVIDER_DOUBLE + BREAK;
        writeWithFormat(msg.getBytes(), new Formatter().get(), Formatter.centerAlign());

        msg = mOrder.customername + " " + mOrder.customerlastname + BREAK;
        writeWithFormat(msg.getBytes(), new Formatter().get(), Formatter.centerAlign());

        msg = String.format("%s, %s, %s", mOrder.cityName, mOrder.deliverystate, mOrder.deliveryzip) + "," + BREAK;
        writeWithFormat(msg.getBytes(), new Formatter().get(), Formatter.centerAlign());

        msg = mOrder.customercellphone + BREAK + DIVIDER_DOUBLE;
        writeWithFormat(msg.getBytes(), new Formatter().get(), Formatter.centerAlign());

        msg = "Qty" + SPACE4 + "Item";
        writeWithFormat(msg.getBytes(), new Formatter().get(), Formatter.leftAlign());

        msg = "Price";
        writeWithFormat(msg.getBytes(), new Formatter().get(), Formatter.rightAlign());

        for (int i = 0; i < mCartList.size(); i++) {
            Cart mCart = mCartList.get(i);
            msg = " " + mCart.qty + "   " + mCart.item;
            writeWithFormat(msg.getBytes(), new Formatter().get(), Formatter.leftAlign());

            msg = Constants.CURRENCY + mCart.price;
            writeWithFormat(msg.getBytes(), new Formatter().get(), Formatter.rightAlign());
        }

        msg = BREAK + "Subtotal:";
        writeWithFormat(msg.getBytes(), new Formatter().get(), Formatter.leftAlign());

        msg = Constants.CURRENCY + mOrder.ordersubtotal ;
        writeWithFormat(msg.getBytes(), new Formatter().get(), Formatter.rightAlign());

        msg = "Tax(" + mOrder.taxvalue + "%):";
        writeWithFormat(msg.getBytes(), new Formatter().get(), Formatter.leftAlign());

        msg = Constants.CURRENCY + mOrder.taxamount;
        writeWithFormat(msg.getBytes(), new Formatter().get(), Formatter.rightAlign());

        msg = "Delivery Charge:";
        writeWithFormat(msg.getBytes(), new Formatter().get(), Formatter.leftAlign());

        msg = Constants.CURRENCY + mOrder.deliveryamount;
        writeWithFormat(msg.getBytes(), new Formatter().get(), Formatter.rightAlign());

        msg = "Convenience Fee:";
        writeWithFormat(msg.getBytes(), new Formatter().get(), Formatter.leftAlign());

        msg = Constants.CURRENCY + mOrder.convenienceFee;
        writeWithFormat(msg.getBytes(), new Formatter().get(), Formatter.rightAlign());


        if (!mOrder.siteDiscountAmount.isEmpty() || !mOrder.siteDiscountAmount.equals("0")) {
            msg = "Discount(" + mOrder.siteDiscountPercent + "% :)";
            writeWithFormat(msg.getBytes(), new Formatter().get(), Formatter.leftAlign());

            msg = Constants.CURRENCY + mOrder.siteDiscountAmount;
            writeWithFormat(msg.getBytes(), new Formatter().get(), Formatter.rightAlign());
        }

        msg = "Tip:";
        writeWithFormat(msg.getBytes(), new Formatter().get(), Formatter.leftAlign());

        msg = Constants.CURRENCY + mOrder.tipamount;
        writeWithFormat(msg.getBytes(), new Formatter().get(), Formatter.rightAlign());

        msg = "Total:";
        writeWithFormat(msg.getBytes(), new Formatter().bold().height().get(), Formatter.leftAlign());

        msg = Constants.CURRENCY + mOrder.ordertotalprice + BREAK + DIVIDER_DOUBLE;
        writeWithFormat(msg.getBytes(), new Formatter().get(), Formatter.rightAlign());

        msg = mOrder.paymentType + BREAK + DIVIDER_DOUBLE + BREAK + BREAK + BREAK;
        writeWithFormat(msg.getBytes(), new Formatter().get(), Formatter.centerAlign());

       // receiptPrinted(mContext, mOrder.orderid);
    }

    private static void receiptPrinted(Context mContext, String orderID) {
        ApiService apiService = APIClient.getClient().create(ApiService.class);
        String token = BDPreferences.readString(mContext, Constants.KEY_TOKEN);

        apiService.getReceipt(Operations.receiptPrintParams(orderID, token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {})
                .doOnError(PrintReceipt::serverError)
                .subscribe();
    }

    private static void serverError(Throwable throwable) {
        if (throwable instanceof HttpException) {
            Response<?> response = ((HttpException) throwable).response();
            Timber.e(response.message());
        }
    }
}
