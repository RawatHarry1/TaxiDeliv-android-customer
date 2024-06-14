package com.venus_customer.firebaseSetup

import android.os.Parcelable
import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class NotificationModel(
    @SerializedName("trip_id")
    var tripId: String? = null,
    @SerializedName("customer_id")
    val customerId: String? = null,
    @SerializedName("driver_id")
    var driverId: String? = null,
    @SerializedName("latitude")
    val latitude: String? = null,
    @SerializedName("longitude")
    val longitude: String? = null,
    @SerializedName("pickup_address")
    val pickupAddress: String? = null,
    @SerializedName("drop_address")
    val dropAddress: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("driver_name")
    val driverName: String? = null,
    @SerializedName("driver_image")
    val driverImage: String? = null,
    @SerializedName("driver_rating")
    val driverRating: String? = null,
    @SerializedName("license_plate")
    val licensePlate: String? = null,
    @SerializedName("brand")
    val brand: String? = null,
    @SerializedName("model_name")
    val modelName: String? = null,
    @SerializedName("image")
    val image: String? = null,
    @SerializedName("session_id")
    val sessionId: String? = null,
) : Parcelable