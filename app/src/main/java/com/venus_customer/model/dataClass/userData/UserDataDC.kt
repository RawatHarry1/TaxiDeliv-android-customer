package com.venus_customer.model.dataClass.userData


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class UserDataDC(
    @SerializedName("access_token")
    val accessToken: String? = null,
    @SerializedName("flag")
    val flag: Int? = null,
    @SerializedName("login")
    val login: Login? = null
) {
    @Keep
    data class Login(
        @SerializedName("cancellation_reasons")
        val cancellationReasons: List<String?>? = null,
        @SerializedName("country_code")
        val countryCode: Int? = null,
        @SerializedName("currency")
        val currency: String? = null,
        @SerializedName("currency_symbol")
        val currencySymbol: String? = null,
        @SerializedName("deactivation_reasons_driver")
        val deactivationReasonsDriver: String? = null,
        @SerializedName("driver_document_status")
        val driverDocumentStatus: DriverDocumentStatus? = null,
        @SerializedName("driver_subscription")
        val driverSubscription: Int? = null,
        @SerializedName("driver_traction_api_interval")
        val driverTractionApiInterval: Any? = null,
        @SerializedName("enable_vehicle_edit_setting")
        val enableVehicleEditSetting: Any? = null,
        @SerializedName("incentive_enabled")
        val incentiveEnabled: Int? = null,
        @SerializedName("mlm_jul_enabled")
        val mlmJulEnabled: Int? = null,
        @SerializedName("multiple_vehicles_enabled")
        val multipleVehiclesEnabled: Int? = null,
        @SerializedName("phone_no")
        val phoneNo: String? = null,
        @SerializedName("req_inactive_drivers")
        val reqInactiveDrivers: Any? = null,
        @SerializedName("show_bank_list")
        val showBankList: Int? = null,
        @SerializedName("show_payouts")
        val showPayouts: Int? = null,
        @SerializedName("show_wallet")
        val showWallet: Int? = null,
        @SerializedName("user_email")
        val userEmail: String? = null,
        @SerializedName("address")
        val address: String? = null,
        @SerializedName("user_id")
        val userId: Int? = null,
        @SerializedName("user_name")
        val userName: String? = null,
        @SerializedName("first_name")
        val firstName: String? = null,
        @SerializedName("last_name")
        val lastName: String? = null,
        @SerializedName("user_image")
        val userImage: String? = null,
        @SerializedName("vehicle_model_enabled")
        val vehicleModelEnabled: Any? = null,
        @SerializedName("is_customer_profile_complete")
        val isCustomerProfileComplete: Int? = null,
        @SerializedName("city", alternate = ["city_id"])
        val city: String? = null,
        @SerializedName("user_ratings")
        val userRating: String? = null,
        @SerializedName("referral_code")
        val referralCode: String? = null,
        @SerializedName("referral_link")
        val referralLink: String? = null,
        @SerializedName("stripeCredentials")
        val stripeCredentials: StripeCredentials? = null,
        @SerializedName("referral_data")
        val referralData: ReferralData? = null,
        @SerializedName("popup")
        val popup: Popup? = null
    ) {
        @Keep
        data class DriverDocumentStatus(
            @SerializedName("error")
            val error: String? = null,
            @SerializedName("requiredDocsStatus")
            val requiredDocsStatus: String? = null
        )

        @Keep
        data class StripeCredentials(
            @SerializedName("publishable_key")
            val publishableKey: String? = null,
            @SerializedName("client_secret")
            val clientSecret: String? = null
        )

        @Keep
        data class ReferralData(
            val branch_android_url: String,
            val branch_desktop_url: String,
            val branch_fallback_url: String,
            val branch_ios_url: String,
            val fb_share_caption: String,
            val fb_share_description: String,
            val invite_earn_more_info: String,
            val invite_earn_short_msg: String,
            val referral_caption: String,
            val referral_cashback_popup_text: String,
            val referral_email_subject: String,
            val referral_message: String,
            val referral_sharing_message: String,
            val referral_image_d2c: String? = null,
            val referral_image_d2d: String? = null
        )

        @Keep
        data class Popup(
            val download_link: String?,
            val force_to_version: Int?,
            val is_force: Int?,
            val popup_text: String?
        )
    }
}


