
package com.bring.dat.model.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@SuppressWarnings("unused")
public class OrdersResponse {

    @SerializedName("success")
    public Boolean success;
    @SerializedName("msg")
    public String msg;
    @SerializedName("authentication")
    public String mAuthentication = "0";
    @SerializedName("totalpending")
    public String totalPending;
    @SerializedName("totalworking")
    public String totalWorking;
    @SerializedName("totalcompleted")
    public String totalCompleted;
    @SerializedName("totalrecords")
    public String totalRecords;
    @SerializedName("totalpages")
    public Integer totalPages;
    @SerializedName("app_printing_option")
    public String appPrintingOption;
    @SerializedName("cod_enable")
    public String codEnable;
    @SerializedName("prepaid_enable")
    public String prepaidEnable;
    @SerializedName("data")
    public Data data;

    public class Data {
        @SerializedName("order")
        public List<Order> orderList = null;
    }
}
