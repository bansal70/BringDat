package com.bring.dat.model.network;

import com.bring.dat.model.pojo.LoginResponse;
import com.bring.dat.model.pojo.OrderDetails;
import com.bring.dat.model.pojo.OrdersResponse;

import java.util.HashMap;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/*
 * Created by win 10 on 12/25/2017.
 */

public interface ApiService {

    @POST("webservices.php?action=login")
    Observable<LoginResponse> loginUser(@QueryMap HashMap<String, String> mapParams);

    @POST("webservices.php?action=completedOrder")
    Flowable<OrdersResponse> getOrders(@QueryMap HashMap<String, String> mapParams);

    @POST("webservices.php?action=getOrder")
    Observable<OrderDetails> getOrderDetails(@QueryMap HashMap<String, String> mapParams);
}
