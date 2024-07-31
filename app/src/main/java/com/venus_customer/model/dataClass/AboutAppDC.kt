package com.venus_customer.model.dataClass


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class AboutAppDC(
    @SerializedName("facebook_url")
    val facebookUrl: String? = null,
    @SerializedName("legal_url")
    val legalUrl: String? = null,
    @SerializedName("privacy_policy")
    val privacyPolicy: String? = null,
    @SerializedName("support_email")
    val supportEmail: String? = null,
    @SerializedName("who_we_are")
    val whoWeAre: String? = null,
    @SerializedName("faq")
    val faq: List<FaqX>? = null
)
@Keep
data class FaqX(
    @SerializedName("Ans")
    val ans: String? = null,
    @SerializedName("Ques")
    val ques: String? = null,
    @SerializedName("isShown")
    var isShown: Boolean? = false
)