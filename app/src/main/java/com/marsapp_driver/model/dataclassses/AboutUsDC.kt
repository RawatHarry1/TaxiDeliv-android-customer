package com.marsapp_driver.model.dataclassses


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class AboutUsDC(
    @SerializedName("facebook_url")
    val facebookUrl: String? = null,
    @SerializedName("legal_url")
    val legalUrl: String? = null,
    @SerializedName("privacy_policy")
    val privacyPolicy: String? = null,
    @SerializedName("support_email")
    val supportEmail: String? = null,
    @SerializedName("who_we_are")
    val whoWeAre: String? = null
)