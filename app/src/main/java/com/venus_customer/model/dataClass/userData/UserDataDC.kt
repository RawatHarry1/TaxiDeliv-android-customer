package com.venus_customer.model.dataClass.userData


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

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
        val city: String? = null
    ) {
        @Keep
        data class DriverDocumentStatus(
            @SerializedName("error")
            val error: String? = null,
            @SerializedName("requiredDocsStatus")
            val requiredDocsStatus: String? = null
        )
    }
}