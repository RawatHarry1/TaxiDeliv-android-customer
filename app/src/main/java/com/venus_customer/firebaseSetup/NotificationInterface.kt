package com.venus_customer.firebaseSetup

interface NotificationInterface {
    fun acceptRide(){}
    fun rideStarted(){}
    fun rideEnd(tripId: String, driverId: String){}
    fun requestTimeout(){}

    fun callFetchRideApi(){}
    fun rideRejectedByDriver(){}
}