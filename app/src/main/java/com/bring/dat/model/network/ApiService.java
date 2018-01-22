package com.bring.dat.model.network;

import com.bring.dat.model.pojo.LoginResponse;
import com.bring.dat.model.pojo.OrderDetails;
import com.bring.dat.model.pojo.OrdersResponse;
import com.bring.dat.model.pojo.Reports;

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

    @POST("webservices.php?action=completedOrder")
    Flowable<OrdersResponse> getOrders(@QueryMap HashMap<String, String> mapParams);

    @POST("webservices.php?action=getOrder")
    Observable<OrderDetails> getOrderDetails(@QueryMap HashMap<String, String> mapParams);

    @POST("webservices.php?action=printStaus")
    Observable<OrderDetails> getReceipt(@QueryMap HashMap<String, String> mapParams);

    @GET("webservices.php?action=saleReportByDate")
    Observable<Reports> getReports(@QueryMap HashMap<String, String> mapParams);
}
