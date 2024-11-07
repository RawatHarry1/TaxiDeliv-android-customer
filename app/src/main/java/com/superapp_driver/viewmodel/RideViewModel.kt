package com.superapp_driver.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superapp_driver.firebaseSetup.NewRideNotificationDC
import com.superapp_driver.model.api.ApiState
import com.superapp_driver.model.api.SingleLiveEvent
import com.superapp_driver.model.api.setApiState
import com.superapp_driver.model.dataclassses.PackageStatus
import com.superapp_driver.model.dataclassses.UploadPackageResponse
import com.superapp_driver.model.dataclassses.base.BaseResponse
import com.superapp_driver.model.dataclassses.rideModels.AcceptRideDC
import com.superapp_driver.model.dataclassses.rideModels.OngoingRideDC
import com.superapp_driver.repo.RideRepo
import com.superapp_driver.util.SharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class RideViewModel @Inject constructor(
    private val rideRepo: RideRepo
) : ViewModel() {

    var newRideNotificationData = NewRideNotificationDC()
    private val _rejectRideData by lazy { SingleLiveEvent<ApiState<BaseResponse<Any>>>() }
    val rejectRideData: LiveData<ApiState<BaseResponse<Any>>> get() = _rejectRideData
    fun rejectRide(tripId: String) = viewModelScope.launch {
        rideRepo.rejectRide(tripId = tripId).setApiState(_rejectRideData)
    }


    private val _acceptRideData by lazy { SingleLiveEvent<ApiState<BaseResponse<AcceptRideDC>>>() }
    val acceptRideData: LiveData<ApiState<BaseResponse<AcceptRideDC>>> get() = _acceptRideData
    fun acceptRide(customerId: String, tripId: String) = viewModelScope.launch {
        rideRepo.acceptRide(customerId, tripId).setApiState(_acceptRideData)
    }


    private val _markArrived by lazy { SingleLiveEvent<ApiState<BaseResponse<Any>>>() }
    val markArrived: LiveData<ApiState<BaseResponse<Any>>> get() = _markArrived
    fun markArrived(customerId: String, tripId: String) = viewModelScope.launch {
        rideRepo.markArrived(customerId, tripId).setApiState(_markArrived)
    }


    private val _startTrip by lazy { SingleLiveEvent<ApiState<BaseResponse<Any>>>() }
    val startTrip: LiveData<ApiState<BaseResponse<Any>>> get() = _startTrip
    fun startTrip(customerId: String, tripId: String) = viewModelScope.launch {
        rideRepo.startTrip(customerId, tripId).setApiState(_startTrip)
    }


    private val _endTrip by lazy { SingleLiveEvent<ApiState<BaseResponse<NewRideNotificationDC>>>() }
    val endTrip: LiveData<ApiState<BaseResponse<NewRideNotificationDC>>> = _endTrip
    fun endTrip(
        customerId: String,
        tripId: String,
        dropLatitude: String,
        dropLongitude: String,
        distanceTravelled: String,
        rideTime: String,
        waitTime: String
    ) = viewModelScope.launch {
        rideRepo.endTrip(
            customerId,
            tripId,
            dropLatitude,
            dropLongitude,
            distanceTravelled,
            rideTime,
            waitTime
        ).setApiState(_endTrip)
    }


    private val _updatePackage by lazy { SingleLiveEvent<ApiState<BaseResponse<PackageStatus>>>() }
    val updatePackage: LiveData<ApiState<BaseResponse<PackageStatus>>> = _updatePackage
    fun updatePackage(
        sessionId: String,
        driverId: String,
        packageId: String,
        cancellationReason: String? = null,
        packageImages: List<String>? = null,
        isEnd: Boolean
    ) = viewModelScope.launch {
        rideRepo.updatePackageStatus(
            sessionId, driverId, packageId, cancellationReason, packageImages, isEnd
        ).setApiState(_updatePackage)
    }


    private val _ongoingTrip by lazy { SingleLiveEvent<ApiState<BaseResponse<OngoingRideDC>>>() }
    val ongoingTrip: LiveData<ApiState<BaseResponse<OngoingRideDC>>> = _ongoingTrip
    fun ongoingTrip() = viewModelScope.launch {
        SharedPreferencesManager.clearKeyData(SharedPreferencesManager.Keys.NEW_BOOKING)
        rideRepo.ongoingTrip().setApiState(_ongoingTrip)
    }


    private val _cancelTrip by lazy { SingleLiveEvent<ApiState<BaseResponse<Any>>>() }
    val cancelTrip: LiveData<ApiState<BaseResponse<Any>>> = _cancelTrip
    fun cancelTrip(jsonObject: JSONObject) = viewModelScope.launch {
        rideRepo.cancelTrip(jsonObject).setApiState(_cancelTrip)
    }


    private val _rateCustomer by lazy { SingleLiveEvent<ApiState<BaseResponse<Any>>>() }
    val rateCustomer: LiveData<ApiState<BaseResponse<Any>>> = _rateCustomer
    fun rateCustomer(jsonObject: JSONObject) = viewModelScope.launch {
        rideRepo.rateCustomer(jsonObject).setApiState(_rateCustomer)
    }

    private val _generateTicket by lazy { SingleLiveEvent<ApiState<BaseResponse<Any>>>() }
    val generateTicket: LiveData<ApiState<BaseResponse<Any>>> = _generateTicket
    fun generateSupportTicket(jsonObject: JSONObject) = viewModelScope.launch {
        rideRepo.generateSupportTicket(jsonObject).setApiState(_generateTicket)
    }

    private val _uploadPackage by lazy { SingleLiveEvent<ApiState<BaseResponse<UploadPackageResponse>>>() }
    val uploadPackage: LiveData<ApiState<BaseResponse<UploadPackageResponse>>> get() = _uploadPackage

    fun uploadPackageImage(part: MultipartBody.Part, hashMap: HashMap<String, RequestBody?>) =
        viewModelScope.launch {
            rideRepo.uploadPackageImage(
                part, hashMap
            ).setApiState(_uploadPackage)
        }

}