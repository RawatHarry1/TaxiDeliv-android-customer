package com.mb_driver.model.dataclassses.bookingHistory


import com.google.gson.annotations.SerializedName
import com.mb_driver.model.dataclassses.rideModels.OngoingPackages

data class RideSummaryDC(
    @SerializedName("tracking_image")
    val trackingImage: String? = null,
    @SerializedName("accept_distance")
    val acceptDistance: String? = null,
    @SerializedName("accept_time")
    val acceptTime: String? = null,
    @SerializedName("actual_fare")
    val actualFare: String? = null,
    @SerializedName("ride_fare")
    val rideFare: String? = null,
    @SerializedName("net_customer_tax")
    val netCustomerTax: String? = null,
    @SerializedName("venus_commission")
    val venusCommission: String? = null,
    @SerializedName("sub_total_ride_fare")
    val subTotalRideFare: String? = null,
    @SerializedName("arrived_at")
    val arrivedAt: String? = null,
    @SerializedName("calculated_driver_fare")
    val calculatedDriverFare: String? = null,
    @SerializedName("distance_travelled")
    val distanceTravelled: String? = null,
    @SerializedName("driver_accept_latitude")
    val driverAcceptLatitude: String? = null,
    @SerializedName("driver_accept_longitude")
    val driverAcceptLongitude: String? = null,
    @SerializedName("driver_rating")
    val driverRating: String? = null,
    @SerializedName("drop_latitude")
    val dropLatitude: String? = null,
    @SerializedName("drop_location_address")
    val dropLocationAddress: String? = null,
    @SerializedName("drop_longitude")
    val dropLongitude: String? = null,
    @SerializedName("drop_time")
    val dropTime: String? = null,
    @SerializedName("pickup_latitude")
    val pickupLatitude: String? = null,
    @SerializedName("pickup_location_address")
    val pickupLocationAddress: String? = null,
    @SerializedName("pickup_longitude")
    val pickupLongitude: String? = null,
    @SerializedName("pickup_time")
    val pickupTime: String? = null,
    @SerializedName("ride_time")
    val rideTime: String? = null,
    @SerializedName("wait_time")
    val waitTime: String? = null,
    @SerializedName("total_fare")
    val totalFare: String? = null,
    @SerializedName("customer_name")
    val customerName: String? = null,
    @SerializedName("service_type")
    val serviceType: Int? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("delivery_packages")
    val deliveryPackages: List<OngoingPackages>? = null
)