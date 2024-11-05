package com.superapp_customer.model.dataClass.fareEstimate


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class FareEstimateDC(
    @SerializedName("convenience_charge")
    val convenienceCharge: String? = null,
    @SerializedName("currency")
    val currency: String? = null,
    @SerializedName("fare")
    val fare: String? = null,
    @SerializedName("fare_text")
    val fareText: String? = null,
    @SerializedName("hold_amount")
    val holdAmount: String? = null,
    @SerializedName("pool_fare_id")
    val poolFareId: String? = null,
    @SerializedName("ride_distance")
    val rideDistance: String? = null,
    @SerializedName("ride_distance_unit")
    val rideDistanceUnit: String? = null,
    @SerializedName("ride_time")
    val rideTime: String? = null,
    @SerializedName("ride_time_unit")
    val rideTimeUnit: String? = null,
    @SerializedName("striked_fare_text")
    val strikedFareText: String? = null,
    @SerializedName("text")
    val text: String? = null,
    @SerializedName("toll_charge")
    val tollCharge: String? = null
)