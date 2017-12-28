package com.bring.dat.model;

import android.content.Context;
import android.content.SharedPreferences;

/*
 * Created by rishav on 8/17/17.
 */

public class BDPreferences {

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences("BD_PREF", Context.MODE_PRIVATE);
    }

    public static String readString(Context context, String key) {
        return getPreferences(context).getString(key, "");
    }

    public static void putString(Context context, String key, String value) {
         getPreferences(context).edit().putString(key, value).apply();
    }

    public static void getString(Context context , String key , String value){
        getPreferences(context).getString(key,value);
    }

    public static void putDouble(Context context, String key, double value) {
        getPreferences(context).edit().putLong(key, Double.doubleToRawLongBits(value)).apply();
    }

    public static double readDouble(Context context, String key) {
        return Double.longBitsToDouble(getPreferences(context).getLong(key, 0));
    }

    public static void putBoolean(Context context, String key, boolean value) {
        getPreferences(context).edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(Context context, String key, boolean value) {
        return getPreferences(context).getBoolean(key, value);
    }

    public static boolean readBoolean(Context context, String key) {
        return getPreferences(context).getBoolean(key, false);
    }

    public static void removeKey(Context context, String key) {
        getPreferences(context).edit().remove(key).apply();
    }
    public static void clearPref(Context context) {
        getPreferences(context).edit().clear().apply();
    }
}
