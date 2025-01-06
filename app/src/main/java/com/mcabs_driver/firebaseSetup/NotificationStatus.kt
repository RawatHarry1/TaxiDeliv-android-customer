package com.mcabs_driver.firebaseSetup

enum class NotificationStatus(val type: Int) {
    NEW_RIDE(0),
    SCHEDULE_RIDE(126),
    TIME_OUT_RIDE(2),
    WALLET_UPDATE(120),
    CHAT(600)
}