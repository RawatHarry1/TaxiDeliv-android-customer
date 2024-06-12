package com.salonedriver.model.dataclassses.earningDC


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class EarningDC(
    @SerializedName("rides")
    val rides: List<Ride> = listOf(),
    @SerializedName("totalEarnings")
    val totalEarnings: String? = null
) {
    @Keep
    data class Ride(
        @SerializedName("accept_distance")
        val acceptDistance: String? = null,
        @SerializedName("accept_distance_from_last_drop")
        val acceptDistanceFromLastDrop: String? = null,
        @SerializedName("accept_distance_subsidy")
        val acceptDistanceSubsidy: String? = null,
        @SerializedName("accept_time")
        val acceptTime: String? = null,
        @SerializedName("actual_fare")
        val actualFare: String? = null,
        @SerializedName("actual_fare_x")
        val actualFareX: String? = null,
        @SerializedName("addn_info")
        val addnInfo: String? = null,
        @SerializedName("arrived_at")
        val arrivedAt: String? = null,
        @SerializedName("bank_charges")
        val bankCharges: String? = null,
        @SerializedName("business_id")
        val businessId: String? = null,
        @SerializedName("calculated_customer_fare")
        val calculatedCustomerFare: String? = null,
        @SerializedName("calculated_driver_fare")
        val calculatedDriverFare: String? = null,
        @SerializedName("cancel_distance")
        val cancelDistance: String? = null,
        @SerializedName("cancel_distance_subsidy")
        val cancelDistanceSubsidy: String? = null,
        @SerializedName("cancel_notify_time")
        val cancelNotifyTime: String? = null,
        @SerializedName("city")
        val city: String? = null,
        @SerializedName("convenience_charge")
        val convenienceCharge: String? = null,
        @SerializedName("convenience_charge_waiver")
        val convenienceChargeWaiver: String? = null,
        @SerializedName("convenience_venus_cut")
        val convenienceVenusCut: String? = null,
        @SerializedName("current_time")
        val currentTime: String? = null,
        @SerializedName("customer_cancel_request")
        val customerCancelRequest: String? = null,
        @SerializedName("customer_cancellation_charges")
        val customerCancellationCharges: String? = null,
        @SerializedName("debt_added")
        val debtAdded: String? = null,
        @SerializedName("details_sent")
        val detailsSent: String? = null,
        @SerializedName("discount")
        val discount: String? = null,
        @SerializedName("distance_travelled")
        val distanceTravelled: String? = null,
        @SerializedName("driver_accept_latitude")
        val driverAcceptLatitude: String? = null,
        @SerializedName("driver_accept_longitude")
        val driverAcceptLongitude: String? = null,
        @SerializedName("driver_cancel_request")
        val driverCancelRequest: String? = null,
        @SerializedName("driver_feedback")
        val driverFeedback: String? = null,
        @SerializedName("driver_id")
        val driverId: String? = null,
        @SerializedName("driver_location_at_pickup")
        val driverLocationAtPickup: String? = null,
        @SerializedName("driver_rating")
        val driverRating: String? = null,
        @SerializedName("drop_latitude")
        val dropLatitude: String? = null,
        @SerializedName("drop_location_address")
        val dropLocationAddress: String? = null,
        @SerializedName("drop_location_address_ln")
        val dropLocationAddressLn: String? = null,
        @SerializedName("drop_longitude")
        val dropLongitude: String? = null,
        @SerializedName("drop_time")
        val dropTime: String? = null,
        @SerializedName("engagement_date")
        val engagementDate: String? = null,
        @SerializedName("engagement_id")
        val engagementId: String? = null,
        @SerializedName("totalEarnings")
        val totalEarnings: String? = null,
        @SerializedName("feedback_reasons")
        val feedbackReasons: String? = null,
        @SerializedName("fleet_id")
        val fleetId: String? = null,
        @SerializedName("hold_amount")
        val holdAmount: String? = null,
        @SerializedName("is_applepay_hyperpay")
        val isApplepayHyperpay: String? = null,
        @SerializedName("is_invoiced")
        val isInvoiced: String? = null,
        @SerializedName("jd_fare_type")
        val jdFareType: String? = null,
        @SerializedName("luggage_count")
        val luggageCount: String? = null,
        @SerializedName("manually_edited")
        val manuallyEdited: String? = null,
        @SerializedName("meter_fare_applicable")
        val meterFareApplicable: String? = null,
        @SerializedName("metering_distance")
        val meteringDistance: String? = null,
        @SerializedName("money_transacted")
        val moneyTransacted: String? = null,
        @SerializedName("net_customer_tax")
        val netCustomerTax: String? = null,
        @SerializedName("network_driver")
        val networkDriver: String? = null,
        @SerializedName("notification_payload")
        val notificationPayload: String? = null,
        @SerializedName("notify_customer")
        val notifyCustomer: String? = null,
        @SerializedName("nts_driver_details")
        val ntsDriverDetails: String? = null,
        @SerializedName("operator_accept_time")
        val operatorAcceptTime: String? = null,
        @SerializedName("operator_cancel_time")
        val operatorCancelTime: String? = null,
        @SerializedName("operator_create_time")
        val operatorCreateTime: String? = null,
        @SerializedName("operator_id_x")
        val operatorIdX: String? = null,
        @SerializedName("operator_match_driver")
        val operatorMatchDriver: String? = null,
        @SerializedName("operator_search_driver")
        val operatorSearchDriver: String? = null,
        @SerializedName("paid_by_customer")
        val paidByCustomer: String? = null,
        @SerializedName("paid_by_customer_x")
        val paidByCustomerX: String? = null,
        @SerializedName("paid_using_freecharge")
        val paidUsingFreecharge: String? = null,
        @SerializedName("paid_using_hyperpay")
        val paidUsingHyperpay: String? = null,
        @SerializedName("paid_using_mobikwik")
        val paidUsingMobikwik: String? = null,
        @SerializedName("paid_using_paytm")
        val paidUsingPaytm: String? = null,
        @SerializedName("paid_using_stripe")
        val paidUsingStripe: String? = null,
        @SerializedName("paid_using_wallet")
        val paidUsingWallet: String? = null,
        @SerializedName("payment_start")
        val paymentStart: String? = null,
        @SerializedName("payout_status")
        val payoutStatus: String? = null,
        @SerializedName("pickup_latitude")
        val pickupLatitude: String? = null,
        @SerializedName("pickup_location_address")
        val pickupLocationAddress: String? = null,
        @SerializedName("pickup_location_address_ln")
        val pickupLocationAddressLn: String? = null,
        @SerializedName("pickup_longitude")
        val pickupLongitude: String? = null,
        @SerializedName("pickup_time")
        val pickupTime: String? = null,
        @SerializedName("push_platform")
        val pushPlatform: String? = null,
        @SerializedName("receipt_sent")
        val receiptSent: String? = null,
        @SerializedName("request_made_on")
        val requestMadeOn: String? = null,
        @SerializedName("request_received_on")
        val requestReceivedOn: String? = null,
        @SerializedName("ride_distance_from_google")
        val rideDistanceFromGoogle: String? = null,
        @SerializedName("ride_distance_using_haversine")
        val rideDistanceUsingHaversine: String? = null,
        @SerializedName("ride_time")
        val rideTime: String? = null,
        @SerializedName("ride_type")
        val rideType: String? = null,
        @SerializedName("session_id")
        val sessionId: String? = null,
        @SerializedName("skip_rating_by_customer")
        val skipRatingByCustomer: String? = null,
        @SerializedName("status")
        val status: String? = null,
        @SerializedName("stripe_charge_id")
        val stripeChargeId: String? = null,
        @SerializedName("stripe_token")
        val stripeToken: String? = null,
        @SerializedName("sub_region_id")
        val subRegionId: String? = null,
        @SerializedName("third_party_ride_type")
        val thirdPartyRideType: String? = null,
        @SerializedName("tip_amount")
        val tipAmount: String? = null,
        @SerializedName("toll_applicable")
        val tollApplicable: String? = null,
        @SerializedName("toll_charge")
        val tollCharge: String? = null,
        @SerializedName("user_id")
        val userId: String? = null,
        @SerializedName("user_rating")
        val userRating: String? = null,
        @SerializedName("validation_end")
        val validationEnd: String? = null,
        @SerializedName("vehicle_id")
        val vehicleId: String? = null,
        @SerializedName("vehicle_type")
        val vehicleType: String? = null,
        @SerializedName("venus_commission")
        val venusCommission: String? = null,
        @SerializedName("wait_time")
        val waitTime: String? = null,
        @SerializedName("waiting_charges_applicable")
        val waitingChargesApplicable: String? = null,
        @SerializedName("customerName")
        val customerName: String? = null,
        @SerializedName("customerImage")
        val customerImage: String? = null,
        @SerializedName("waypoint_distance")
        val waypointDistance: String? = null
    )
}