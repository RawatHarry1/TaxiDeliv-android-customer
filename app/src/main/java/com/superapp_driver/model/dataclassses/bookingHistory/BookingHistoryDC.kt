package com.superapp_driver.model.dataclassses.bookingHistory


import com.google.gson.annotations.SerializedName

data class BookingHistoryDC(
    @SerializedName("actual_fare")
    val actualFare: String? = null,
    @SerializedName("tracking_image")
    val trackingImage: String? = null,
    @SerializedName("ride_fare")
    val rideDFare: String? = null,
    @SerializedName("distance")
    val distance: String? = null,
    @SerializedName("driver_ride_fare")
    val driverRideFare: String? = null,
    @SerializedName("drop_latitude")
    val dropLatitude: String? = null,
    @SerializedName("drop_longitude")
    val dropLongitude: String? = null,
    @SerializedName("fare")
    val fare: String? = null,
    @SerializedName("from")
    val from: String? = null,
    @SerializedName("pickup_latitude")
    val pickupLatitude: String? = null,
    @SerializedName("pickup_longitude")
    val pickupLongitude: String? = null,
    @SerializedName("ride_time")
    val rideTime: String? = null,
    @SerializedName("status_string")
    val statusString: String? = null,
    @SerializedName("time")
    val time: String? = null,
    @SerializedName("to")
    val to: String? = null,
    @SerializedName("trip_id")
    val tripId: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("wait_time")
    val waitTime: String? = null,
    @SerializedName("customer_name")
    val customerName: String? = null,
    @SerializedName("total_fare")
    val totalFare: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("is_for_rental")
    val isForRental: String? = null
)