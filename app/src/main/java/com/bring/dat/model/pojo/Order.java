package com.bring.dat.model.pojo;

/*
 * Created by rishav on 12/26/2017.
 */

import com.google.gson.annotations.SerializedName;

public class Order {
    @SerializedName("orderid")
    public String orderid;
    @SerializedName("ordergenerateid")
    public String ordergenerateid;
    @SerializedName("restaurant_id")
    public String restaurantId;
    @SerializedName("customer_id")
    public String customerId;
    @SerializedName("usertype")
    public String usertype;
    @SerializedName("customername")
    public String customername;
    @SerializedName("customerlastname")
    public String customerlastname;
    @SerializedName("customeremail")
    public String customeremail;
    @SerializedName("customerpassword")
    public String customerpassword;
    @SerializedName("customercellphone")
    public String customercellphone;
    @SerializedName("customerlandline")
    public String customerlandline;
    @SerializedName("customerstreet")
    public String customerstreet;
    @SerializedName("deliverydoornumber")
    public String deliverydoornumber;
    @SerializedName("deliverystreet")
    public String deliverystreet;
    @SerializedName("deliverylandmark")
    public String deliverylandmark;
    @SerializedName("deliveryarea")
    public String deliveryarea;
    @SerializedName("deliverycity")
    public String deliverycity;
    @SerializedName("deliverystate")
    public String deliverystate;
    @SerializedName("deliveryzip")
    public String deliveryzip;
    @SerializedName("deliverytype")
    public String deliverytype;
    @SerializedName("foodassoonas")
    public String foodassoonas;
    @SerializedName("deliverydate")
    public String deliverydate;
    @SerializedName("deliverytime")
    public String deliverytime;
    @SerializedName("instructions")
    public String instructions;
    @SerializedName("offervalue")
    public String offervalue;
    @SerializedName("taxvalue")
    public String taxvalue;
    @SerializedName("deliveryamount")
    public String deliveryamount;
    @SerializedName("offeramount")
    public String offeramount;
    @SerializedName("taxamount")
    public String taxamount;
    @SerializedName("tipamount")
    public String tipamount;
    @SerializedName("ordersubtotal")
    public String ordersubtotal;
    @SerializedName("ordertotalprice")
    public String ordertotalprice;
    @SerializedName("payment_type")
    public String paymentType;
    @SerializedName("transaction_id")
    public String transactionId;
    @SerializedName("cardpaymentfees")
    public String cardpaymentfees;
    @SerializedName("cardpaymentper")
    public String cardpaymentper;
    @SerializedName("payment_status")
    public String paymentStatus;
    @SerializedName("status")
    public String status;
    @SerializedName("paypal_status")
    public String paypalStatus;
    @SerializedName("printer_sent")
    public String printerSent;
    @SerializedName("printer_response")
    public String printerResponse;
    @SerializedName("printer_res_deli_time")
    public String printerResDeliTime;
    @SerializedName("printer_ack")
    public String printerAck;
    @SerializedName("printer_ack_msg")
    public String printerAckMsg;
    @SerializedName("orderdate")
    public String orderdate;
    @SerializedName("res_comm_perchantage")
    public String resCommPerchantage;
    @SerializedName("res_comm_price")
    public String resCommPrice;
    @SerializedName("res_order_delivereddate")
    public String resOrderDelivereddate;
    @SerializedName("delete_status")
    public String deleteStatus;
    @SerializedName("discount_type")
    public String discountType;
    @SerializedName("offer_id")
    public String offerId;
    @SerializedName("convenience_fee")
    public String convenienceFee;
    @SerializedName("convenience_fee_status")
    public String convenienceFeeStatus;
    @SerializedName("site_discount_percent")
    public String siteDiscountPercent;
    @SerializedName("site_discount_amount")
    public String siteDiscountAmount;
    @SerializedName("sms_status")
    public String smsStatus;
    @SerializedName("sms_carrier_id")
    public String smsCarrierId;
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
    @SerializedName("offer_name")
    public String offerName;
    @SerializedName("offer_percentage")
    public String offerPercentage;
    @SerializedName("has_adjust_log")
    public String hasAdjustLog;
    @SerializedName("orderdeliverydate")
    public String orderdeliverydate;
    @SerializedName("two_days_interval")
    public String twoDaysInterval;
    @SerializedName("order_date")
    public String orderDate;
    @SerializedName("cityName")
    public String cityName;
    @SerializedName("apply_adjust")
    public String applyAdjust;
    @SerializedName("apply_void")
    public String applyVoid;
    @SerializedName("merchant_name")
    public String merchantName;
    @SerializedName("merchant_address")
    public String merchantAddress;
    @SerializedName("merchant_phone")
    public String merchantPhone;
    @SerializedName("order_print_status")
    public String order_print_status;
}
