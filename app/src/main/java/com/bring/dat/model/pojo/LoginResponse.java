
package com.bring.dat.model.pojo;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class LoginResponse {

    @SerializedName("data")
    public Data mData;
    @SerializedName("msg")
    public String mMsg;
    @SerializedName("success")
    public Boolean mSuccess;

    public class Data {
        @SerializedName("restaurant_id")
        public String restaurantId;
        @SerializedName("restaurant_email")
        public String restaurantEmail;
        @SerializedName("token")
        public String token;
    }
}
