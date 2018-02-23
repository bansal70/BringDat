package com.bring.dat.model.pojo;

import com.google.gson.annotations.SerializedName;

/*
 * Created by rishav on 12/26/2017.
 */

public class Cart {

    @SerializedName("orderid")
    public String orderid;
    @SerializedName("item")
    public String item;
    @SerializedName("qty")
    public String qty;
    @SerializedName("pizza_size")
    public String pizzaSize;
    @SerializedName("description")
    public String description;
    @SerializedName("price")
    public Float price;
    @SerializedName("specialinstruction")
    public String specialinstruction;
    @SerializedName("q")
    public Integer q;
    @SerializedName("toppingsname")
    public String toppingsName;
    @SerializedName("full")
    public String full;
    @SerializedName("half1")
    public String half1;
    @SerializedName("half2")
    public String half2;

}
