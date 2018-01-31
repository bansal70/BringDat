package com.bring.dat.model.network;

import com.bring.dat.model.pojo.AdjustReasons;
import com.bring.dat.model.pojo.LoginResponse;
import com.bring.dat.model.pojo.OrderDetails;
import com.bring.dat.model.pojo.OrdersResponse;
import com.bring.dat.model.pojo.Reports;
import com.bring.dat.model.pojo.Settings;
import com.bring.dat.model.pojo.Transaction;

import java.util.HashMap;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/*
 * Created by rishav on 12/25/2017.
 */

public interface ApiService {

    @POST("webservices.php?action=login")
    Observable<LoginResponse> loginUser(@QueryMap HashMap<String, String> mapParams);

    @POST("webservices.php?action=pendingOrder")
    Flowable<OrdersResponse> getNewOrders(@QueryMap HashMap<String, String> mapParams);

    @POST("webservices.php?action=workingOrder")
    Flowable<OrdersResponse> getWorkingOrders(@QueryMap HashMap<String, String> mapParams);

    @POST("webservices.php?action=completedOrder")
    Flowable<OrdersResponse> getOrders(@QueryMap HashMap<String, String> mapParams);

    @POST("webservices.php?action=getOrder")
    Observable<OrderDetails> getOrderDetails(@QueryMap HashMap<String, String> mapParams);

    @POST("webservices.php?action=printStaus")
    Observable<OrderDetails> getReceipt(@QueryMap HashMap<String, String> mapParams);

    @GET("webservices.php?action=saleReportByDate")
    Observable<Reports> getReports(@QueryMap HashMap<String, String> mapParams);

    @POST("webservices.php?action=updatesoundthere")
    Observable<Settings> updateSound(@QueryMap HashMap<String, String> mapParams);

    @POST("webservices.php?action=updateOrderingStatus")
    Observable<Settings> updateOrderStatus(@QueryMap HashMap<String, String> mapParams);

    @POST("webservices.php?action=updatePrintingPaymentMethod")
    Observable<Settings> updatePrintingStatus(@QueryMap HashMap<String, String> mapParams);

    @POST("webservices.php?action=changeStatus")
    Observable<Settings> changeOrderStatus(@QueryMap HashMap<String, String> mapParams);

    @POST("webservices.php?action=getSettings")
    Observable<Settings> getSettings(@QueryMap HashMap<String, String> mapParams);

    @POST("webservices.php?action=voidTransaction")
    Observable<Transaction> voidTransaction(@QueryMap HashMap<String, String> mapParams);

    @GET("webservices.php?action=getDialogOption&type=adjust")
    Observable<AdjustReasons> getAdjustReasons();

    @POST("webservices.php?action=adjustTransaction")
    Observable<Transaction> adjustTransaction(@QueryMap HashMap<String, String> mapParams);
}
