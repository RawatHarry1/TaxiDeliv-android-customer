package com.marsapp_driver.model.dataclassses.changeStatus


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class ChangeStatusDC(
    @SerializedName("docStatus")
    val docStatus: String? = null,
    @SerializedName("autos_available")
    val autosAvailable: Int = 0
)