package com.superapp_customer.model.dataClass

import androidx.annotation.Keep

@Keep
data class ScheduleList(
    val drop_location_address: String,
    val latitude: Double,
    val longitude: Double,
    val modifiable: Int,
    val op_drop_latitude: Double,
    val op_drop_longitude: Double,
    val pickup_id: Int,
    val pickup_location_address: String,
    val pickup_time: String,
    val preferred_payment_mode: Int,
    val ride_type: Int,
    var status: Int,
    var service_type: Int = 1,
    val vehicle_name: String,
    val is_for_rental: String?,
    val start_time: String?,
    val end_time: String?
)