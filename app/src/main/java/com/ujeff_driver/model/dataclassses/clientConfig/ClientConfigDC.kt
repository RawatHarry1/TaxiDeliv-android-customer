package com.ujeff_driver.model.dataclassses.clientConfig


import androidx.annotation.Keep
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
    @SerializedName("google_map_keys")
    val googleMapKey: String? = null,
    @SerializedName("mandatory_registration_steps")
    val mandatoryRegistrationSteps: MandatoryRegistrationSteps? = null,
    @SerializedName("city_list")
    val cityList: ArrayList<CountryList>? = ArrayList(),
    @SerializedName("operator_availablity")
    val operatorAvailablity: List<OperatorAvailablity>? = null,
    @SerializedName("enabled_service")
    val enabledService: Int? = null
)

@Keep
data class OperatorAvailablity(
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("image")
    val image: String? = null,
    @SerializedName("name")
    val name: String? = null
)

@Keep
data class MandatoryRegistrationSteps(
    val is_bank_details_mandatory: Boolean,
    val is_document_upload_mandatory: Boolean,
    val is_profile_mandatory: Boolean,
    val is_vehicle_info_mandatory: Boolean
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
    val isGenderEnabled: String? = null,
    @SerializedName("package_delivery_restriction_enabled")
    val packageDeliveryRestrictionEnabled: Int? = null,
    @SerializedName("maximum_distance")
    val maximumDistance: String? = null,
    @SerializedName("operator_available")
    val operatorAvailable: List<Int>? = null
)