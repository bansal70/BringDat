package com.bring.dat.model;

/*
 * Created by rishav on 12/27/2017.
 */

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

import com.bring.dat.R;
import com.bring.dat.model.pojo.Cart;
import com.bring.dat.model.pojo.Order;
import com.bring.dat.model.pojo.OrderDetails;
import com.bring.dat.views.MainActivity;
import com.bring.dat.views.services.AlarmService;
import com.bring.dat.views.services.BTService;
import com.bring.dat.views.services.BluetoothService;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import retrofit2.HttpException;
import retrofit2.Response;
import timber.log.Timber;

public class AppUtils {
    private static BluetoothService mService;

    public static void logoutAlert(final Activity mActivity) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mActivity);
        alertBuilder.setMessage(R.string.alert_logout);

        alertBuilder
                .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> {
                    dialogInterface.dismiss();

                    openMain(mActivity);
                    Utils.showToast(mActivity, mActivity.getString(R.string.prompt_logged_out));
                })
                .setNegativeButton(android.R.string.no, (dialogInterface, i) -> dialogInterface.dismiss());

        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    public static void openMain(Activity mActivity) {
        BDPreferences.clearPref(mActivity);
        mActivity.startActivity(new Intent(mActivity, MainActivity.class));
        mActivity.finish();

        Intent intent = new Intent(mActivity, BTService.class);
        mActivity.stopService(intent);

        Intent alarmIntent = new Intent(mActivity, AlarmService.class);
        mActivity.stopService(alarmIntent);
    }

    public static void serverError(Throwable throwable) {
        if (throwable instanceof HttpException) {
            Response<?> response = ((HttpException) throwable).response();
            Timber.e(response.message());
        }
    }

    public static String userAddress(Order mOrder) {
        String address = "";

        if (!mOrder.deliverystreet.isEmpty()) {
            address = mOrder.deliverystreet + "\n";
        }
        if (!mOrder.deliverydoornumber.isEmpty()) {
            address += "Apt/Suite/Bldg# " + mOrder.deliverydoornumber + "\n";
        }
        if (!mOrder.deliverylandmark.isEmpty()) {
            address += mOrder.deliverylandmark + ", ";
        }
        if (!mOrder.deliveryarea.isEmpty()) {
            address += mOrder.deliveryarea + ", ";
        }
        if (!mOrder.cityName.isEmpty()) {
            address += mOrder.cityName + ", ";
        }
        if (!mOrder.deliverystate.isEmpty()) {
            address += mOrder.deliverystate + ", ";
        }
        if (!mOrder.deliveryzip.isEmpty()) {
            address += mOrder.deliveryzip;
        }

        return address;
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

    private static void printLeft(String txt) {
        mService = BTService.mService;
        byte[] format = new byte[]{0x1B, 'a', 0x00};
        mService.write(format);
        mService.sendMessage(txt,"GBK");
    }

    private static void printCenter(String txt) {
        mService = BTService.mService;
        byte[] format = new byte[]{0x1B, 'a', 0x01};
        mService.write(format);
        mService.sendMessage(txt,"GBK");
    }

    private static void printRight(String txt) {
        mService = BTService.mService;
        byte[] format = new byte[]{0x1B, 'a', 0x02};
        mService.write(format);
        mService.sendMessage(txt,"GBK");
    }

    private static void printCenterBold(String txt) {
        byte[] arrayOfByte1 = { 27, 33, 0 };
        mService = BTService.mService;
        byte[] format = new byte[]{0x1B, 'a', 0x01};
        format[2] = ((byte) (0x8 | arrayOfByte1[2]));
        mService.write(format);
        mService.sendMessage(txt,"GBK");
    }

    public static void printOrderReceipt(OrderDetails mOrderDetails) {

        mService = BTService.mService;
        String msg = "";
        String DIVIDER = "--------------------------------";
        String DIVIDER_DOUBLE = "================================";
        String BREAK = "\n";
        String SPACE5 = "     ";
        String SPACE4 = "    ";

        OrderDetails.Data data = mOrderDetails.data;
        Order mOrder = data.order.get(0);
        List<Cart> mCartList = data.cart;

        msg = mOrder.merchantName + BREAK;
        printCenterBold(msg);
        msg = mOrder.merchantAddress + BREAK;
        printCenterBold(msg);
        msg = mOrder.merchantPhone + BREAK;
        printCenterBold(msg);

        msg = DIVIDER;
        printCenter(msg);

        msg = "DELIVERY" + DIVIDER ;
        printCenter(msg);

        msg = mOrder.ordergenerateid + SPACE5 + mOrder.deliverydate + " " + mOrder.orderdate ;
        printCenter(msg);

        msg = mOrder.orderdeliverydate + DIVIDER_DOUBLE;
        printCenter(msg);

        msg = mOrder.customername + " " + mOrder.customerlastname;
        printCenter(msg);

        msg = String.format("%s, %s, %s", mOrder.cityName, mOrder.deliverystate, mOrder.deliveryzip) + "," + BREAK;
        printCenter(msg);

        msg = mOrder.customercellphone + BREAK + DIVIDER_DOUBLE;
        printCenter(msg);

        msg = "Qty" + SPACE4 + "Item";
        printLeft(msg);

        msg = "Price";
        printRight(msg);

        for (int i = 0; i < mCartList.size(); i++) {
            Cart mCart = mCartList.get(i);
            msg = mCart.qty + "   " + mCart.item;
            printLeft(msg);

            msg = Constants.CURRENCY + mCart.price;
            printRight(msg);
        }

        msg = BREAK;
        msg += "Subtotal:";
        printLeft(msg);

        msg = Constants.CURRENCY + mOrder.ordersubtotal ;
        printRight(msg);

        msg = "Tax(" + mOrder.taxvalue + "%):";
        printLeft(msg);

        msg = Constants.CURRENCY + mOrder.taxamount;
        printRight(msg);

        msg = "Delivery Charge:";
        printLeft(msg);

        msg = Constants.CURRENCY + mOrder.deliveryamount;
        printRight(msg);

        msg = "Convenience Fee:";
        printLeft(msg);

        msg = Constants.CURRENCY + mOrder.convenienceFee;
        printRight(msg);


        if (!mOrder.siteDiscountAmount.isEmpty() || !mOrder.siteDiscountAmount.equals("0")) {
            msg = "Discount(" + mOrder.siteDiscountPercent + "% :)";
            printLeft(msg);

            msg = Constants.CURRENCY + mOrder.siteDiscountAmount;
            printRight(msg);
        }

        msg = "Tip:";
        printLeft(msg);

        msg = Constants.CURRENCY + mOrder.tipamount;
        printRight(msg);

        msg = "Total:";
        printLeft(msg);

        msg = Constants.CURRENCY + mOrder.ordertotalprice;
        printRight(msg);

        msg = mOrder.paymentType + BREAK + DIVIDER;
        printCenter(msg);
    }

    public void printOrderReceipt(Activity mContext, OrderDetails mOrderDetails) {
        String DIVIDER = "<LINE";
        String DIVIDER_DOUBLE = "<DLINE>";
        String BREAK = "<BR>";
        String SPACE5 = "     ";
        String SPACE4 = "    ";
        String CENTER = "<CENTER>";
        String LEFT = "<LEFT>";
        String RIGHT = "<RIGHT>";
        String BOLD = "<BOLD>";
        String NORMAL = "<NORMAL>";
        String CUT = "<CUT>";

        OrderDetails.Data data = mOrderDetails.data;
        Order mOrder = data.order.get(0);
        List<Cart> mCartList = data.cart;

        StringBuilder command;
        command = new StringBuilder(CENTER + BOLD + mOrder.merchantName + BREAK);
        command.append(CENTER).append(BOLD).append(mOrder.merchantAddress).append(BREAK);
        command.append(CENTER).append(BOLD).append(mOrder.merchantPhone).append(BREAK);

        command.append(NORMAL).append(DIVIDER).append(BREAK).append(CENTER).append("DELIVERY").append(BREAK).append(DIVIDER).append(BREAK);
        command.append(CENTER).append(mOrder.ordergenerateid).append(SPACE5).append(mOrder.deliverydate).append(" ").append(mOrder.orderdate).append(BREAK);
        command.append(CENTER).append(mOrder.orderdeliverydate).append(DIVIDER_DOUBLE).append(BREAK);
        command.append(CENTER).append(mOrder.customername).append(" ").append(mOrder.customerlastname).append(BREAK);
        command.append(CENTER).append(String.format("%s, %s, %s", mOrder.cityName, mOrder.deliverystate, mOrder.deliveryzip)).append(",").append(BREAK);
        command.append(CENTER).append(mOrder.customercellphone).append(BREAK).append(DIVIDER_DOUBLE).append(BREAK);
        command.append(LEFT).append("Qty").append(SPACE4).append("Item").append(RIGHT).append("Price").append(BREAK);

        for (int i = 0; i < mCartList.size(); i++) {
            Cart mCart = mCartList.get(i);
            command.append(LEFT).append(mCart.qty).append("   ").append(mCart.item);
            command.append(RIGHT).append(Constants.CURRENCY).append(mCart.price).append(BREAK);
        }

        command.append(LEFT).append("Subtotal:");

        command.append(RIGHT).append(Constants.CURRENCY).append(mOrder.ordersubtotal).append(BREAK);

        command.append(LEFT).append("Tax(").append(mOrder.taxvalue).append("%):");
        command.append(RIGHT).append(Constants.CURRENCY).append(mOrder.taxamount).append(BREAK);

        command.append(LEFT).append("Delivery Charge:");

        command.append(RIGHT).append(Constants.CURRENCY).append(mOrder.deliveryamount).append(BREAK);

        command.append(LEFT).append("Convenience Fee:");

        command.append(RIGHT).append(Constants.CURRENCY).append(mOrder.convenienceFee).append(BREAK);


        if (!mOrder.siteDiscountAmount.isEmpty() || !mOrder.siteDiscountAmount.equals("0")) {
            command.append(LEFT).append("bringDat Discount Yea! (").append(mOrder.siteDiscountPercent).append("%:)");
            command.append(RIGHT).append(Constants.CURRENCY).append(mOrder.siteDiscountAmount).append(BREAK);
        }

        command.append(LEFT).append("Tip:");
        command.append(RIGHT).append(Constants.CURRENCY).append(mOrder.tipamount).append(BREAK);

        command.append(LEFT).append("Total:");

        command.append(RIGHT).append(Constants.CURRENCY).append(mOrder.ordertotalprice).append(BREAK);
        command.append(mOrder.paymentType).append(BREAK).append(DIVIDER).append(BREAK).append(CUT);

        printReceipt(mContext, command.toString());
    }

    private void printReceipt(Activity mActivity, String textToPrint) {
        try {
            Intent intent = new Intent("pe.diegoveloper.printing");
            intent.setType("text/plain");
            intent.putExtra(android.content.Intent.EXTRA_TEXT, textToPrint);
            mActivity.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=pe.diegoveloper.printerserverapp"));
            mActivity.startActivity(intent);
        }
    }

    public static BigDecimal roundTwoDecimal(float price) {
        BigDecimal bill = new BigDecimal(price);
        return bill.setScale(2, RoundingMode.HALF_UP);
    }

    public static String makeErrorMessage(Context mContext, PrinterStatusInfo status) {
        String msg = "";

        if (status.getOnline() == Printer.FALSE) {
            msg += mContext.getString(R.string.handlingmsg_err_offline);
        }
        if (status.getConnection() == Printer.FALSE) {
            msg += mContext.getString(R.string.handlingmsg_err_no_response);
        }
        if (status.getCoverOpen() == Printer.TRUE) {
            msg += mContext.getString(R.string.handlingmsg_err_cover_open);
        }
        if (status.getPaper() == Printer.PAPER_EMPTY) {
            msg += mContext.getString(R.string.handlingmsg_err_receipt_end);
        }
        if (status.getPaperFeed() == Printer.TRUE || status.getPanelSwitch() == Printer.SWITCH_ON) {
            msg += mContext.getString(R.string.handlingmsg_err_paper_feed);
        }
        if (status.getErrorStatus() == Printer.MECHANICAL_ERR || status.getErrorStatus() == Printer.AUTOCUTTER_ERR) {
            msg += mContext.getString(R.string.handlingmsg_err_autocutter);
            msg += mContext.getString(R.string.handlingmsg_err_need_recover);
        }
        if (status.getErrorStatus() == Printer.UNRECOVER_ERR) {
            msg += mContext.getString(R.string.handlingmsg_err_unrecover);
        }
        if (status.getErrorStatus() == Printer.AUTORECOVER_ERR) {
            if (status.getAutoRecoverError() == Printer.HEAD_OVERHEAT) {
                msg += mContext.getString(R.string.handlingmsg_err_overheat);
                msg += mContext.getString(R.string.handlingmsg_err_head);
            }
            if (status.getAutoRecoverError() == Printer.MOTOR_OVERHEAT) {
                msg += mContext.getString(R.string.handlingmsg_err_overheat);
                msg += mContext.getString(R.string.handlingmsg_err_motor);
            }
            if (status.getAutoRecoverError() == Printer.BATTERY_OVERHEAT) {
                msg += mContext.getString(R.string.handlingmsg_err_overheat);
                msg += mContext.getString(R.string.handlingmsg_err_battery);
            }
            if (status.getAutoRecoverError() == Printer.WRONG_PAPER) {
                msg += mContext.getString(R.string.handlingmsg_err_wrong_paper);
            }
        }
        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_0) {
            msg += mContext.getString(R.string.handlingmsg_err_battery_real_end);
        }

        return msg;
    }

    public static void displayPrinterWarnings(Context mContext, PrinterStatusInfo status) {
        String warningsMsg = "";

        if (status == null) {
            return;
        }

        if (status.getPaper() == Printer.PAPER_NEAR_END) {
            warningsMsg += mContext.getString(R.string.handlingmsg_warn_receipt_near_end);
        }

        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_1) {
            warningsMsg += mContext.getString(R.string.handlingmsg_warn_battery_near_end);
        }

        Utils.showToast(mContext, warningsMsg);
    }

}
