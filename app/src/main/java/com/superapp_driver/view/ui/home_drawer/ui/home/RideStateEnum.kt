package com.superapp_driver.view.ui.home_drawer.ui.home

enum class RideStateEnum(val data: Int) {
    RIDE_ACCEPT(0),
    ARRIVE_AT_PICKUP(1), //Reach Pickup
    ON_THE_WAY(2), //Reach Destination
    END_TRIP(3) // Mark Complete
}