package com.superapp_customer.viewmodel.rideVM

import android.os.Parcelable
import com.google.errorprone.annotations.Keep
import kotlinx.parcelize.Parcelize

@Keep
data class CreateRideData(
    var regionId: String? = null,
    var vehicleType: String? = null,
    var sessionId: String? = null,
    var currencyCode: String? = null,
    var tripId: String? = null,
    var customerId: String? = null,
    var vehicleData: VehicleData? = VehicleData(),
    var driverDetail: DriverDetail? = DriverDetail(),
    var pickUpLocation: LocationData? = LocationData(),
    var dropLocation: LocationData? = LocationData(),
    var driverLocation: DriverLocation? = DriverLocation(),
    var status: Int? = null,
    var deliveryPackages: List<Package>? = null
) {

    @Parcelize
    data class DriverLocation(
        var latitude: String? = null,
        var longitude: String? = null,
    ) : Parcelable

    @Parcelize
    data class LocationData(
        var latitude: String? = null,
        var longitude: String? = null,
        var address: String? = null,
        var placeId: String? = null
    ) : Parcelable

    data class VehicleData(
        var image: String? = null,
        var name: String? = null,
        var totalCapacity: String? = null,
        var eta: String? = null,
        var distance: String? = null,
        var fare: String? = null,
        var discount: Double? = null,
        var original_fare: Double? = null,
        var currency: String? = null,
        var vehicleNumber: String? = null,
    )

    data class DriverDetail(
        var driverImage: String? = null,
        var driverName: String? = null,
        var driverId: String? = null,
        var driverPhoneNo: String? = null,
        var driverRating: String? = null
    )

    data class Package(
        val delivery_status: Int,
        val notes: Any,
        val package_id: Int,
        val package_image_while_drop_off: List<String>,
        val package_image_while_pickup: List<String>,
        val package_images_by_customer: List<String>,
        val package_quantity: Int,
        val package_size: String,
        val package_type: String
    )
}
