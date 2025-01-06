package com.mcabs_driver.model.dataclassses.cityVehicle


import com.google.gson.annotations.SerializedName

data class Color(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("value")
    val value: String? = null,
    @SerializedName("isSelected")
    var isSelected: Boolean? = null
)