package com.superapp_customer.firebaseSetup

interface NotificationInterface {
    fun acceptRide(){}
    fun rideStarted(){}
    fun rideEnd(tripId: String, driverId: String,driverName:String,engagementId:String){}
    fun requestTimeout(msg:String){}

    fun callFetchRideApi(){}
    fun rideRejectedByDriver(){}
}