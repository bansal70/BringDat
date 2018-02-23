package com.bring.dat.model.pojo;

/*
 * Created by rishav on 1/19/2018.
 */

import com.google.gson.annotations.SerializedName;

public class Reports {

    @SerializedName("success")
    public boolean success;
    @SerializedName("msg")
    public String msg;
    @SerializedName("data")
    public Data data;
    @SerializedName("authentication")
    public String mAuthentication = "0";

    public static class Data {
        @SerializedName("tipAmount")
        public String tipAmount;
        @SerializedName("deliveryCharge")
        public String deliveryCharge;
        @SerializedName("taxAmount")
        public String taxAmount;
        @SerializedName("total")
        public String total;
        @SerializedName("codtotal")
        public String codTotal;
        @SerializedName("creditCardtotal")
        public String creditCardTotal;
        @SerializedName("convenience_fee")
        public String convenience_fee;
        @SerializedName("adjustPositive")
        public String adjustPositive;
        @SerializedName("adjustNegative")
        public String adjustNegative;
        @SerializedName("grosstotal")
        public String grossTotal;
        @SerializedName("cancel_order")
        public String cancelOrder;
        @SerializedName("scnt")
        public String totalOrders;
    }
}
