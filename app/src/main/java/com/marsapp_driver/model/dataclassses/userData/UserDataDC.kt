package com.marsapp_driver.model.dataclassses.userData


import com.google.gson.annotations.SerializedName

data class UserDataDC(
    @SerializedName("access_token")
    val accessToken: String? = null,
    @SerializedName("flag")
    val flag: Int? = null,
    @SerializedName("login")
    var login: Login? = null
)