package com.salonedriver.socketSetup

import com.salonedriver.model.dataclassses.MessageData

interface SocketInterface {
    fun onCustomerMsg(msg: MessageData) {}
    fun allMsg(msgs: ArrayList<MessageData>) {}
}


object SocketKeys {
    const val DRIVER_TRACKING = "driver-tracking"
    const val GET_ALL_MESSAGE= "list_of_message"
    const val SEND_MESSAGE = "send_message"

}