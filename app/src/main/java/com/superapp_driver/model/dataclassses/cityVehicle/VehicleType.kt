package com.superapp_driver.model.dataclassses.cityVehicle


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class VehicleType(
    @SerializedName("vehicle_type")
    val vehicleType: String? = null,
    @SerializedName("vehicle_type_name")
    val vehicleTypeName: String? = null,
    @SerializedName("isSelected")
    var isSelected: Boolean = false
)