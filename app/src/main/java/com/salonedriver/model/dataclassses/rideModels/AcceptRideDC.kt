package com.salonedriver.model.dataclassses.rideModels


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class AcceptRideDC(
    @SerializedName("address")
    val address: String? = null,
    @SerializedName("currency")
    val currency: String? = null,
    @SerializedName("current_latitude")
    val currentLatitude: String? = null,
    @SerializedName("current_longitude")
    val currentLongitude: String? = null,
    @SerializedName("customer_data")
    val customerData: CustomerData? = null,
    @SerializedName("distance_unit")
    val distanceUnit: String? = null,
    @SerializedName("fare_details")
    val fareDetails: FareDetails? = null,
    @SerializedName("tripId")
    val tripId: String? = null
) {
    @Keep
    data class CustomerData(
        @SerializedName("address")
        val address: String? = null,
        @SerializedName("customer_id")
        val customerId: String? = null,
        @SerializedName("customer_image")
        val customerImage: String? = null,
        @SerializedName("customer_name")
        val customerName: String? = null,
        @SerializedName("customer_rating")
        val customerRating: String? = null,
        @SerializedName("phone_no")
        val phoneNo: String? = null,
        @SerializedName("venuc_balance")
        val venucBalance: String? = null
    )

    @Keep
    data class FareDetails(
        @SerializedName("display_base_fare")
        val displayBaseFare: String? = null,
        @SerializedName("fare_fixed")
        val fareFixed: String? = null,
        @SerializedName("fare_minimum")
        val fareMinimum: String? = null,
        @SerializedName("fare_per_km")
        val farePerKm: String? = null,
        @SerializedName("id")
        val id: String? = null
    )
}