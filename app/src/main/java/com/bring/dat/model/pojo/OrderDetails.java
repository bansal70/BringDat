package com.bring.dat.model.pojo;

/*
 * Created by rishav on 12/26/2017.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrderDetails {
    @SerializedName("success")
    public Boolean success;
    @SerializedName("msg")
    public String msg;
    @SerializedName("data")
    public Data data;

    public class Data {
        @SerializedName("order")
        public List<Order> order = null;
        @SerializedName("cart")
        public List<Cart> cart = null;
        @SerializedName("ccdetails")
        public List<List<String>> ccDetails = null;
        @SerializedName("orderLog")
        public List<OrderLog> orderLog = null;
    }

    public class OrderLog {
        @SerializedName("action_id")
        @Expose
        public String actionId;
        @SerializedName("restaurant_id")
        @Expose
        public String restaurantId;
        @SerializedName("order_id")
        @Expose
        public String orderId;
        @SerializedName("actionType")
        @Expose
        public String actionType;
        @SerializedName("addeddate")
        @Expose
        public String addeddate;
    }
}
