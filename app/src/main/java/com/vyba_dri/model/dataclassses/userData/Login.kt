package com.vyba_dri.model.dataclassses.userData


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

data class Login(
    @SerializedName("actual_credit_balance")
    var actualCreditBalance: String? = null,
    @SerializedName("cancellation_reasons")
    var cancellationReasons: ArrayList<String>? = ArrayList(),
    @SerializedName("delivery_cancellation_reasons")
    var deliveryCancellationReasons: ArrayList<String>? = ArrayList(),
    @SerializedName("city")
    var city: String? = null,
    @SerializedName("country_code")
    var countryCode: String? = null,
    @SerializedName("date_format")
    var dateFormat: String? = null,
    @SerializedName("deactivation_reasons_driver")
    var deactivationReasonsDriver: List<String?>? = null,
    @SerializedName("driver_dob_input")
    var driverDobInput: String? = null,
    @SerializedName("driver_gender_filter")
    var driverGenderFilter: String? = null,
    @SerializedName("driver_image")
    var driverImage: String? = null,
    @SerializedName("driver_rating")
    var driverRating: String? = null,
    @SerializedName("driver_subscription")
    var driverSubscription: String? = null,
    @SerializedName("driver_tag")
    var driverTag: String? = null,
    @SerializedName("driver_traction_api_intervar")
    var driverTractionApiIntervar: String? = null,
    @SerializedName("enable_vehicle_edit_setting")
    var enableVehicleEditSetting: String? = null,
    @SerializedName("incentive_enabled")
    var incentiveEnabled: String? = null,
    @SerializedName("min_driver_balance")
    var minDriverBalance: String? = null,
    @SerializedName("mlm_jul_enabled")
    var mlmJulEnabled: String? = null,
    @SerializedName("multiple_vehicles_enabled")
    var multipleVehiclesEnabled: String? = null,
    @SerializedName("ownership_status")
    var ownershipStatus: String? = null,
    @SerializedName("phone_no")
    var phoneNo: String? = null,
    @SerializedName("req_inactive_drivers")
    var reqInactiveDrivers: String? = null,
    @SerializedName("show_bank_list")
    var showBankList: String? = null,
    @SerializedName("show_payouts")
    var showPayouts: String? = null,
    @SerializedName("show_wallet")
    var showWallet: String? = null,
    @SerializedName("user_email")
    var userEmail: String? = null,
    @SerializedName("user_id")
    var userId: String? = null,
    @SerializedName("user_name", alternate = ["name"])
    var userName: String? = null,
    @SerializedName("vehicle_model_enabled")
    var vehicleModelEnabled: String? = null,
    @SerializedName("vehicle_no")
    var vehicleNo: String? = null,
    @SerializedName("vehicle_type")
    var vehicleType: String? = null,
    @SerializedName("vehicle_year")
    var vehicleYear: String? = null,
    @SerializedName("address")
    var address: String? = null,
    @SerializedName("date_of_birth")
    var dateOfBirth: String? = null,
    @SerializedName("driver_id")
    var driverId: Int? = null,
    @SerializedName("email")
    var email: String? = null,
    @SerializedName("emergency_phone_number")
    var emergencyPhoneNumber: String? = null,
    @SerializedName("first_name")
    var firstName: String? = null,
    @SerializedName("last_name")
    var lastName: String? = null,
    @SerializedName("profile_image")
    var profileImage: String? = null,
    @SerializedName("is_registration_complete")
    var isRegistrationComplete: Boolean? = null,
    @SerializedName("is_threshold_reached")
    var isThresholdReached: Int = 0,
    @SerializedName("registration_step_completed")
    var registrationStepCompleted: RegistrationStepComplete? = RegistrationStepComplete(),
    @SerializedName("registration_steps")
    var registrationSteps: RegistrationSteps? = RegistrationSteps(),
    @SerializedName("make_details")
    var makeDetails: MakeDetails? = null,
    @SerializedName("currency_symbol")
    var currencySymbol: String? = null,
    @SerializedName("driver_document_status")
    var driverDocumentStatus: DriverDocumentStatus? = null,
    @SerializedName("driver_blocked_multiple_cancelation")
    var driverBlockedMultipleCancellation: DriverBlockedMultipleCancellation? = null,
    @SerializedName("popup")
    val popup: Popup? = null,
    @SerializedName("stripeCredentials")
    val stripeCredentials: StripeCredentials? = null,
    @SerializedName("service_type")
    val serviceType: Int? = null,
    @SerializedName("support_ticket_reasons")
    val supportTicketReasons: List<String>
)

@Keep
data class StripeCredentials(
    @SerializedName("publishable_key")
    val publishableKey: String? = null,
    @SerializedName("client_secret")
    val clientSecret: String? = null
)

@Keep
data class Popup(
    val download_link: String?,
    val force_to_version: Int?,
    val is_force: Int?,
    val popup_text: String?
)

data class DriverDocumentStatus(
    @SerializedName("error")
    var error: String? = null,
    @SerializedName("requiredDocsStatus")
    var requiredDocStatus: String? = null
)


data class DriverBlockedMultipleCancellation(
    @SerializedName("blocked")
    var blocked: Int? = null,
    @SerializedName("message")
    var message: String? = null
)


data class MakeDetails(
    @SerializedName("brand")
    var brand: String? = null,
    @SerializedName("color")
    var color: String? = null,
    @SerializedName("color_id")
    var colorId: String? = null,
    @SerializedName("created_at")
    var createdAt: String? = null,
    @SerializedName("door_id")
    var doorId: String? = null,
    @SerializedName("id")
    var id: String? = null,
    @SerializedName("model_id")
    var modelId: String? = null,
    @SerializedName("model_name")
    var modelName: String? = null,
    @SerializedName("no_of_doors")
    var noOfDoors: String? = null,
    @SerializedName("no_of_seat_belts")
    var noOfSeatBelts: String? = null,
    @SerializedName("seat_belt_id")
    var seatBeltId: String? = null,
    @SerializedName("updated_at")
    var updatedAt: String? = null,
    @SerializedName("vehicle_image")
    var vehicleImage: String? = null,
    @SerializedName("vehicle_type")
    var vehicleType: String? = null

)


data class RegistrationStepComplete(
    @SerializedName("is_document_uploaded")
    var isDocumentUploaded: Boolean? = null,
    @SerializedName("is_profile_completed")
    var isProfileCompleted: Boolean? = null,
    @SerializedName("is_bank_details_completed")
    var isBankDetailsCompleted: Boolean? = null,
    @SerializedName("is_vehicle_info_completed")
    var isVehicleInfoCompleted: Boolean? = null,
)


data class RegistrationSteps(
    @SerializedName("profile")
    var profile: String? = null,
    @SerializedName("document_upload")
    var documentUpload: String? = null,
    @SerializedName("vehicle_info")
    var vehicleInfo: String? = null,
    @SerializedName("bank_details")
    var bankDetails: String? = null,
)