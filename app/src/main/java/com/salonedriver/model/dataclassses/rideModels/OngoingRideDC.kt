package com.salonedriver.model.dataclassses.rideModels


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.salonedriver.firebaseSetup.NewRideNotificationDC

@Keep
data class OngoingRideDC(
    @SerializedName("is_driver_online")
    val isDriverOnline: Int? = null,
    @SerializedName("trips")
    val trips: List<NewRideNotificationDC?>? = null
)