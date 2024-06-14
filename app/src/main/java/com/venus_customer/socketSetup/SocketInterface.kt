package com.venus_customer.socketSetup

import com.google.android.gms.maps.model.LatLng

interface SocketInterface {

    fun driverLocation(latLng: LatLng, bearing: Float){}

}


object SocketKeys {
    const val CUSTOMER_TRACKING = "customer-tracking"

    const val DRIVER_LOCATION_LISTENER = "tracking-driver-location"
}