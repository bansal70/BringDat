package com.bring.dat.model;

/*
 * Created by rishav on 12/25/2017.
 */

import java.util.HashMap;

public class Operations {

    public static HashMap<String, String> loginParams(String email, String password, String deviceToken) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uname", email);
        hashMap.put("passwd", password);
        hashMap.put("dtype", Constants.DEVICE_TOKEN);
        hashMap.put("dtoken", deviceToken);

        return hashMap;
    }

    public static HashMap<String, String> ordersListParams(String restaurantId, int page, String token) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("restaurantId", restaurantId);
        hashMap.put("page", String.valueOf(page));
        hashMap.put("token", token);

        return hashMap;
    }

    public static HashMap<String, String> ordersDetailsParams(String orderId, String token) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", orderId);
        hashMap.put("token", token);

        return hashMap;
    }

    public static HashMap<String, String> receiptPrintParams(String orderId, String token) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", orderId);
        hashMap.put("status", "1");
        hashMap.put("token", token);

        return hashMap;
    }

    public static HashMap<String, String> reportParams(String restaurantId, String startDate, String endDate, String token) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("restaurantId", restaurantId);
        hashMap.put("startDate", startDate);
        hashMap.put("endDate", endDate);
        hashMap.put("token", token);

        return hashMap;
    }
}
