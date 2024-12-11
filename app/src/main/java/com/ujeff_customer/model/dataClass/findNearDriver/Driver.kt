package com.ujeff_customer.model.dataClass.findNearDriver

import com.google.gson.annotations.SerializedName

data class Driver(
    @SerializedName("app_versioncode")
    val appVersionCode: String? = null,
    @SerializedName("audit_status")
    val auditStatus: String? = null,
    @SerializedName("battery_percentage")
    val batteryPercentage: String? = null,
    @SerializedName("bearing")
    val bearing: String? = null,
    @SerializedName("captive_driver_enabled")
    val captiveDriverEnabled: String? = null,
    @SerializedName("car_no")
    val carNo: String? = null,
    @SerializedName("city_id")
    val cityId: String? = null,
    @SerializedName("customers_rated")
    val customersRated: String? = null,
    @SerializedName("device_token")
    val deviceToken: String? = null,
    @SerializedName("device_type")
    val deviceType: String? = null,
    @SerializedName("distance")
    val distance: String? = null,
    @SerializedName("driver_id")
    val driverId: String? = null,
    @SerializedName("driver_image")
    val driverImage: String? = null,
    @SerializedName("latitude")
    val latitude: Double? = null,
    @SerializedName("longitude")
    val longitude: Double? = null,
    @SerializedName("status")
    val status: Double? = null
)