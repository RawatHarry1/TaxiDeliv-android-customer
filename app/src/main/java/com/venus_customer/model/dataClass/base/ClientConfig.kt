package com.venus_customer.model.dataClass.base


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class ClientConfig(
    @SerializedName("city_list")
    val cityList: List<City?>? = null,
    @SerializedName("client_id")
    val clientId: String? = null,
    @SerializedName("default_country_code")
    val defaultCountryCode: String? = null,
    @SerializedName("default_country_iso")
    val defaultCountryIso: String? = null,
    @SerializedName("default_lang")
    val defaultLang: String? = null,
    @SerializedName("information_urls")
    val informationUrls: InformationUrls? = null,
    @SerializedName("locale")
    val locale: String? = null,
    @SerializedName("login_channel")
    val loginChannel: String? = null,
    @SerializedName("mandatory_registration_steps")
    val mandatoryRegistrationSteps: MandatoryRegistrationSteps? = null,
    @SerializedName("operator_id")
    val operatorId: Int? = null,
    @SerializedName("operator_token")
    val operatorToken: String? = null,
    @SerializedName("registration_steps")
    val registrationSteps: RegistrationSteps? = null,
    @SerializedName("show_facebook_login")
    val showFacebookLogin: String? = null,
    @SerializedName("show_google_login")
    val showGoogleLogin: String? = null,
    @SerializedName("show_terms")
    val showTerms: String? = null,
    @SerializedName("terms_of_use_url")
    val termsOfUseUrl: String? = null
) {
    @Keep
    data class City(
        @SerializedName("bank_list")
        val bankList: Any? = null,
        @SerializedName("city_id")
        val cityId: Int? = null,
        @SerializedName("city_name")
        val cityName: String? = null,
        @SerializedName("config_json")
        val configJson: String? = null,
        @SerializedName("elm_verification_enabled")
        val elmVerificationEnabled: Int? = null,
        @SerializedName("is_gender_enabled")
        val isGenderEnabled: Int? = null,
        @SerializedName("mandatory_fleet_registration")
        val mandatoryFleetRegistration: Int? = null,
        @SerializedName("vehicle_model_enabled")
        val vehicleModelEnabled: Int? = null
    )

    @Keep
    data class InformationUrls(
        @SerializedName("aboutUs")
        val aboutUs: AboutUs? = null,
        @SerializedName("appstore")
        val appstore: Appstore? = null,
        @SerializedName("contactUs")
        val contactUs: ContactUs? = null,
        @SerializedName("facebook")
        val facebook: Facebook? = null,
        @SerializedName("faq")
        val faq: Faq? = null,
        @SerializedName("instagram")
        val instagram: Instagram? = null,
        @SerializedName("playstore")
        val playstore: Playstore? = null,
        @SerializedName("privacyPolicy")
        val privacyPolicy: PrivacyPolicy? = null
    ) {
        @Keep
        data class AboutUs(
            @SerializedName("isHide")
            val isHide: Int? = null,
            @SerializedName("url")
            val url: String? = null
        )

        @Keep
        data class Appstore(
            @SerializedName("isHide")
            val isHide: Int? = null,
            @SerializedName("url")
            val url: String? = null
        )

        @Keep
        data class ContactUs(
            @SerializedName("isHide")
            val isHide: Int? = null,
            @SerializedName("url")
            val url: String? = null
        )

        @Keep
        data class Facebook(
            @SerializedName("isHide")
            val isHide: Int? = null,
            @SerializedName("url")
            val url: String? = null
        )

        @Keep
        data class Faq(
            @SerializedName("isHide")
            val isHide: Int? = null,
            @SerializedName("url")
            val url: String? = null
        )

        @Keep
        data class Instagram(
            @SerializedName("isHide")
            val isHide: Int? = null,
            @SerializedName("url")
            val url: String? = null
        )

        @Keep
        data class Playstore(
            @SerializedName("isHide")
            val isHide: Int? = null,
            @SerializedName("url")
            val url: String? = null
        )

        @Keep
        data class PrivacyPolicy(
            @SerializedName("isHide")
            val isHide: Int? = null,
            @SerializedName("url")
            val url: String? = null
        )
    }

    @Keep
    data class MandatoryRegistrationSteps(
        @SerializedName("is_bank_details_mandatory")
        val isBankDetailsMandatory: Boolean? = null,
        @SerializedName("is_document_upload_mandatory")
        val isDocumentUploadMandatory: Boolean? = null,
        @SerializedName("is_profile_mandatory")
        val isProfileMandatory: Boolean? = null,
        @SerializedName("is_vehicle_info_mandatory")
        val isVehicleInfoMandatory: Boolean? = null
    )

    @Keep
    data class RegistrationSteps(
        @SerializedName("bank_details")
        val bankDetails: Int? = null,
        @SerializedName("document_upload")
        val documentUpload: Int? = null,
        @SerializedName("profile")
        val profile: Int? = null,
        @SerializedName("vehicle_info")
        val vehicleInfo: Int? = null
    )
}