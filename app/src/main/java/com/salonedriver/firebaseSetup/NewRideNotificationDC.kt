package com.salonedriver.firebaseSetup

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class NewRideNotificationDC(
    @SerializedName("customer_name", alternate = ["customerName"])
    var customerName: String? = null,
    @SerializedName("customer_image", alternate = ["customerImage"])
    var customerImage: String? = null,
    @SerializedName("customer_id", alternate = ["user_id"])
    var customerId: String? = null,
    @SerializedName("pickup_address", alternate = ["pickup_location_address"])
    var pickUpAddress: String? = null,
    @SerializedName("start_latitude", alternate = ["latitude","pickup_latitude"])
    var latitude: String? = null,
    @SerializedName("trip_id", alternate = ["engagement_id"])
    var tripId: String? = null,
    @SerializedName("estimated_driver_fare", alternate = ["fare"])
    var estimatedDriverFare: String? = null,
    @SerializedName("start_longitude", alternate = ["longitude","pickup_longitude"])
    var longitude: String? = null,
    @SerializedName("currency")
    var currency: String? = null,
    @SerializedName("estimated_distance")
    var estimatedDistance: String? = null,
    @SerializedName("drop_address", alternate = ["drop_location_address"])
    var dropAddress: String? = null,
    @SerializedName("drop_latitude")
    var dropLatitude: String? = null,
    @SerializedName("drop_longitude")
    var dropLongitude: String? = null,
    @SerializedName("dry_eta")
    var dryEta: String? = null,
    @SerializedName("status")
    var status: Int? = null,
    @SerializedName("distance_travelled")
    var distanceTravelled: String? = null,
    @SerializedName("ride_time")
    var rideTime: String? = null,
    @SerializedName("wait_time")
    var waitTime: String? = null,
    @SerializedName("distanceUnit")
    var distanceUnit: String? = null,
    @SerializedName("date", alternate = ["driver_ride_date"])
    var date: String? = null,
): Parcelable