package com.superapp_customer.viewmodel.rideVM

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.superapp_customer.VenusApp
import com.superapp_customer.model.api.ApiState
import com.superapp_customer.model.api.SingleLiveEvent
import com.superapp_customer.model.api.setApiState
import com.superapp_customer.model.dataClass.CouponResponse
import com.superapp_customer.model.dataClass.ScheduleList
import com.superapp_customer.model.dataClass.ShowMessage
import com.superapp_customer.model.dataClass.UploadPackageResponse
import com.superapp_customer.model.dataClass.addedAddresses.AddedAddressData
import com.superapp_customer.model.dataClass.base.BaseResponse
import com.superapp_customer.model.dataClass.fareEstimate.FareEstimateDC
import com.superapp_customer.model.dataClass.fetchOngoingTrip.FetchOngoingTripDC
import com.superapp_customer.model.dataClass.findDriver.FindDriverDC
import com.superapp_customer.model.dataClass.findNearDriver.FindNearDriverDC
import com.superapp_customer.model.dataClass.requestTrip.RequestTripDC
import com.superapp_customer.model.dataClass.tripsDC.RideSummaryDC
import com.superapp_customer.model.dataClass.tripsDC.TripListDC
import com.superapp_customer.model.dataClass.userData.UserDataDC
import com.superapp_customer.repo.RideRepo
import com.superapp_customer.util.SharedPreferencesManager
import com.superapp_customer.util.convertDouble
import com.superapp_customer.model.dataClass.Ticket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class RideVM @Inject constructor(
    private val rideRepo: RideRepo
) : ViewModel() {
    val regionsList by lazy { ArrayList<FindDriverDC.Region>() }
    val fareStructureList by lazy { ArrayList<FindDriverDC.FareStructure>() }
    lateinit var customerETA: FindDriverDC.CustomerETA
    var createRideData = CreateRideData()
    var vehicleType: UserDataDC.Login.VehicleType? = null
    val userData by lazy { SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA) }
    var couponToApply = 0
    var paymentOption = 1
    var promoCode = ""
    var cardId = ""
    var last4 = ""
    var schedule = false
    var isRental = false
    var selectedPickDateTimeForSchedule = ""

    /**
     * Ride Alert UI State
     * */
    private val _rideAlertUiState by lazy { SingleLiveEvent<RideAlertUiState>() }
    val rideAlertUiState: LiveData<RideAlertUiState> = _rideAlertUiState

    /**
     * Home navigation hide/unhide
     * */
    private val _hideHomeNavigation by lazy { SingleLiveEvent<Boolean>() }
    val hideHomeNavigation: LiveData<Boolean> = _hideHomeNavigation


    private val _delivery by lazy { SingleLiveEvent<Boolean>() }
    val delivery: LiveData<Boolean> = _delivery

    init {
        updateUiState(RideAlertUiState.HomeScreen)
    }


    /**
     * Find Drive
     * */
    private val _findDriverData by lazy { SingleLiveEvent<ApiState<BaseResponse<FindDriverDC>>>() }
    val findDriverData: LiveData<ApiState<BaseResponse<FindDriverDC>>> = _findDriverData
    fun findDriver(isSchedule: Boolean = false) = viewModelScope.launch {
        rideRepo.findDriver(
            latLng = LatLng(
                createRideData.pickUpLocation?.latitude.convertDouble(),
                createRideData.pickUpLocation?.longitude.convertDouble()
            ),
            opLatLng = LatLng(
                createRideData.dropLocation?.latitude.convertDouble(),
                createRideData.dropLocation?.longitude.convertDouble()
            ), isSchedule
        ).setApiState(_findDriverData)
    }

    /**
     * Find Drive in loop
     * */
    private val _findDriverDataInLoop by lazy { SingleLiveEvent<ApiState<BaseResponse<FindDriverDC>>>() }
    val findDriverDataInLoop: LiveData<ApiState<BaseResponse<FindDriverDC>>> = _findDriverDataInLoop
    fun findDriverInLoop(isSchedule: Boolean = false) = viewModelScope.launch {
        rideRepo.findDriver(
            latLng = LatLng(
                createRideData.pickUpLocation?.latitude.convertDouble(),
                createRideData.pickUpLocation?.longitude.convertDouble()
            ),
            opLatLng = LatLng(
                createRideData.dropLocation?.latitude.convertDouble(),
                createRideData.dropLocation?.longitude.convertDouble()
            ), isSchedule
        ).setApiState(_findDriverDataInLoop)
    }


    /**
     * Find Near Driver
     * */
    private val _findNearDriverData by lazy { SingleLiveEvent<ApiState<BaseResponse<FindNearDriverDC>>>() }
    val findNearDriverData: LiveData<ApiState<BaseResponse<FindNearDriverDC>>> = _findNearDriverData
    fun findNearDriver(latitude: Double, longitude: Double) = viewModelScope.launch {
        rideRepo.findNearDriver(latitude, longitude).setApiState(_findNearDriverData)
    }


    /**
     * Request Ride
     * */
    private val _requestRideData by lazy { SingleLiveEvent<ApiState<BaseResponse<RequestTripDC>>>() }
    val requestRideData: LiveData<ApiState<BaseResponse<RequestTripDC>>> = _requestRideData
    fun requestRideData(
        receiverName: String = "",
        receiverNumber: String = "",
        notes: String = ""
    ) = viewModelScope.launch {


        rideRepo.requestTrip(
            jsonObject = JSONObject().apply {
                put("currentLongitude", VenusApp.latLng.longitude)
                put("currentLatitude", VenusApp.latLng.latitude)
                put("latitude", createRideData.pickUpLocation?.latitude)
                put("longitude", createRideData.pickUpLocation?.longitude)
                put("pickupLocationAddress", createRideData.pickUpLocation?.address)
                put("dropLocationAddress", createRideData.dropLocation?.address)
                put("duplicateFlag", "0")
                put("dropLongitude", createRideData.dropLocation?.longitude)
                put("dropLatitude", createRideData.dropLocation?.latitude)
                put("regionId", createRideData.regionId)
                put("vehicleType", createRideData.vehicleType)
                put("phoneNo", userData?.login?.phoneNo.orEmpty())
                put("customerNote", notes)
                put("estimated_fare", createRideData.vehicleData?.fare ?: "")
                put("estimated_trip_distance", createRideData.vehicleData?.distance ?: "")
                put("coupon_to_apply", couponToApply)
                put("promo_to_apply", VenusApp.offerApplied.toString())
                put(
                    "request_ride_type",
                    SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID)
                )

                if (
                    SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID) == 1
                ) {
                    put("customerNote", notes)
                } else {
                    put("recipient_name", receiverName)
                    put("recipient_phone_no", receiverNumber)
//                    put("customerNote", notes)
                    // Convert list of packages to JSONArray
                    val packageArray = JSONArray()
                    SharedPreferencesManager.getAddPackageList(SharedPreferencesManager.Keys.ADD_PACKAGE)
                        ?.forEach {
                            val packageObject = JSONObject().apply {
                                put("id", it.id.toString())
                                put("package_size", it.packageSize.toString())
                                put("image", JSONArray().put(it.image))
                                put("quantity", it.quantity)
                                put("description", it.itemDescription)
                                put("type", it.packageType)
                            }
                            packageArray.put(packageObject)
                        }
                    put("package_details", packageArray)
                }

                if (paymentOption == 9) {
                    put("stripe_token", cardId)
                    put("preferred_payment_mode", "9")
                    put("card_id", cardId)
                }
            }
        ).setApiState(_requestRideData)
    }

    /**
     * Schedule Ride
     * */
    private val _scheduleRideData by lazy { SingleLiveEvent<ApiState<BaseResponse<RequestTripDC>>>() }
    val scheduleRideData: LiveData<ApiState<BaseResponse<RequestTripDC>>> = _scheduleRideData
    fun scheduleRideData(
        receiverName: String = "",
        receiverNumber: String = "", notes: String = "", pickUpTime: String = ""
    ) = viewModelScope.launch {
        rideRepo.requestSchedule(
            jsonObject = JSONObject().apply {
                put("currentLongitude", VenusApp.latLng.longitude)
                put("currentLatitude", VenusApp.latLng.latitude)
                put("latitude", createRideData.pickUpLocation?.latitude)
                put("longitude", createRideData.pickUpLocation?.longitude)
                put("pickup_location_address", createRideData.pickUpLocation?.address)
                put("drop_location_address", createRideData.dropLocation?.address)
                put("duplicateFlag", "0")
                put("op_drop_longitude", createRideData.dropLocation?.longitude)
                put("op_drop_latitude", createRideData.dropLocation?.latitude)
                put("region_id", createRideData.regionId)
                put("vehicle_type", createRideData.vehicleType)
                put("phoneNo", userData?.login?.phoneNo.orEmpty())
                put("preferred_payment_mode", "1")

                put("ride_distance", createRideData.vehicleData?.eta)
                put("fare", createRideData.vehicleData?.fare)
                put("pickup_time", pickUpTime)
                put("coupon_to_apply", couponToApply)
                put("promo_to_apply", VenusApp.offerApplied.toString())
                put(
                    "request_ride_type",
                    SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID)
                )
                if (
                    SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID) == 1
                ) {
                    put("customerNote", notes)
                } else {
                    put("recipient_name", receiverName)
                    put("recipient_phone_no", receiverNumber)
                    // Convert list of packages to JSONArray
                    val packageArray = JSONArray()
                    SharedPreferencesManager.getAddPackageList(SharedPreferencesManager.Keys.ADD_PACKAGE)
                        ?.forEach {
                            val packageObject = JSONObject().apply {
                                put("id", it.id.toString())
                                put("package_size", it.packageSize.toString())
                                put("image", JSONArray().put(it.image))
                                put("quantity", it.quantity)
                                put("description", it.itemDescription)
                                put("type", it.packageType)
                            }
                            packageArray.put(packageObject)
                        }
                    put("package_details", packageArray)
                }
                if (paymentOption == 9) {
                    put("stripe_token", cardId)
                    put("preferred_payment_mode", "9")
                    put("card_id", cardId)
                }
            }
        ).setApiState(_scheduleRideData)
    }


    /**
     * Cancel Trip
     * */
    private val _cancelTripData by lazy { SingleLiveEvent<ApiState<BaseResponse<Any>>>() }
    val cancelTripData: LiveData<ApiState<BaseResponse<Any>>> = _cancelTripData
    fun cancelTrip(sessionId: String, reason: String) {
        viewModelScope.launch {
            rideRepo.cancelTrip(sessionId = sessionId, jsonObject = JSONObject().apply {
                put("sessionId", sessionId)
                put("reasons", reason)
            }).setApiState(_cancelTripData)
        }
    }

    /**
     * Enter promo code
     * */
    private val _enterPromoCode by lazy { SingleLiveEvent<ApiState<BaseResponse<CouponResponse>>>() }
    val enterPromoCode: LiveData<ApiState<BaseResponse<CouponResponse>>> = _enterPromoCode
    fun enterPromoCode(
        promoCode: String,
        regionId: String,
        vehicleType: String,
        fare: String,
        distance: String,
        currency: String
    ) {
        viewModelScope.launch {
            rideRepo.enterPromoCode(jsonObject = JSONObject().apply {
                put("code", promoCode)
                put("region_id", regionId)
                put("vehicle_type", vehicleType)
                put("fare", fare)
                put("distance", distance)
                put("currency", currency)
            }).setApiState(_enterPromoCode)
        }
    }


    /**
     * SOS
     * */
    private val _sosData by lazy { SingleLiveEvent<ApiState<BaseResponse<Any>>>() }
    val sosData: LiveData<ApiState<BaseResponse<Any>>> = _sosData
    fun sosApi(tripId: String) {
        viewModelScope.launch {
            rideRepo.sosApi(jsonObject = JSONObject().apply {
                put("engagement_id", tripId)
            }).setApiState(_sosData)
        }
    }


    /**
     * Remove Schedule
     * */
    private val _removeScheduleData by lazy { SingleLiveEvent<ApiState<BaseResponse<ShowMessage>>>() }
    val removeScheduleData: LiveData<ApiState<BaseResponse<ShowMessage>>> = _removeScheduleData
    fun removeSchedule(pickUpId: String) {
        viewModelScope.launch {
            rideRepo.removeSchedules(jsonObject = JSONObject().apply {
                put("pickup_id", pickUpId)
            }).setApiState(_removeScheduleData)
        }
    }


    /**
     * Fare Estimate
     * */
    private val _fareEstimateData by lazy { SingleLiveEvent<ApiState<BaseResponse<FareEstimateDC>>>() }
    val fareEstimateData: LiveData<ApiState<BaseResponse<FareEstimateDC>>> = _fareEstimateData
    fun fareEstimate() = viewModelScope.launch {
        rideRepo.fareEstimate(jsonObject = JSONObject().apply {
            put("latitude", "30.680107")
            put("longitude", "76.723158")
            put("rideDistance", "10")
            put("rideTime", "10")
            put("startLongitude", "76.723158")
            put("startLatitude", "30.680107")
            put("pickupLocationAddress", "Lifestyle-CP67")
            put("dropLocationAddress", "Manav Mangal Smart School")
            put("duplicateFlag", "0")
            put("dropLatitude", "30.691539")
            put("dropLongitude", "76.745904")
            put("regionId", createRideData.regionId)
            put("vehicleType", createRideData.vehicleType)
            put("phoneNo", "9991112224")
        }).setApiState(_fareEstimateData)
    }


    /**
     * Rate Driver
     * */
    private val _rateDriverData by lazy { SingleLiveEvent<ApiState<BaseResponse<Any>>>() }
    val rateDriverData: LiveData<ApiState<BaseResponse<Any>>> = _rateDriverData
    fun rateDriver(engagementId: String, rating: String, feedback: String) = viewModelScope.launch {
        rideRepo.rateDriver(jsonObject = JSONObject().apply {
            if (feedback.isNotEmpty()) {
                put("feedback", feedback)
                put("feedbackReasons", feedback)
            }
            put("givenRating", rating)
            put("engagementId", engagementId)
        }).setApiState(_rateDriverData)
    }


    /**
     * Fetch Ongoing Trip
     * */
    private val _fetchOngoingTripData by lazy { SingleLiveEvent<ApiState<BaseResponse<FetchOngoingTripDC>>>() }
    val fetchOngoingTripData: LiveData<ApiState<BaseResponse<FetchOngoingTripDC>>> =
        _fetchOngoingTripData

    fun fetchOngoingTrip() = viewModelScope.launch {
        rideRepo.fetchOngoingTrip().setApiState(_fetchOngoingTripData)
    }

    /**
     * Get saved addresses
     * */
    private val _fetchAddedAddresses by lazy { SingleLiveEvent<ApiState<BaseResponse<AddedAddressData>>>() }
    val fetchAddedAddresses: LiveData<ApiState<BaseResponse<AddedAddressData>>> =
        _fetchAddedAddresses

    fun fetchAddedAddresses() = viewModelScope.launch {
        rideRepo.fetchAddedAddresses().setApiState(_fetchAddedAddresses)
    }

    /**
     * Update UI State
     * */
    fun updateUiState(rideAlertUiState: RideAlertUiState) {
        try {
//            if (rideAlertUiState == _rideAlertUiState.value) return
            _rideAlertUiState.value = rideAlertUiState
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hideHomeNav(isHide: Boolean) {
        try {
//            if (rideAlertUiState == _rideAlertUiState.value) return
            _hideHomeNavigation.value = isHide
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun delivery(boolean: Boolean) {
        try {
//            if (rideAlertUiState == _rideAlertUiState.value) return
            _delivery.value = boolean
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private val _tripListData by lazy { SingleLiveEvent<ApiState<BaseResponse<List<TripListDC>>>>() }
    val tripListData: LiveData<ApiState<BaseResponse<List<TripListDC>>>> = _tripListData

    fun allTripList() = viewModelScope.launch {
        rideRepo.getAllTrips().setApiState(_tripListData)
    }

    private val _scheduleListData by lazy { SingleLiveEvent<ApiState<BaseResponse<List<ScheduleList>>>>() }
    val scheduleListData: LiveData<ApiState<BaseResponse<List<ScheduleList>>>> = _scheduleListData

    fun allScheduleList() = viewModelScope.launch {
        rideRepo.getAllSchedules().setApiState(_scheduleListData)
    }


    private val _rideSummaryData by lazy { SingleLiveEvent<ApiState<BaseResponse<RideSummaryDC>>>() }
    val rideSummaryData: LiveData<ApiState<BaseResponse<RideSummaryDC>>> = _rideSummaryData

    fun rideSummary(tripId: String, driverId: String) = viewModelScope.launch {
        rideRepo.getRideSummary(tripId, driverId).setApiState(_rideSummaryData)
    }


    fun getDistanceFromGoogle(
        isLoading: () -> Unit,
        onSuccess: (distance: String, time: String) -> Unit,
        onError: (string: String) -> Unit
    ) = viewModelScope.launch {
        rideRepo.getDistanceFromGoogle(
            originId = createRideData.pickUpLocation?.placeId.orEmpty(),
            destinationId = createRideData.dropLocation?.placeId.orEmpty()
        ).onStart {
            isLoading.invoke()
        }.catch { error ->
            onError(error.localizedMessage.orEmpty())
        }.collectLatest {
            if (it.isSuccessful) {
                JSONObject(it.body().toString()).apply {
                    if (has("rows")) {
                        optJSONArray("rows")?.let { rows ->
                            if (rows.length() > 0) {
                                rows.optJSONObject(0)?.let { row ->
                                    row.optJSONArray("elements")?.let { elements ->
                                        if (elements.length() > 0) {
                                            elements.optJSONObject(0)?.let { element ->
                                                val distance = element.optJSONObject("distance")
                                                    ?.optString("text").orEmpty()
                                                val time = element.optJSONObject("duration")
                                                    ?.optString("text").orEmpty()
                                                onSuccess(distance, time)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                onError(it.errorBody()?.string().toString())
            }
        }
    }

    private val _uploadPackage by lazy { SingleLiveEvent<ApiState<BaseResponse<UploadPackageResponse>>>() }
    val uploadPackage: LiveData<ApiState<BaseResponse<UploadPackageResponse>>> get() = _uploadPackage

    fun uploadDocument(part: MultipartBody.Part) = viewModelScope.launch {
        rideRepo.uploadDocument(
            part
        ).setApiState(_uploadPackage)
    }

    private val _uploadTicketFile by lazy { SingleLiveEvent<ApiState<BaseResponse<UploadPackageResponse>>>() }
    val uploadTicketFile: LiveData<ApiState<BaseResponse<UploadPackageResponse>>> get() = _uploadTicketFile

    fun uploadTicketFile(part: MultipartBody.Part, hashMap: HashMap<String, RequestBody?>) =
        viewModelScope.launch {
            rideRepo.uploadTicketFile(
                part, hashMap
            ).setApiState(_uploadTicketFile)
        }


//    private val _initializeMobileMoney by lazy { SingleLiveEvent<ApiState<BaseResponse<MobileMoney>>>() }
//    val initializeMobileMoney: LiveData<ApiState<BaseResponse<MobileMoney>>> get() = _initializeMobileMoney
//
//    fun initializeMobileMoney(jsonObject: JSONObject) = viewModelScope.launch {
//        rideRepo.initializeMobileMoney(
//            jsonObject
//        ).setApiState(_initializeMobileMoney)
//    }
//
//    private val _mobileMoneyStatus by lazy { SingleLiveEvent<ApiState<BaseResponse<MobileMoney>>>() }
//    val mobileMoneyStatus: LiveData<ApiState<BaseResponse<MobileMoney>>> get() = _mobileMoneyStatus
//
//    fun mobileMoneyStatus(jsonObject: JSONObject) = viewModelScope.launch {
//        rideRepo.mobileMoneyStatus(
//            jsonObject
//        ).setApiState(_mobileMoneyStatus)
//    }

    private val _raiseATicket by lazy { SingleLiveEvent<ApiState<BaseResponse<Any>>>() }
    val raiseATicket: LiveData<ApiState<BaseResponse<Any>>> get() = _raiseATicket

    fun raiseATicket(jsonObject: JSONObject) = viewModelScope.launch {
        rideRepo.raiseATicket(
            jsonObject
        ).setApiState(_raiseATicket)
    }

    private val _getTicketList by lazy { SingleLiveEvent<ApiState<BaseResponse<List<Ticket>>>>() }
    val getTicketList: LiveData<ApiState<BaseResponse<List<Ticket>>>> get() = _getTicketList

    fun getTicketList() = viewModelScope.launch {
        rideRepo.getTicketsList().setApiState(_getTicketList)
    }



    /**
     * Ride Alert UI State
     * */
    sealed class RideAlertUiState {
        object HomeScreen : RideAlertUiState()
        object ShowLocationDialog : RideAlertUiState()
        object ShowVehicleTypesDialog : RideAlertUiState()
        object ShowCustomerDetailPaymentDialog : RideAlertUiState()
        object FindDriverDialog : RideAlertUiState()
        object ShowCustomerDetailDialog : RideAlertUiState()
//        object ShowDeliveryVehicleTypeDialog : RideAlertUiState()
    }

}