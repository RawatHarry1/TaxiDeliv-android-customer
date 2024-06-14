package com.venus_customer.model.dataClass.base

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    @SerializedName("responseCode")
    var statusCode: Int = 0,
    @SerializedName("flag")
    var flag: Int = 0,
    @SerializedName("message", alternate = ["error"])
    var message: String = "",
    @SerializedName("data", alternate = ["rides"])
    var data: T? = null
)