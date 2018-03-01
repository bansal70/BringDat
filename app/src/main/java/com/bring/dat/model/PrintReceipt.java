package com.bring.dat.model;

/*
 * Created by rishav on 1/3/2018.
 */

import android.content.Context;
import android.os.StrictMode;
import android.text.TextUtils;

import com.bring.dat.R;
import com.bring.dat.model.network.APIClient;
import com.bring.dat.model.network.ApiService;
import com.bring.dat.model.pojo.Cart;
import com.bring.dat.model.pojo.Order;
import com.bring.dat.model.pojo.OrderDetails;
import com.bring.dat.views.services.BTService;
import com.bring.dat.views.services.BluetoothService;
import com.bring.dat.views.services.WFService;
import com.zj.wfsdk.WifiCommunication;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;
import timber.log.Timber;

public class PrintReceipt {

    private static boolean isWifiService(Context mContext) {
        return Utils.isServiceRunning(mContext, WFService.class);
    }

    private static boolean isBTService(Context mContext) {
        return Utils.isServiceRunning(mContext, BTService.class);
    }

    private static void printLeft(String txt) {
        byte[] format = new byte[]{0x1B, 'a', 0x00};
        Timber.e("\n%s", txt);

        if (isBTService(Application.getInstance().getApplicationContext())) {
            BluetoothService mService = BTService.mService;
            mService.write(format);
            mService.sendMessage(txt, "GBK");
        }
    }

    private static void printCenter(String txt) {
        byte[] format = new byte[]{0x1B, 'a', 0x01};
        Timber.e("\n%s", txt);

        if (isBTService(Application.getInstance().getApplicationContext())) {
            BluetoothService mService = BTService.mService;
            mService.write(format);
            mService.sendMessage(txt, "GBK");
        }
    }

    private static void printRight(String txt) {
        byte[] format = new byte[]{0x1B, 'a', 0x02};

        if (isBTService(Application.getInstance().getApplicationContext())) {
            BluetoothService mService = BTService.mService;
            mService.write(format);
            mService.sendMessage(txt, "GBK");
        } else {
            printViaWIFI(txt, format);
        }
    }

    private static void printLeftRightAlign(String str1, String str2) {
        byte[] format = new byte[]{0x1B, 'a', 0x02};
        String txt = str1 +str2;
        if(txt.length() <31){
            int n = (31 - (str1.length() + str2.length()));
            txt = str1 + new String(new char[n]).replace("\0", " ") + str2;
        }

        Timber.e("\n%s", txt);

        if (isBTService(Application.getInstance().getApplicationContext())) {
            BluetoothService mService = BTService.mService;
            mService.write(format);
            mService.sendMessage(txt, "GBK");
        }
    }

    private static void printViaWIFI(String message, byte[] format) {
        if (!WFService.isWifi()) {
            return;
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        WifiCommunication wfComm = WFService.wfComm;
        if (wfComm == null) {
            return;
        }
        wfComm.sndByte(format);
        wfComm.sendMsg(message, "GBK");
    }

    private static void writeWithFormat(String message, byte[] buffer, final byte[] pFormat, final byte[] pAlignment) {
        BluetoothService mService = BTService.mService;
        try {
            Timber.e("\n%s", message);
            if (isBTService(Application.getInstance().getApplicationContext())) {
                mService.write(pAlignment);
                mService.write(pFormat);
                mService.sendMessage(message, "GBK");
            } /*else {
                if (!WFService.isWifi()) {
                    return;
                }
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                WifiCommunication wfComm = WFService.wfComm;
                if (wfComm == null)
                    return;
                wfComm.sndByte(pAlignment);
                wfComm.sndByte(pFormat);
                wfComm.sendMsg(message, "GBK");
            }*/
        } catch (Exception e) {
            Timber.e(e, "Exception during write");
        }
    }

    public static void printOrderReceipt(Context mContext, OrderDetails mOrderDetails) {
        String msg, msg2;
        //String DIVIDER = "--------------------------------";
        String DIVIDER_DOUBLE = "================================";
        String HORIZONTAL_LINE = "________________________________";
        String BREAK = "\n";
        String SPACE5 = "     ";
        String SPACE4 = "    ";

        OrderDetails.Data data = mOrderDetails.data;
        Order mOrder = data.order.get(0);
        List<Cart> mCartList = data.cart;

        msg = "ONLINE ORDER" + BREAK + BREAK;
        writeWithFormat(msg, msg.getBytes(), new Formatter().bold().height().get(), Formatter.centerAlign());

        msg = mOrder.merchantName + BREAK;
        writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.centerAlign());

        msg = mOrder.merchantAddress + BREAK;
        writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.centerAlign());

        msg = mOrder.merchantPhone + BREAK;
        writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.centerAlign());

        msg = DIVIDER_DOUBLE + BREAK;
        writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.centerAlign());

        if (mOrder.deliverytype.equalsIgnoreCase("delivery")) {
            msg = "DELIVERY" + BREAK + DIVIDER_DOUBLE + BREAK;
        } else {
            msg = "PICKUP" + BREAK + DIVIDER_DOUBLE + BREAK;
        }
        writeWithFormat(msg, msg.getBytes(), new Formatter().bold().height().get(), Formatter.centerAlign());

        msg = mOrder.ordergenerateid + SPACE5 + mOrder.deliverydate + " " + mOrder.orderdate + BREAK;
        writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.centerAlign());

        if (mOrder.deliverytime.equalsIgnoreCase("ASAP")) {
            msg = mOrder.orderdeliverydate + BREAK;
        } else {
            String time = "Ready By: " + Utils.parseDateToMMdd(mOrder.deliverydate) + " " + mOrder.deliverytime;
            msg = time + BREAK + DIVIDER_DOUBLE;
        }
        writeWithFormat(msg, msg.getBytes(), new Formatter().bold().get(), Formatter.centerAlign());

        msg = DIVIDER_DOUBLE + BREAK;
        writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.centerAlign());

        msg = mOrder.customername + " " + mOrder.customerlastname + BREAK;
        writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.centerAlign());

        msg = AppUtils.userAddress(mOrder) + BREAK;
        writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.centerAlign());

        msg = mOrder.customercellphone + BREAK + DIVIDER_DOUBLE + BREAK;
        writeWithFormat(msg, msg.getBytes(), new Formatter().bold().height().get(), Formatter.centerAlign());

        msg = "Qty" + SPACE4 + "Item";
        //printLeft(msg);

        msg2 = "Price" + BREAK;
        //printRight(msg);
        printLeftRightAlign(msg, msg2);

        for (int i = 0; i < mCartList.size(); i++) {
            Cart mCart = mCartList.get(i);
            msg = " " + mCart.qty + "   " + mCart.item;
            //printLeft(msg);
            float totalPrice = Float.parseFloat(mCart.qty) * mCart.price;

            msg2 = Constants.CURRENCY + AppUtils.roundTwoDecimal(totalPrice);
            //printRight(msg);
            printLeftRightAlign(msg, msg2);

            printCenter(BREAK);

            if (!mCart.description.isEmpty()) {
                msg = SPACE5 + "  " + mCart.description;
                printLeft(msg);
                printCenter(BREAK);
            }
            if (!mCart.full.isEmpty()) {
                StringBuilder textData = new StringBuilder();
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
                msg = SPACE5 + "  " + textData.toString();
                printLeft(msg);
                printCenter(BREAK);
            }

            if (!mCart.half1.isEmpty()) {
                StringBuilder firstHalf = new StringBuilder();
                if (mCart.half1.contains(",")) {
                    firstHalf.append(mContext.getString(R.string.prompt_half_one)).append(" ");

                    String[] split = mCart.half1.split(",");
                    for (String topping : split) {
                        firstHalf.append(decode(topping));

                        if (split[split.length - 1].equals(topping))
                            firstHalf.append(BREAK);
                        else
                            firstHalf.append(",").append(BREAK);
                    }
                }
                msg = firstHalf.toString();
                printLeft(msg);
                printCenter(BREAK);
            }
            if (!mCart.half2.isEmpty()) {
                StringBuilder secondHalf = new StringBuilder();
                if (mCart.half2.contains(",")) {
                    secondHalf.append(mContext.getString(R.string.prompt_half_two)).append(" ");

                    String[] split = mCart.half2.split(",");
                    for (String topping : split) {
                        secondHalf.append(decode(topping));

                        if (split[split.length - 1].equals(topping))
                            secondHalf.append(BREAK);
                        else
                            secondHalf.append(",").append(BREAK);
                    }
                }

                msg = secondHalf.toString();
                printLeft(msg);
                printCenter(BREAK);
            }

        }

        msg = DIVIDER_DOUBLE + BREAK;
        writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.centerAlign());

        msg = BREAK + "Subtotal:";
        //printLeft(msg);

        msg2 = Constants.CURRENCY + mOrder.ordersubtotal;
        printLeftRightAlign(msg, msg2);
        //printRight(msg);
        printCenter(BREAK);

        if (!mOrder.offerId.isEmpty() && !mOrder.offeramount.equals("0.00")) {
            msg = "COUPONS/DISCOUNTS";
            //printLeft(msg);

            msg2 = Constants.CURRENCY + mOrder.offeramount;
            //printRight(msg);
            printLeftRightAlign(msg, msg2);

            printCenter(BREAK);

            msg = mOrder.offerName;
            printLeft(msg);
        }

        msg = "Tax(" + mOrder.taxvalue + "%):";
        //printLeft(msg);

        //double salesTax = Double.valueOf(mOrder.taxamount);

        msg2 = SPACE5 + Constants.CURRENCY + mOrder.taxamount;
        //printRight(msg);
        printLeftRightAlign(msg, msg2);
        printCenter(BREAK);

        msg = "Delivery Charge:";
        //printLeft(msg);

        msg2 = Constants.CURRENCY + mOrder.deliveryamount;
        //printRight(msg);
        printLeftRightAlign(msg, msg2);
        printCenter(BREAK);

        msg = "Convenience Fee:";
        //printLeft(msg);

        msg2 = SPACE5 + Constants.CURRENCY + mOrder.convenienceFee;
        //printRight(msg);
        printLeftRightAlign(msg, msg2);
        printCenter(BREAK);

        if (!mOrder.siteDiscountAmount.isEmpty() || !mOrder.siteDiscountAmount.equals("0")) {
            msg = "Discount(" + mOrder.siteDiscountPercent + "%):";
            //printLeft(msg);

            msg2 = SPACE5 + Constants.CURRENCY + mOrder.siteDiscountAmount;
            //printRight(msg);
            printLeftRightAlign(msg, msg2);
            printCenter(BREAK);
        }

        msg = "Tip:";
        //printLeft(msg);

        msg2 = SPACE5 + Constants.CURRENCY + mOrder.tipamount;
        //printRight(msg);
        printLeftRightAlign(msg, msg2);
        printCenter(BREAK);

        msg = "Total:";
        //printLeft(msg);

        msg2 = SPACE5 + Constants.CURRENCY + mOrder.ordertotalprice;

        String total = leftRightAlign(msg, msg2);
        //printRight(msg);
        writeWithFormat(total, total.getBytes(), new Formatter().bold().height().get(), Formatter.centerAlign());
        printCenter(BREAK);

        msg = BREAK + BREAK + "Order Instructions: " + mOrder.instructions;
        writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.centerAlign());

        msg = BREAK + BREAK + DIVIDER_DOUBLE + BREAK;
        writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.centerAlign());

        if (mOrder.paymentType.equals(Constants.PAYMENT_COD))
            msg = BREAK + mOrder.paymentType + BREAK + DIVIDER_DOUBLE + BREAK;
        else
            msg = Constants.PAYMENT_PREPAID + BREAK + DIVIDER_DOUBLE + BREAK;

        writeWithFormat(msg, msg.getBytes(), new Formatter().bold().height().get(), Formatter.centerAlign());

        StringBuilder textData = new StringBuilder();
        List<List<String>> listCard = data.ccDetails;
        if (listCard.size() != 0) {
            List<String> card = listCard.get(0);
            if (card.size() > 4 && (mOrder.paymentType.equalsIgnoreCase(Constants.PAYMENT_CC))) {
                if (!card.get(2).isEmpty() && card.get(2).contains("-")) {
                    String[] split = TextUtils.split(card.get(2), "-");
                    textData.append(DIVIDER_DOUBLE);
                    textData.append("Card Number : ");
                    textData.append("XXXX-XXXX-XXXX-").append(split[3]).append(BREAK);

                    writeWithFormat(textData.toString(), textData.toString().getBytes(), new Formatter().get(), Formatter.centerAlign());
                }
            }
        }

        msg = BREAK + BREAK + BREAK + BREAK;
        writeWithFormat(msg, msg.getBytes(), new Formatter().get(), Formatter.centerAlign());

        receiptPrinted(mContext, mOrder.orderid);
    }

    private static void receiptPrinted(Context mContext, String orderID) {
        ApiService apiService = APIClient.getClient().create(ApiService.class);
         String token = BDPreferences.readString(mContext, Constants.KEY_TOKEN);

        apiService.getReceipt(Operations.receiptPrintParams(orderID, token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {})
                .doOnNext(orderDetails -> {})
                .doOnError(PrintReceipt::serverError)
                .subscribe();
    }

    private static String decode(String url) {
        return url.replace("&amp;", "&");
    }

    private static void printUsingXML(Context mContext) {
        final InputStream[] inputStream = new InputStream[1];
        String req =
                "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                        "<s:Body>" +
                        makeFormat() +
                        "</s:Body>" +
                        "</s:Envelope>";

        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("text/xml; charset=utf-8"), req);
        ApiService apiService = APIClient.getClient().create(ApiService.class);
        apiService.getPrintApi("http://192.168.1.14/cgi-bin/epos/service.cgi?devid=local_printer&timeout=10000", body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {
                    Utils.showToast(mContext, "Something went wrong");
                })
                .doOnNext(PrintReceipt::fetchResponse)
                .doOnError(PrintReceipt::serverError)
                .subscribe();


    }

    private static  void fetchResponse(ResponseBody response) {
        Timber.e("Response: %s", response.toString());
        try {
            // Receive response document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Parse response document(DOM)
            Document doc = builder.parse(response.byteStream());
            Element el = (Element) doc.getElementsByTagName("response").item(0);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void serverError(Throwable throwable) {
        throwable.printStackTrace();
        if (throwable instanceof HttpException) {
            Response<?> response = ((HttpException) throwable).response();
            Timber.e(response.message());
        }
    }




    private static String makeFormat() {
        return "<epos-print xmlns=\"http://www.epson-pos.com/schemas/2011/03/epos-print\">" +
                "<text align=\"center\"/>" +
                "<text dw=\"true\" dh=\"true\"/>" +
                "<feed unit=\"20\"/>" +
                "<text>ONLINE ORDER&#10;</text>" +
                "<feed line=\"1\"/>" +
                "<text dw=\"false\" dh=\"false\"/>" +
                "<text>Demo restaurant name&#10;</text>" +
                "<text>Demo Address&#10;</text>" +
                "<text>(212) 555-1213&#10;</text>" +
                "<feed/>" +
                "<text>================================&#10;</text>0" +
                "<text>Delivery&#10;</text>" +
                "<text>ASAP ON FEB 20, 2018</text>" +
                "<text>================================&#10;</text>" +
                "<text>ORD0387&#9; Feb 20, 2018 09:29:33 AM&#10;</text>" +
                "<feed/>" +
                "<text>Hardeep Singh&#10;</text>" +
                "<text>Apt/Suite/Bldg#227&#10;</text>" +
                "<text>123 Main Street Newark&#10;</text>" +
                "<text>(985)500-4374&#10;</text>" +
                "<text>================================&#10;</text>" +
                "<text>Qty&#9; Items&#9; Prices&#10;</text>" +
                "<feed unit=\"20\"/>" +
                "<text align=\"left\"/>" +
                "<text x=\"25\"/>" +
                "<text>1&#9; Veg Item &#9; $5.00&#10;</text>" +
                "<text>&#9; Addons: test addons&#10;</text>" +
                "<feed/>" +
                "<text align=\"center\"/>" +
                "<text>Subtotal:&#9;&#9; $5.00&#10;</text>" +
                "<text>Tax(6.625%):&#9;&#9; $1.25&#10;</text>" +
                "<text>Delivery Charge:&#9; $1.50&#10;</text>" +
                "<text>Convenience Fee:&#9; $0.98&#10;</text>" +
                "<text>Tip:&#9;&#9;&#9; $2.00&#10;</text>" +
                "<text>BringDat Discount(5%):&#9; -$5.00&#10;</text>" +
                "<text width=\"1\" height=\"1\"/>"+
                "<text>Total:&#9;&#9; $15.00&#10;</text>" +
                "<text width=\"1\" height=\"1\"/>"+
                "<text>================================&#10;</text>" +
                "<text>PREPAID&#10;</text>" +
                "<text>================================&#10;</text>" +
                "<feed/>" +
                "<text>================================&#10;</text>" +
                "<text>Card Number:XXXX-XXXX-XXXX-0002</text>" +
                "<feed line=\"3\"/>" +
                "<cut type=\"feed\"/>" +
                "</epos-print>";
    }


    private static String leftRightAlign(String str1, String str2) {
        String txt = str1 + str2;
        if (txt.length() < 31) {
            int n = (31 - (str1.length() + str2.length()));
            txt = str1 + new String(new char[n]).replace("\0", " ") + str2;
        }
        return txt;
    }

}
