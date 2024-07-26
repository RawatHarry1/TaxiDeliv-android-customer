package com.venus_customer.viewmodel.rideVM

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
    var status: Int? = null
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
        var driverRating: String? = null )
}
