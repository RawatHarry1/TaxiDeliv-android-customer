package com.superapp_customer.model.dataClass.tripsDC


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class TripListDC(
    @SerializedName("pickup_time")
    val pickupTime: String? = null,
    @SerializedName("drop_time")
    val dropTime: String? = null,
    @SerializedName("amount")
    val amount: String? = null,
    @SerializedName("autos_status")
    val autosStatus: String? = null,
    @SerializedName("autos_status_color")
    val autosStatusColor: String? = null,
    @SerializedName("autos_status_text")
    val autosStatusText: String? = null,
    @SerializedName("cancellation_charges")
    val cancellationCharges: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("currency")
    val currency: String? = null,
    @SerializedName("currency_symbol")
    val currencySymbol: String? = null,
    @SerializedName("customer_fare_factor")
    val customerFareFactor: String? = null,
    @SerializedName("date")
    val date: String? = null,
    @SerializedName("distance")
    val distance: String? = null,
    @SerializedName("distance_unit")
    val distanceUnit: String? = null,
    @SerializedName("driver_id")
    val driverId: String? = null,
    @SerializedName("driver_rating")
    val driverRating: Int? = null,
    @SerializedName("drop_address")
    val dropAddress: String? = null,
    @SerializedName("drop_latitude")
    val dropLatitude: String? = null,
    @SerializedName("drop_longitude")
    val dropLongitude: String? = null,
    @SerializedName("engagement_id")
    val engagementId: String? = null,
    @SerializedName("images")
    val images: String? = null,
    @SerializedName("is_cancelled_ride")
    val isCancelledRide: String? = null,
    @SerializedName("is_rated_before")
    val isRatedBefore: String? = null,
    @SerializedName("manually_edited")
    val manuallyEdited: String? = null,
    @SerializedName("pickup_address")
    val pickupAddress: String? = null,
    @SerializedName("pickup_latitude")
    val pickupLatitude: String? = null,
    @SerializedName("pickup_longitude")
    val pickupLongitude: String? = null,
    @SerializedName("product_type")
    val productType: String? = null,
    @SerializedName("ride_time")
    val rideTime: String? = null,
    @SerializedName("ride_type")
    val rideType: String? = null,
    @SerializedName("user_id")
    val userId: String? = null,
    @SerializedName("utc_offset")
    val utcOffset: String? = null,
    @SerializedName("vehicle_type")
    val vehicleType: String? = null,
    @SerializedName("wait_time")
    val waitTime: String? = null
)