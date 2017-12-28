package com.bring.dat.model;

/*
 * Created by rishav on 12/27/2017.
 */

import android.content.Context;
import android.os.Handler;

import com.bring.dat.R;
import com.bring.dat.model.pojo.Cart;
import com.bring.dat.model.pojo.Order;
import com.bring.dat.model.pojo.OrderDetails;
import com.zj.btsdk.BluetoothService;

import java.util.List;

import retrofit2.HttpException;
import retrofit2.Response;
import timber.log.Timber;

public class AppUtils {

    public static void serverError(Throwable throwable) {
        if (throwable instanceof HttpException) {
            Response<?> response = ((HttpException) throwable).response();
            Timber.e(response.message());
        }
    }

    public static Handler btHandler(Context mContext) {
        return new Handler(msg -> {
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Utils.showToast(mContext, mContext.getString(R.string.success_connection));
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Timber.e("Bluetooth is connecting");
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            Timber.e("Bluetooth state listen or none");
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST:
                    Utils.showToast(mContext, mContext.getString(R.string.error_connection_lost));
                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT:
                    Utils.showToast(mContext, mContext.getString(R.string.error_connection_failed));
                    break;
            }

            return false;
        });
    }

    public static String headerOrderReceipt(OrderDetails mOrderDetails) {
        String header = "";
        String BREAK = "\n";
        OrderDetails.Data data = mOrderDetails.data;
        Order mOrder = data.order.get(0);

        header += mOrder.merchantName + BREAK;
        header += mOrder.merchantAddress + BREAK;
        header += mOrder.merchantPhone + BREAK;

        return header;
    }

    public static String makeOrderReceipt(OrderDetails mOrderDetails) {
        String msg = "";
        String DIVIDER = "--------------------------------";
        String DIVIDER_DOUBLE = "================================";
        String BREAK = "\n";
        String SPACE5 = "     ";
        String SPACE4 = "    ";

        OrderDetails.Data data = mOrderDetails.data;
        Order mOrder = data.order.get(0);
        List<Cart> mCartList = data.cart;

        msg += DIVIDER + BREAK;
        msg += "DELIVERY" + BREAK;
        msg += DIVIDER + BREAK;
        msg += mOrder.ordergenerateid + SPACE5 + mOrder.deliverydate + " " + mOrder.orderdate + BREAK;
        msg += mOrder.orderdeliverydate + BREAK;
        msg += DIVIDER_DOUBLE + BREAK;
        msg += mOrder.customername + " " + mOrder.customerlastname + BREAK;
        msg += String.format("%s, %s, %s", mOrder.cityName, mOrder.deliverystate, mOrder.deliveryzip) + "," + BREAK + BREAK;
        msg += mOrder.customercellphone + BREAK + BREAK;
        msg += DIVIDER_DOUBLE + BREAK;
        msg += "Qty" + SPACE4 + "Item" + SPACE5 + SPACE5 + SPACE5 + "Price" + BREAK;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mCartList.size(); i++) {
            Cart mCart = mCartList.get(i);
            sb.append(mCart.qty).append(SPACE4).append(mCart.item).append(SPACE5).append(SPACE5).append(Constants.CURRENCY).append(mCart.price).append(BREAK);
            //msg += mCart.qty + SPACE4 + mCart.item + SPACE5 + SPACE5 + SPACE5 + Constants.CURRENCY + mCart.price + BREAK;
        }
        msg += sb.toString();
        msg += BREAK + BREAK + BREAK;
        msg += "Subtotal:" + SPACE5 + Constants.CURRENCY + mOrder.ordersubtotal + BREAK;
        msg += "Tax(" + mOrder.taxvalue + "%):" + SPACE5 + Constants.CURRENCY + mOrder.taxamount + BREAK;
        msg += "Delivery Charge:" + SPACE5 + Constants.CURRENCY + mOrder.deliveryamount + BREAK;
        msg += "Convenience Fee:" + SPACE5 + Constants.CURRENCY + mOrder.convenienceFee + BREAK;

        if (!mOrder.siteDiscountAmount.isEmpty() || !mOrder.siteDiscountAmount.equals("0"))
            msg += "bringDat Discount Yea! (" + mOrder.siteDiscountPercent + "%)" + ":" + SPACE5 + mOrder.siteDiscountAmount + BREAK;

        msg += "Tip:" + SPACE5 + Constants.CURRENCY + mOrder.tipamount + BREAK;
        msg += "Total:" + SPACE5 + Constants.CURRENCY + mOrder.ordertotalprice + BREAK + BREAK;
        msg += SPACE5 + SPACE5 + SPACE5 + mOrder.paymentType + BREAK + BREAK + BREAK + BREAK + BREAK + DIVIDER;

        return msg;
    }
}
