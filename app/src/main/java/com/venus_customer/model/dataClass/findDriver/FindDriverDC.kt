package com.venus_customer.model.dataClass.findDriver


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.venus_customer.util.convertDouble
import com.venus_customer.util.splitSpace
import kotlin.Exception

@Keep
data class FindDriverDC(
    @SerializedName("autos_enabled")
    val autosEnabled: String? = null,
    @SerializedName("city_id")
    val cityId: String? = null,
    @SerializedName("currency")
    val currency: String? = null,
    @SerializedName("distance_unit")
    val distanceUnit: String? = null,
    @SerializedName("driver_fare_factor")
    val driverFareFactor: String? = null,
    @SerializedName("drivers")
    val drivers: List<Driver?>? = null,
    @SerializedName("fare_factor")
    val fareFactor: String? = null,
    @SerializedName("fare_structure")
    val fareStructure: List<FareStructure>? = null,
    @SerializedName("operational_hours_data")
    val operationalHoursData: OperationalHoursData? = null,
    @SerializedName("pay_enabled")
    val payEnabled: String? = null,
    @SerializedName("regions")
    val regions: List<Region>? = null,
    @SerializedName("request_levels")
    val requestLevels: List<RequestLevel?>? = null,
    @SerializedName("show_region_specific_fare")
    val showRegionSpecificFare: String? = null,
    @SerializedName("total_rides_as_user")
    val totalRidesAsUser: String? = null
) {
    @Keep
    data class Driver(
        @SerializedName("app_versioncode")
        val appVersioncode: String? = null,
        @SerializedName("audit_status")
        val auditStatus: String? = null,
        @SerializedName("battery_percentage")
        val batteryPercentage: String? = null,
        @SerializedName("bearing")
        val bearing: String? = null,
        @SerializedName("captive_driver_enabled")
        val captiveDriverEnabled: String? = null,
        @SerializedName("car_no")
        val carNo: String? = null,
        @SerializedName("city_id")
        val cityId: String? = null,
        @SerializedName("customers_rated")
        val customersRated: String? = null,
        @SerializedName("device_token")
        val deviceToken: String? = null,
        @SerializedName("device_type")
        val deviceType: String? = null,
        @SerializedName("distance")
        val distance: String? = null,
        @SerializedName("driver_id")
        val driverId: String? = null,
        @SerializedName("driver_image")
        val driverImage: String? = null,
        @SerializedName("experience")
        val experience: String? = null,
        @SerializedName("external_id")
        val externalId: String? = null,
        @SerializedName("fleet_id")
        val fleetId: String? = null,
        @SerializedName("is_charging")
        val isCharging: String? = null,
        @SerializedName("is_tracker")
        val isTracker: String? = null,
        @SerializedName("latitude")
        val latitude: String? = null,
        @SerializedName("longitude")
        val longitude: String? = null,
        @SerializedName("operator_id")
        val operatorId: String? = null,
        @SerializedName("phone_no")
        val phoneNo: String? = null,
        @SerializedName("pooled_customers")
        val pooledCustomers: String? = null,
        @SerializedName("rating")
        val rating: String? = null,
        @SerializedName("status")
        val status: String? = null,
        @SerializedName("user_id")
        val userId: String? = null,
        @SerializedName("user_name")
        val userName: String? = null,
        @SerializedName("vehicle_brand")
        val vehicleBrand: String? = null,
        @SerializedName("vehicle_make_id")
        val vehicleMakeId: String? = null,
        @SerializedName("vehicle_name")
        val vehicleName: String? = null,
        @SerializedName("vehicle_no")
        val vehicleNo: String? = null,
        @SerializedName("vehicle_type")
        val vehicleType: String? = null,
        @SerializedName("vehicle_year")
        val vehicleYear: String? = null
    )

    @Keep
    data class FareStructure(
        @SerializedName("display_base_fare")
        val displayBaseFare: String? = null,
        @SerializedName("display_fare_text")
        val displayFareText: String? = null,
        @SerializedName("end_time")
        val endTime: String? = null,
        @SerializedName("fare_fixed")
        val fareFixed: String? = null,
        @SerializedName("fare_minimum")
        val fareMinimum: String? = null,
        @SerializedName("fare_per_baggage")
        val farePerBaggage: String? = null,
        @SerializedName("fare_per_km")
        val farePerKm: String? = null,
        @SerializedName("fare_per_km_after_threshold")
        val farePerKmAfterThreshold: String? = null,
        @SerializedName("fare_per_km_before_threshold")
        val farePerKmBeforeThreshold: String? = null,
        @SerializedName("fare_per_km_threshold_distance")
        val farePerKmThresholdDistance: String? = null,
        @SerializedName("fare_per_min")
        val farePerMin: String? = null,
        @SerializedName("fare_per_waiting_min")
        val farePerWaitingMin: String? = null,
        @SerializedName("fare_per_xmin")
        val farePerXmin: String? = null,
        @SerializedName("fare_threshold_distance")
        val fareThresholdDistance: String? = null,
        @SerializedName("fare_threshold_time")
        val fareThresholdTime: String? = null,
        @SerializedName("fare_threshold_waiting_time")
        val fareThresholdWaitingTime: String? = null,
        @SerializedName("no_of_xmin")
        val noOfXmin: String? = null,
        @SerializedName("operator_id")
        val operatorId: String? = null,
        @SerializedName("region_id")
        val regionId: String? = null,
        @SerializedName("ride_type")
        val rideType: String? = null,
        @SerializedName("scheduled_ride_fare")
        val scheduledRideFare: String? = null,
        @SerializedName("start_time")
        val startTime: String? = null,
        @SerializedName("vehicle_type")
        val vehicleType: String? = null
    )

    @Keep
    data class OperationalHoursData(
        @SerializedName("day_id")
        val dayId: String? = null,
        @SerializedName("end_operation_time")
        val endOperationTime: String? = null,
        @SerializedName("is_operation_available")
        val isOperationAvailable: String? = null,
        @SerializedName("start_operation_time")
        val startOperationTime: String? = null
    )

    @Keep
    data class Region(
        @SerializedName("applicable_gender")
        val applicableGender: String? = null,
        @SerializedName("customer_fare_factor")
        val customerFareFactor: String? = null,
        @SerializedName("customer_notes_enabled")
        val customerNotesEnabled: String? = null,
        @SerializedName("deepindex")
        val deepindex: String? = null,
        @SerializedName("destination_mandatory")
        val destinationMandatory: String? = null,
        @SerializedName("disclaimer_text")
        val disclaimerText: String? = null,
        @SerializedName("driver_fare_factor")
        val driverFareFactor: String? = null,
        @SerializedName("fare_mandatory")
        val fareMandatory: String? = null,
        @SerializedName("icon_set")
        val iconSet: String? = null,
        @SerializedName("images")
        val images: Images? = null,
        @SerializedName("max_people")
        val maxPeople: String? = null,
        @SerializedName("multiple_destinations_enabled")
        val multipleDestinationsEnabled: String? = null,
        @SerializedName("offer_texts")
        val offerTexts: OfferTexts? = null,
        @SerializedName("operator_id")
        val operatorId: String? = null,
        @SerializedName("priority_tip_category")
        val priorityTipCategory: String? = null,
        @SerializedName("region_id")
        val regionId: String? = null,
        @SerializedName("region_name")
        val regionName: String? = null,
        @SerializedName("restricted_payment_modes")
        val restrictedPaymentModes: List<String?>? = null,
        @SerializedName("reverse_bidding_enabled")
        val reverseBiddingEnabled: String? = null,
        @SerializedName("ride_type")
        val rideType: String? = null,
        @SerializedName("show_fare_estimate")
        val showFareEstimate: String? = null,
        @SerializedName("vehicle_properties")
        val vehicleProperties: String? = null,
        @SerializedName("vehicle_type")
        val vehicleType: String? = null,
        @SerializedName("eta")
        val eta: String? = null,
        @SerializedName("distance")
        val distance: String? = null,
        @SerializedName("vehicleAmount")
        var vehicleAmount: String? = null,
        @SerializedName("isSelected")
        var isSelected: Boolean = false,
        @SerializedName("rideTotalDistance")
        var rideTotalDistance: String? = null,
        @SerializedName("rideTotalTime")
        var rideTotalTime: String? = null
    ) {
        @Keep
        data class Images(
            @SerializedName("driver_icon")
            val driverIcon: String? = null,
            @SerializedName("history_icon")
            val historyIcon: String? = null,
            @SerializedName("invoice_icon")
            val invoiceIcon: String? = null,
            @SerializedName("marker_icon")
            val markerIcon: String? = null,
            @SerializedName("ride_now_highlighted")
            val rideNowHighlighted: String? = null,
            @SerializedName("ride_now_normal")
            val rideNowNormal: String? = null,
            @SerializedName("tab_highlighted")
            val tabHighlighted: String? = null,
            @SerializedName("tab_normal")
            val tabNormal: String? = null,
            @SerializedName("ride_now_normal_2x")
            val rideNowNormal2x: String? = null
        )

        @Keep
        data class OfferTexts(
            @SerializedName("text1")
            val text1: String? = null,
            @SerializedName("text2")
            val text2: String? = null
        )


        fun calculateFearStructure(list: List<FareStructure>): String{
            return try {
                val fare = list.find { it.vehicleType == vehicleType.orEmpty() } ?: kotlin.run { throw Exception() }
                if (fare.farePerKmThresholdDistance.convertDouble() > 0.0){
                    return ((fare.fareFixed.convertDouble() +
                            if ((rideTotalDistance.splitSpace().convertDouble() - fare.fareThresholdDistance.convertDouble()) < 0)
                                0.0
                            else
                                if ((rideTotalDistance.splitSpace().convertDouble() - fare.farePerKmThresholdDistance.convertDouble()) < 0)
                                    (rideTotalDistance.splitSpace().convertDouble() - fare.fareThresholdDistance.convertDouble()) * fare.farePerKm.convertDouble()
                                else
                                    (fare.farePerKmThresholdDistance.convertDouble() - fare.fareThresholdDistance.convertDouble()) * fare.farePerKm.convertDouble() +
                                            (rideTotalDistance.splitSpace().convertDouble() - fare.farePerKmThresholdDistance.convertDouble()) * fare.farePerKm.convertDouble()) +
                            if (rideTotalTime.splitSpace().convertDouble() - fare.fareThresholdTime.convertDouble() < 0)
                                0.0
                            else
                                (rideTotalTime.splitSpace().convertDouble() - fare.fareThresholdTime.convertDouble()) * fare.farePerMin.convertDouble() +
                                        fare.farePerXmin.convertDouble()).toString()
                } else {
                    return (fare.fareFixed.convertDouble() +
                            (if ((rideTotalDistance.splitSpace().convertDouble() - fare.farePerKmThresholdDistance.convertDouble()) < 0){
                                0.0
                            } else {
                                rideTotalDistance.splitSpace().convertDouble() - fare.farePerKmThresholdDistance.convertDouble()
                            }) * fare.farePerKm.convertDouble() +
                            (if ((rideTotalTime.splitSpace().convertDouble() - fare.fareThresholdTime.convertDouble()) < 0){
                                0.0
                            } else {
                                rideTotalTime.splitSpace().convertDouble() - fare.fareThresholdTime.convertDouble()
                            }) * fare.farePerMin.convertDouble() + fare.farePerXmin.convertDouble()).toString()
                }
            }catch (e:Exception){
                "0.0"
            }
        }

    }

    @Keep
    data class RequestLevel(
        @SerializedName("enabled")
        val enabled: String? = null,
        @SerializedName("level")
        val level: String? = null,
        @SerializedName("tip_enabled")
        val tipEnabled: String? = null
    )
}