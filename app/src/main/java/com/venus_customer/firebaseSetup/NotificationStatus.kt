package com.venus_customer.firebaseSetup

enum class NotificationStatus(val type: Int) {
    NEW_RIDE(0),
    TIME_OUT_RIDE(2),
    WALLET_UPDATE(120),
    REQUEST(0), //Driver
    REQUEST_TIMEOUT(1), //Customer
    REQUEST_CANCELLED(2), //Driver
    RIDE_STARTED(3), //Customer
    RIDE_ENDED(4), //Customer
    RIDE_ACCEPTED(5), //Customer
    GO_TO_PICKUP(72),//Customer
    RIDE_REJECTED_BY_DRIVER(7) ,//Customer
    CHAT(600)
}