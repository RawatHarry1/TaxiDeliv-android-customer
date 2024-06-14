package com.venus_customer.model.dataClass.requestTrip


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class RequestTripDC(
    @SerializedName("drop_location_address")
    val dropLocationAddress: String? = null,
    @SerializedName("gps_lock_status")
    val gpsLockStatus: String? = null,
    @SerializedName("latitude")
    val latitude: String? = null,
    @SerializedName("log")
    val log: String? = null,
    @SerializedName("longitude")
    val longitude: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("order_id")
    val orderId: String? = null,
    @SerializedName("pickup_location_address")
    val pickupLocationAddress: String? = null,
    @SerializedName("session_id")
    val sessionId: String? = null,
    @SerializedName("start_time")
    val startTime: String? = null,
    @SerializedName("tip_amount")
    val tipAmount: String? = null
)