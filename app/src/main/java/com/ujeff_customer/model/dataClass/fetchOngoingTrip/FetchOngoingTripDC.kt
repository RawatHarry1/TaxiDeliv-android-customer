package com.ujeff_customer.model.dataClass.fetchOngoingTrip


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.ujeff_customer.viewmodel.rideVM.CreateRideData

@Keep
data class FetchOngoingTripDC(
    @SerializedName("trips")
    val trips: List<Trip?>? = null,
    @SerializedName("deliveryPackages")
    val deliveryPackages: List<CreateRideData.Package>? = null
) {
    @Keep
    data class Trip(
        @SerializedName("brand")
        val brand: String? = null,
        @SerializedName("currency")
        val currency: String? = null,
        @SerializedName("current_time")
        val currentTime: String? = null,
        @SerializedName("customer_id")
        val customerId: String? = null,
        @SerializedName("date")
        val date: String? = null,
        @SerializedName("driver_id")
        val driverId: String? = null,
        @SerializedName("driver_image")
        val driverImage: String? = null,
        @SerializedName("driver_name")
        val driverName: String? = null,
        @SerializedName("driver_rating")
        val driverRating: String? = null,
        @SerializedName("driver_phone_no")
        val driverPhoneNo: String? = null,
        @SerializedName("drop_address")
        val dropAddress: String? = null,
        @SerializedName("dry_eta")
        val dryEta: String? = null,
        @SerializedName("estimated_distance")
        val estimatedDistance: String? = null,
        @SerializedName("estimated_driver_fare")
        val estimatedDriverFare: String? = null,
        @SerializedName("image")
        val image: String? = null,
        @SerializedName("latitude")
        val latitude: String? = null,
        @SerializedName("license_plate")
        val licensePlate: String? = null,
        @SerializedName("longitude")
        val longitude: String? = null,
        @SerializedName("model_name")
        val modelName: String? = null,
        @SerializedName("pickup_address")
        val pickupAddress: String? = null,
        @SerializedName("status")
        val status: Int? = null,
        @SerializedName("trip_id")
        val tripId: String? = null,
        @SerializedName("session_id")
        val sessionId: String? = null,
        @SerializedName("paystack_reference")
        val paystackReference: String? = null,
        @SerializedName("service_type")
        val serviceType: Int? = null,
        @SerializedName("request_drop_latitude")
        val dropLatitude: String? = null,
        @SerializedName("request_drop_longitude")
        val dropLongitude: String? = null,
        @SerializedName("driver_current_latitude")
        val driverCurrentLatitude: String? = null,
        @SerializedName("driver_current_longitude")
        val driverCurrentLongitude: String? = null
    )
}