package com.bring.dat.model.pojo;

/*
 * Created by rishav on 1/23/2018.
 */

import com.google.gson.annotations.SerializedName;

public class Settings {

    @SerializedName("success")
    public boolean success;
    @SerializedName("msg")
    public String msg;
    @SerializedName("sound")
    public String mSound;
    @SerializedName("printing_option")
    public String printingOption;
    @SerializedName("ordering_status")
    public String mOrderStatus;
    @SerializedName("data")
    public Data data;
    @SerializedName("authentication")
    public String mAuthentication = "0";

    public static class Data {
        @SerializedName("menu_only_merchant_flag")
        public String onlineOrder;
        @SerializedName("printing_option")
        public String printingOption;
        @SerializedName("printing_payment_methods")
        public String printingType;
        @SerializedName("sound_type")
        public String soundType;
    }
}
