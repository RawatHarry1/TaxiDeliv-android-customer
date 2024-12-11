package com.ujeff_driver.model.dataclassses.cityVehicle


import com.google.gson.annotations.SerializedName

data class Vehicle(
    @SerializedName("brand")
    val brand: String? = null,
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("model_name")
    val modelName: String? = null,
    @SerializedName("isSelected")
    var isSelected: Boolean? = null,
    @SerializedName("vehicle_type")
    var vehicleType: String? = null
)