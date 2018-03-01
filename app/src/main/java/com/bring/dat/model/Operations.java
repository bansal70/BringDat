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

    public static HashMap<String, String> newOrdersParams(String restaurantId, String token) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("restaurantId", restaurantId);
        hashMap.put("token", token);

        return hashMap;
    }

    public static HashMap<String, String> workingOrdersParams(String restaurantId, String token) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("restaurantId", restaurantId);
        hashMap.put("token", token);

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

    public static HashMap<String, String> soundParams(String restaurantId, String sound, String token) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("restaurantId", restaurantId);
        hashMap.put("sound", sound);
        hashMap.put("token", token);

        return hashMap;
    }

    public static HashMap<String, String> updateAutoPrintStatus(String restaurantId, String status, String token) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("restaurantId", restaurantId);
        hashMap.put("status", status);
        hashMap.put("token", token);

        return hashMap;
    }

    public static HashMap<String, String> orderStatusParams(String restaurantId, String status, String token) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("restaurantId", restaurantId);
        hashMap.put("status", status);
        hashMap.put("token", token);

        return hashMap;
    }

    public static HashMap<String, String> printingStatusParams(String restaurantId, String payment, String token) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("restaurantId", restaurantId);
        hashMap.put("payment", payment);
        hashMap.put("token", token);

        return hashMap;
    }

    public static HashMap<String, String> updateOrderParams(String restaurantId, String orderId, String status, String workingTime, String token) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("restaurantId", restaurantId);
        hashMap.put("orderId", orderId);
        hashMap.put("status", status);
        hashMap.put("token", token);
        if (!workingTime.isEmpty())
            hashMap.put("selectedtime", workingTime);

        return hashMap;
    }

    public static HashMap<String, String> settingsParams(String restaurantId, String token) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("restaurantId", restaurantId);
        hashMap.put("token", token);

        return hashMap;
    }

    public static HashMap<String, String> voidTransactionParams(String restaurantId, String name , String message,
                                                                String transactionId, String orderId, String token) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("restaurantId", restaurantId);
        hashMap.put("name", name);
        hashMap.put("message", message);
        hashMap.put("transactionId", transactionId);
        hashMap.put("orderId", orderId);
        hashMap.put("token", token);

        return hashMap;
    }

    public static HashMap<String, String> adjustTransactionParams(String restaurantId, String name , String message,
                                                                String transactionId, String orderId, String token, String price) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("restaurantId", restaurantId);
        hashMap.put("username", name);
        hashMap.put("justification", message);
        hashMap.put("price", price);
        hashMap.put("transactionId", transactionId);
        hashMap.put("orderId", orderId);
        hashMap.put("token", token);

        return hashMap;
    }
}
