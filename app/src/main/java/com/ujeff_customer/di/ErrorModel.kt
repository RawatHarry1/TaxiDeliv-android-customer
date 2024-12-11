package com.ujeff_customer.di

import com.google.gson.annotations.SerializedName

    data class ErrorModel(
        @field:SerializedName("message")
        var message: String? = null,
        @field:SerializedName("statusCode")
        var statusCode: String? = null,
        @field:SerializedName("type")
        var type: String? = null
    )
