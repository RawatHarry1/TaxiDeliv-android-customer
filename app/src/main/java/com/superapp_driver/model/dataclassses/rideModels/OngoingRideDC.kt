package com.superapp_driver.model.dataclassses.rideModels


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.superapp_driver.firebaseSetup.NewRideNotificationDC

@Keep
data class OngoingRideDC(
    @SerializedName("is_driver_online")
    val isDriverOnline: Int? = null,
    @SerializedName("can_start")
    val canStart: Int? = null,
    @SerializedName("can_end")
    val canEnd: Int? = null,
    @SerializedName("trips")
    val trips: List<NewRideNotificationDC?>? = null,
    @SerializedName("deliveryPackages")
    val deliveryPackages: List<OngoingPackages>? = null
)

@Keep
data class OngoingPackages(
    val notes: Any,
    val package_id: Int,
    val package_image_while_drop_off: List<String>,
    val package_image_while_pickup: List<String>,
    val package_images_by_customer: List<String>,
    val package_size: String,
    val package_type: String,
    val package_quantity: Int,
    val delivery_status: Int
)