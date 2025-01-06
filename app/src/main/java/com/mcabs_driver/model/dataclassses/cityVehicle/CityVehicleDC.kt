package com.mcabs_driver.model.dataclassses.cityVehicle


import com.google.gson.annotations.SerializedName

data class CityVehicleDC(
    @SerializedName("colors")
    val colors: List<Color>? = null,
    @SerializedName("vehicles")
    val vehicles: List<Vehicle>? = null,
    @SerializedName("vehicle_type")
    val vehicleType: List<VehicleType>? = null
)