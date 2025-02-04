package com.marsapp_driver.model.dataclassses.base


import com.google.gson.annotations.SerializedName

data class ClientConfigDC(
    @SerializedName("client_id")
    val clientId: String? = null,
    @SerializedName("default_country_code")
    val defaultCountryCode: String? = null,
    @SerializedName("default_lang")
    val defaultLang: String? = null,
    @SerializedName("locale")
    val locale: String? = null,
    @SerializedName("login_channel")
    val loginChannel: String? = null,
    @SerializedName("operator_token")
    val operatorToken: String? = null,
    @SerializedName("show_facebook_login")
    val showFacebookLogin: String? = null,
    @SerializedName("show_google_login")
    val showGoogleLogin: String? = null,
    @SerializedName("show_terms")
    val showTerms: String? = null,
    @SerializedName("terms_of_use_url")
    val termsOfUseUrl: String? = null
)