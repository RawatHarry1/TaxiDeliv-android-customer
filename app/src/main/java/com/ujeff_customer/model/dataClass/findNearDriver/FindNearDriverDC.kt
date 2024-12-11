package com.ujeff_customer.model.dataClass.findNearDriver

import com.google.gson.annotations.SerializedName

data class FindNearDriverDC(
    @SerializedName("drivers")
    val drivers: List<Driver>? = null
)