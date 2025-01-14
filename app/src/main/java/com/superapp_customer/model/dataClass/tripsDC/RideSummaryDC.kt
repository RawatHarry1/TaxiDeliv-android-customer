package com.superapp_customer.model.dataClass.tripsDC


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class RideSummaryDC(
    @SerializedName("autos_status_text")
    val autosStatusText: String? = null,
    @SerializedName("model_name")
    val modelName: String? = null,
    @SerializedName("accept_time")
    val acceptTime: String? = null,
    @SerializedName("addn_info")
    val addnInfo: String? = null,
    @SerializedName("base_fare")
    val baseFare: String? = null,
    @SerializedName("cancellation_charges")
    val cancellationCharges: String? = null,
    @SerializedName("city")
    val city: String? = null,
    @SerializedName("convenience_charge")
    val convenienceCharge: String? = null,
    @SerializedName("currency")
    val currency: String? = null,
    @SerializedName("currency_symbol")
    val currencySymbol: String? = null,
    @SerializedName("customer_cancellation_charges")
    val customerCancellationCharges: String? = null,
    @SerializedName("customer_fare_id")
    val customerFareId: String? = null,
    @SerializedName("customer_fare_per_baggage")
    val customerFarePerBaggage: String? = null,
    @SerializedName("customer_tax_percentage")
    val customerTaxPercentage: String? = null,
    @SerializedName("debt_added")
    val debtAdded: String? = null,
    @SerializedName("discount")
    val discount: List<Discount?>? = null,
    @SerializedName("discount_value")
    val discountValue: String? = null,
    @SerializedName("distance")
    val distance: String? = null,
    @SerializedName("distance_travelled")
    val distanceTravelled: String? = null,
    @SerializedName("distance_unit")
    val distanceUnit: String? = null,
    @SerializedName("driver_car_no")
    val driverCarNo: String? = null,
    @SerializedName("driver_fare_id")
    val driverFareId: String? = null,
    @SerializedName("driver_id")
    val driverId: String? = null,
    @SerializedName("driver_image")
    val driverImage: String? = null,
    @SerializedName("driver_name")
    val driverName: String? = null,
    @SerializedName("driver_rating")
    val driverRating: Int? = null,
    @SerializedName("driver_upi")
    val driverUpi: String? = null,
    @SerializedName("drop_address")
    val dropAddress: String? = null,
    @SerializedName("drop_latitude")
    val dropLatitude: String? = null,
    @SerializedName("drop_longitude")
    val dropLongitude: String? = null,
    @SerializedName("drop_time")
    val dropTime: String? = null,
    @SerializedName("engagement_date")
    val engagementDate: String? = null,
    @SerializedName("engagement_id")
    val engagementId: String? = null,
    @SerializedName("fare")
    val fare: String? = null,
    @SerializedName("fare_discount")
    val fareDiscount: String? = null,
    @SerializedName("fare_factor")
    val fareFactor: String? = null,
    @SerializedName("flag")
    val flag: String? = null,
    @SerializedName("icon_set")
    val iconSet: String? = null,
    @SerializedName("invoice_icon")
    val invoiceIcon: String? = null,
    @SerializedName("is_applepay_hyperpay")
    val isApplepayHyperpay: String? = null,
    @SerializedName("is_corporate_ride")
    val isCorporateRide: String? = null,
    @SerializedName("is_invoiced")
    val isInvoiced: String? = null,
    @SerializedName("is_pooled")
    val isPooled: String? = null,
    @SerializedName("last_4")
    val last4: String? = null,
    @SerializedName("luggage_charges")
    val luggageCharges: String? = null,
    @SerializedName("luggage_count")
    val luggageCount: String? = null,
    @SerializedName("manually_edited")
    val manuallyEdited: String? = null,
    @SerializedName("meter_fare_applicable")
    val meterFareApplicable: String? = null,
    @SerializedName("net_customer_tax")
    val netCustomerTax: String? = null,
    @SerializedName("nts_driver_details")
    val ntsDriverDetails: String? = null,
    @SerializedName("nts_enabled")
    val ntsEnabled: String? = null,
    @SerializedName("operator_id")
    val operatorId: String? = null,
    @SerializedName("paid_using_freecharge")
    val paidUsingFreecharge: String? = null,
    @SerializedName("paid_using_mobikwik")
    val paidUsingMobikwik: String? = null,
    @SerializedName("paid_using_paytm")
    val paidUsingPaytm: String? = null,
    @SerializedName("paid_using_razorpay")
    val paidUsingRazorpay: String? = null,
    @SerializedName("paid_using_stripe")
    val paidUsingStripe: String? = null,
    @SerializedName("paid_using_wallet")
    val paidUsingWallet: String? = null,
    @SerializedName("partner_name")
    val partnerName: String? = null,
    @SerializedName("partner_type")
    val partnerType: String? = null,
    @SerializedName("payment_mode_razorpay")
    val paymentModeRazorpay: String? = null,
    @SerializedName("pf_tip_amount")
    val pfTipAmount: String? = null,
    @SerializedName("phone_no")
    val phoneNo: String? = null,
    @SerializedName("pickup_address")
    val pickupAddress: String? = null,
    @SerializedName("pickup_latitude")
    val pickupLatitude: String? = null,
    @SerializedName("pickup_longitude")
    val pickupLongitude: String? = null,
    @SerializedName("pickup_time")
    val pickupTime: String? = null,
    @SerializedName("pool_fare_id")
    val poolFareId: String? = null,
    @SerializedName("pool_ride_time")
    val poolRideTime: String? = null,
    @SerializedName("preferred_payment_mode")
    val preferredPaymentMode: String? = null,
    @SerializedName("rate_app")
    val rateApp: String? = null,
    @SerializedName("rate_app_dialog_content")
    val rateAppDialogContent: RateAppDialogContent? = null,
    @SerializedName("ride_date")
    val rideDate: String? = null,
    @SerializedName("ride_end_good_feedback_view_type")
    val rideEndGoodFeedbackViewType: String? = null,
    @SerializedName("ride_end_time")
    val rideEndTime: String? = null,
    @SerializedName("ride_end_time_utc")
    val rideEndTimeUtc: String? = null,
    @SerializedName("ride_time")
    val rideTime: String? = null,
    @SerializedName("ride_type")
    val rideType: String? = null,
    @SerializedName("scheduled_ride_pickup_id")
    val scheduledRidePickupId: String? = null,
    @SerializedName("show_tip_option")
    val showTipOption: String? = null,
    @SerializedName("skip_rating_by_customer")
    val skipRatingByCustomer: String? = null,
    @SerializedName("status")
    val status: Int? = null,
    @SerializedName("service_type")
    val serviceType: Int? = null,
    @SerializedName("sub_region_id")
    val subRegionId: String? = null,
    @SerializedName("tip_amount")
    val tipAmount: String? = null,
    @SerializedName("to_pay")
    val toPay: String? = null,
    @SerializedName("toll_charge")
    val tollCharge: String? = null,
    @SerializedName("total_luggage_charges")
    val totalLuggageCharges: String? = null,
    @SerializedName("total_rides_as_user")
    val totalRidesAsUser: String? = null,
    @SerializedName("trip_total")
    val tripTotal: String? = null,
    @SerializedName("user_id")
    val userId: String? = null,
    @SerializedName("utc_offset")
    val utcOffset: String? = null,
    @SerializedName("vehicle_type")
    val vehicleType: String? = null,
    @SerializedName("venus_balance")
    val venusBalance: String? = null,
    @SerializedName("wait_time")
    val waitTime: String? = null,
    @SerializedName("waiting_charges_applicable")
    val waitingChargesApplicable: String? = null,
    @SerializedName("tracking_image")
    val trackingImage: String? = null,
    @SerializedName("support_number")
    val supportNumber: String? = null,
    @SerializedName("is_for_rental")
    val isForRental: String? = null,
    @SerializedName("end_time")
    val endTime: String? = null,
    @SerializedName("start_time")
    val startTime: String? = null,
    @SerializedName("delivery_packages")
    val deliveryPackages: List<OngoingPackages>? = null
) {
    @Keep
    data class Discount(
        @SerializedName("key")
        val key: String? = null,
        @SerializedName("value")
        val value: String? = null
    )

    @Keep
    data class RateAppDialogContent(
        @SerializedName("cancel_button_text")
        val cancelButtonText: String? = null,
        @SerializedName("confirm_button_text")
        val confirmButtonText: String? = null,
        @SerializedName("never_button_text")
        val neverButtonText: String? = null,
        @SerializedName("text")
        val text: String? = null,
        @SerializedName("title")
        val title: String? = null,
        @SerializedName("url")
        val url: String? = null
    )

    @Keep
    data class OngoingPackages(
        val notes: Any,
        val package_id: Int,
        val package_image_while_drop_off: List<String>,
        val package_image_while_pickup: List<String>,
        val package_images_by_customer: List<String>,
        val package_size: String,
        val package_type: String,
        val package_quantity: Int,
        val delivery_status: Int
    )
}