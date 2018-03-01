package com.bring.dat.model;

/*
 * Created by rishav on 2/23/2018.
 */

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.bring.dat.R;
import com.bring.dat.model.pojo.Cart;
import com.bring.dat.model.pojo.Order;
import com.bring.dat.model.pojo.OrderDetails;
import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;

import java.util.List;

import timber.log.Timber;

public class NetworkPrinting implements ReceiveListener {
    private Printer mPrinter;
    private Activity mActivity;
    public boolean isPrinted = true;

    public NetworkPrinting(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public boolean printData(Activity mActivity, OrderDetails mOrderDetails) {
        isPrinted = false;
        if (!initializeObject(mActivity)) {
            return false;
        }
        if (!createReceiptData(mActivity, mOrderDetails)) {
            return false;
        }

        if (!printData()) {
            finalizeObject();
            return false;
        }

        return true;
    }

    private static String leftRightAlign(String str1, String str2) {
        String txt = str1 + str2;
        if (txt.length() < 31) {
            int n = (31 - (str1.length() + str2.length()));
            txt = str1 + new String(new char[n]).replace("\0", " ") + str2;
        }
        return txt;
    }

    private boolean createReceiptData(Context mContext, OrderDetails mOrderDetails) {
        OrderDetails.Data data = mOrderDetails.data;
        Order mOrder = data.order.get(0);
        List<Cart> mCartList = data.cart;
        String BREAK = "\n";
        String SPACE5 = "     ";
        String SPACE4 = "    ";
        String itemLeft, itemRight;
        String DIVIDER_DOUBLE = "================================";

        StringBuilder textData = new StringBuilder();
        if (mPrinter == null)
            return false;

        try {
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);

            boldText(true);
            mPrinter.addTextSize(2, 2);
            textData.append("ONLINE ORDER").append(BREAK);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(1);

            boldText(false);
            mPrinter.addTextSize(1, 1);
            textData.append(mOrder.merchantName).append(BREAK);
            textData.append(mOrder.merchantAddress).append(BREAK);
            textData.append(mOrder.merchantPhone).append(BREAK);
            Timber.e(textData.toString());
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            boldText(false);
            mPrinter.addTextSize(1, 1);
            textData.append(DIVIDER_DOUBLE).append(BREAK);
            mPrinter.addFeedLine(1);
            Timber.e(textData.toString());
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            boldText(true);
            mPrinter.addTextSize(2, 2);
            if (mOrder.deliverytype.equalsIgnoreCase("delivery")) {
                textData.append("DELIVERY").append(BREAK);
            } else {
                textData.append("PICKUP").append(BREAK);
            }
            Timber.e(textData.toString());
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            boldText(false);
            mPrinter.addTextSize(1, 1);
            textData.append(DIVIDER_DOUBLE).append(BREAK);
            textData.append(mOrder.ordergenerateid).append(SPACE5).append(mOrder.deliverydate)
                    .append(" ").append(mOrder.orderdate).append(BREAK);
            Timber.e(textData.toString());
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            boldText(true);
            mPrinter.addTextSize(1, 2);
            if (mOrder.deliverytime.equalsIgnoreCase("ASAP")) {
                textData.append(mOrder.orderdeliverydate).append(BREAK);
            } else {
                colorText(true);
                textData.append("Ready By: ").append(Utils.parseDateToMMdd(mOrder.deliverydate)).append(" ")
                        .append(mOrder.deliverytime).append(BREAK);
            }
            Timber.e(textData.toString());
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            colorText(false);
            boldText(false);
            mPrinter.addTextSize(1, 1);
            textData.append(DIVIDER_DOUBLE).append(BREAK);
            textData.append(mOrder.customername).append(" ").append(mOrder.customerlastname).append(BREAK);
            textData.append(BREAK);
            textData.append(AppUtils.userAddress(mOrder)).append(BREAK);
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            boldText(true);
            mPrinter.addTextSize(1, 2);
            textData.append(mOrder.customercellphone).append(BREAK);
            Timber.e(textData.toString());
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            boldText(false);
            mPrinter.addTextSize(1, 1);
            textData.append(DIVIDER_DOUBLE).append(BREAK);
            itemLeft = "Qty" + SPACE4 + "Item";
            itemRight = "Price";

            textData.append(leftRightAlign(itemLeft, itemRight));
            textData.append(BREAK);

            for (int i = 0; i < mCartList.size(); i++) {
                Cart mCart = mCartList.get(i);
                float totalPrice = Float.parseFloat(mCart.qty) * mCart.price;
                itemLeft = " " + mCart.qty + "   " + decode(mCart.item);
                itemRight = Constants.CURRENCY + AppUtils.roundTwoDecimal(totalPrice);
                textData.append(leftRightAlign(itemLeft, itemRight));
                textData.append(BREAK);
                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());

                if (!TextUtils.isEmpty(mCart.specialinstruction)) {
                    colorText(true);
                    textData.append("Special Instructions: ").append(mCart.specialinstruction).append(BREAK);
                }
                mPrinter.addText(textData.toString());
                textData.delete(0, textData.length());

                colorText(false);
                if (!mCart.description.isEmpty()) {
                    textData.append(decode(mCart.description)).append(BREAK);
                }

                if (!mCart.full.isEmpty()) {
                    if (mCart.full.contains(",")) {
                        String[] split = mCart.full.split(",");
                        for (String topping : split) {
                            textData.append(decode(topping));

                            if (split[split.length - 1].equals(topping))
                                textData.append(BREAK);
                            else
                                textData.append(",").append(BREAK);

                        }
                    }
                }

                if (!mCart.half1.isEmpty()) {
                    if (mCart.half1.contains(",")) {
                        textData.append(mContext.getString(R.string.prompt_half_one)).append(" ");

                        String[] split = mCart.half1.split(",");
                        for (String topping : split) {
                            textData.append(decode(topping));

                            if (split[split.length - 1].equals(topping))
                                textData.append(BREAK);
                            else
                                textData.append(",").append(BREAK);
                        }
                    }
                }

                if (!mCart.half2.isEmpty()) {
                    if (mCart.half2.contains(",")) {
                        textData.append(mContext.getString(R.string.prompt_half_two)).append(" ");

                        String[] split = mCart.half2.split(",");
                        for (String topping : split) {
                            textData.append(decode(topping));

                            if (split[split.length - 1].equals(topping))
                                textData.append(BREAK);
                            else
                                textData.append(",").append(BREAK);
                        }
                    }
                }
            }
            Timber.e(textData.toString());
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            mPrinter.addFeedLine(1);

            textData.append(DIVIDER_DOUBLE).append(BREAK);
            itemLeft = "Subtotal:";
            itemRight = Constants.CURRENCY + mOrder.ordersubtotal;
            textData.append(leftRightAlign(itemLeft, itemRight));
            textData.append(BREAK);

            if (!mOrder.offerId.isEmpty() && !mOrder.offeramount.equals("0.00")) {
                itemLeft = "COUPONS/DISCOUNTS";
                itemRight = "-" + Constants.CURRENCY + mOrder.offeramount;
                textData.append(leftRightAlign(itemLeft, itemRight));
                textData.append(BREAK);
                textData.append("(").append(mOrder.offerName).append(")").append(BREAK);
            }

            itemLeft = "Tax(" + mOrder.taxvalue + "%):";
            itemRight = Constants.CURRENCY + mOrder.taxamount;
            textData.append(leftRightAlign(itemLeft, itemRight));
            textData.append(BREAK);

            itemLeft = "Delivery Charge:";
            itemRight = Constants.CURRENCY + mOrder.deliveryamount;
            textData.append(leftRightAlign(itemLeft, itemRight));
            textData.append(BREAK);

            itemLeft = "Convenience Fee:";
            itemRight = Constants.CURRENCY + mOrder.convenienceFee;
            textData.append(leftRightAlign(itemLeft, itemRight));
            textData.append(BREAK);

            if (!mOrder.siteDiscountAmount.isEmpty() || !mOrder.siteDiscountAmount.equals("0")) {
                itemLeft = "Discount(" + mOrder.siteDiscountPercent + "%):";
                itemRight = "-" + Constants.CURRENCY + mOrder.siteDiscountAmount;
                textData.append(leftRightAlign(itemLeft, itemRight));
                textData.append(BREAK);
            }

            itemLeft = "Tip:";
            itemRight = Constants.CURRENCY + mOrder.tipamount;
            textData.append(leftRightAlign(itemLeft, itemRight));
            textData.append(BREAK);
            Timber.e(textData.toString());

            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            boldText(true);
            mPrinter.addTextSize(1, 2);
            itemLeft = "Total:";
            itemRight = Constants.CURRENCY + mOrder.ordertotalprice;
            textData.append(leftRightAlign(itemLeft, itemRight));
            textData.append(BREAK);
            Timber.e(textData.toString());
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            boldText(false);
            mPrinter.addTextSize(1, 1);
            if (!TextUtils.isEmpty(mOrder.instructions)) {
                colorText(true);
                textData.append("Order Instructions: ").append(mOrder.instructions).append(BREAK);
            }
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            colorText(false);
            textData.append(DIVIDER_DOUBLE).append(BREAK);
            mPrinter.addFeedLine(1);
            Timber.e(textData.toString());
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            boldText(true);
            mPrinter.addTextSize(2, 2);
            if (mOrder.paymentType.equals(Constants.PAYMENT_COD))
                textData.append(mOrder.paymentType).append(BREAK);
            else
                textData.append(Constants.PAYMENT_PREPAID).append(BREAK);
            mPrinter.addFeedLine(1);
            Timber.e(textData.toString());
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            boldText(false);
            mPrinter.addTextSize(1, 1);
            textData.append(DIVIDER_DOUBLE);
            mPrinter.addFeedLine(1);
            Timber.e(textData.toString());
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            List<List<String>> listCard = data.ccDetails;
            if (listCard.size() != 0) {
                List<String> card = listCard.get(0);
                if (card.size() > 4 && (mOrder.paymentType.equalsIgnoreCase(Constants.PAYMENT_CC))) {
                    if (!card.get(2).isEmpty() && card.get(2).contains("-")) {
                        String[] split = TextUtils.split(card.get(2), "-");

                        boldText(false);
                        mPrinter.addTextSize(1, 1);
                        textData.append(DIVIDER_DOUBLE).append(BREAK);
                        textData.append("Card Number : ");
                        textData.append("XXXX-XXXX-XXXX-").append(split[3]).append(BREAK);
                        Timber.e(textData.toString());

                        mPrinter.addText(textData.toString());
                        textData.delete(0, textData.length());
                    }
                }
            }

            mPrinter.addFeedLine(2);
            mPrinter.addCut(Printer.CUT_FEED);
        } catch (Exception e) {
            ShowMsg.showException(e, "", mContext);
            e.printStackTrace();
        }

        return true;
    }

    private void boldText(boolean bold) {
        try {
            if (bold) {
                mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT, Printer.TRUE, Printer.PARAM_DEFAULT);
            } else {
                mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void colorText(boolean color) {
        try {
            if (color) {
                mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT, Printer.COLOR_2);
            } else {
                mPrinter.addTextStyle(Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String decode(String url) {
        return url.replace("&amp;", "&");
    }

    private boolean printData() {
        if (mPrinter == null) {
            return false;
        }

        if (!connectPrinter(mActivity)) {
            return false;
        }

        PrinterStatusInfo status = mPrinter.getStatus();

        AppUtils.displayPrinterWarnings(mActivity, status);

        if (!isPrintable(status)) {
            ShowMsg.showMsg(AppUtils.makeErrorMessage(mActivity, status), mActivity);
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                // Do nothing
            }
            return false;
        }

        try {
            mPrinter.sendData(Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "sendData", mActivity);
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                // Do nothing
            }
            return false;
        }

        return true;
    }

    private boolean isPrintable(PrinterStatusInfo status) {
        if (status == null) {
            return false;
        }

        if (status.getConnection() == Printer.FALSE) {
            return false;
        }
        else if (status.getOnline() == Printer.FALSE) {
            return false;
        }
        /*else {
            ;//print available
        }*/

        return true;
    }


    private boolean initializeObject(Context mContext) {
        try {
            mPrinter = new Printer(Printer.TM_U220, Printer.MODEL_ANK, mContext);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "Printer", mContext);
            return false;
        }

        mPrinter.setReceiveEventListener(this);

        return true;
    }

    private boolean connectPrinter(Activity mContext) {
        boolean isBeginTransaction = false;

        if (mPrinter == null) {
            return false;
        }

        String ipAddress = BDPreferences.readString(mContext, Constants.KEY_IP_ADDRESS);
        if (TextUtils.isEmpty(ipAddress)) {
            Utils.showToast(mContext, "Please connect the printer from settings");
            return false;
        }
        ipAddress = "TCP:" + ipAddress;
        Timber.e("Ip address: %s", ipAddress);

        try {
            mPrinter.connect(ipAddress, Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "connect", mContext);
            return false;
        }

        try {
            mPrinter.beginTransaction();
            isBeginTransaction = true;
        }
        catch (Exception e) {
            ShowMsg.showException(e, "beginTransaction", mContext);
        }

        if (!isBeginTransaction) {
            try {
                mPrinter.disconnect();
            }
            catch (Epos2Exception e) {
                // Do nothing
                return false;
            }
        }

        return true;
    }

    private void disconnectPrinter() {
        if (mPrinter == null) {
            return;
        }

        try {
            mPrinter.endTransaction();
        }
        catch (final Exception e) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    ShowMsg.showException(e, "endTransaction", mActivity);
                }
            });
        }

        try {
            mPrinter.disconnect();
        }
        catch (final Exception e) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public synchronized void run() {
                    ShowMsg.showException(e, "disconnect", mActivity);
                }
            });
        }

        finalizeObject();
    }

    @Override
    public void onPtrReceive(Printer printer, int code, PrinterStatusInfo status, String s) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                ShowMsg.showResult(code, AppUtils.makeErrorMessage(mActivity, status), mActivity);

                AppUtils.displayPrinterWarnings(mActivity, status);

                isPrinted = true;

                new Thread(() -> disconnectPrinter()).start();
            }
        });
    }

    private void finalizeObject() {
        if (mPrinter == null) {
            return;
        }
        isPrinted = true;

        mPrinter.clearCommandBuffer();

        mPrinter.setReceiveEventListener(null);

        mPrinter = null;
    }
}
