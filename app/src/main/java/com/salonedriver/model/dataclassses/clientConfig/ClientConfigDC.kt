package com.salonedriver.model.dataclassses.clientConfig


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
    @SerializedName("operatorToken")
    val operatorToken: String? = null,
    @SerializedName("update_location_timmer")
    val updateLocationTimer: Float? = null,
    @SerializedName("show_facebook_login")
    val showFacebookLogin: String? = null,
    @SerializedName("show_google_login")
    val showGoogleLogin: String? = null,
    @SerializedName("show_terms")
    val showTerms: String? = null,
    @SerializedName("terms_of_use_url")
    val termsOfUseUrl: String? = null,
    @SerializedName("default_country_iso")
    val defaultCountryIso: String? = null,
    @SerializedName("operator_id")
    val operatorId: String? = null,
    @SerializedName("city_list")
    val cityList: ArrayList<CountryList>? = ArrayList()
)


data class CountryList(
    @SerializedName("city_id")
    val cityId: String? = null,
    @SerializedName("city_name")
    val cityName: String? = null,
    @SerializedName("mandatory_fleet_registration")
    val mandatoryFleetRegistration: String? = null,
    @SerializedName("elm_verification_enabled")
    val elmVerificationEnabled: String? = null,
    @SerializedName("vehicle_model_enabled")
    val vehicleModelEnabled: String? = null,
    @SerializedName("is_gender_enabled")
    val isGenderEnabled: String? = null
)