package com.bring.dat.model.pojo;

/*
 * Created by rishav on 1/25/2018.
 */

import com.google.gson.annotations.SerializedName;

public class Transaction {
    @SerializedName("success")
    public boolean success;
    @SerializedName("msg")
    public String msg;
}
