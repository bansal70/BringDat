package com.bring.dat.model.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AdjustReasons {

    @SerializedName("option")
    public List<Option> mOption;
    @SerializedName("msg")
    public String mMsg;
    @SerializedName("success")
    public boolean mSuccess;

    public static class Option {
        @SerializedName("addeddate")
        public String mAddeddate;
        @SerializedName("response_text")
        public String mReason;
        @SerializedName("response_type")
        public String mResponse_type;
        @SerializedName("id")
        public String mId;
    }
}
