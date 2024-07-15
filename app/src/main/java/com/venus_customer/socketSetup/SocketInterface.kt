package com.venus_customer.socketSetup

import com.google.android.gms.maps.model.LatLng
import com.venus_customer.model.dataClass.MessageData

interface SocketInterface {

    fun driverLocation(latLng: LatLng, bearing: Float){}
    fun driverMessage(message: MessageData){}
    fun allMessages(messages: ArrayList<MessageData>){}

}


object SocketKeys {
    const val CUSTOMER_TRACKING = "customer-tracking"
    const val SEND_MESSAGE= "send_message"
    const val GET_ALL_MESSAGE= "list_of_message"
    const val DRIVER_LOCATION_LISTENER = "tracking-driver-location"
}