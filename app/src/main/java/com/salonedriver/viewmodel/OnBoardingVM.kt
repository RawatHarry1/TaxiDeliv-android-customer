package com.salonedriver.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.salonedriver.SaloneDriver
import com.salonedriver.customClasses.LocationResultHandler
import com.salonedriver.customClasses.SingleFusedLocation
import com.salonedriver.model.api.ApiState
import com.salonedriver.model.api.SingleLiveEvent
import com.salonedriver.model.api.getPartMap
import com.salonedriver.model.api.setApiState
import com.salonedriver.model.dataclassses.AboutUsDC
import com.salonedriver.model.dataclassses.base.BaseResponse
import com.salonedriver.model.dataclassses.cityVehicle.CityVehicleDC
import com.salonedriver.model.dataclassses.cityVehicle.Color
import com.salonedriver.model.dataclassses.cityVehicle.Vehicle
import com.salonedriver.model.dataclassses.cityVehicle.VehicleType
import com.salonedriver.model.dataclassses.clientConfig.ClientConfigDC
import com.salonedriver.model.dataclassses.fetchRequiredDocument.FetchRequiredDocumentDC
import com.salonedriver.model.dataclassses.userData.UserDataDC
import com.salonedriver.repo.PreLoginRepo
import com.salonedriver.util.SharedPreferencesManager
import com.salonedriver.model.dataclassses.updateDriverInfo.UpdateDriverInfo
import com.salonedriver.model.dataclassses.userData.Login
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

@HiltViewModel
class OnBoardingVM @Inject constructor(
    private val repository: PreLoginRepo
) : ViewModel() {
    var phoneNumber: String? = null
    var countryCode: String? = null
    var profilePic: String? = ""

    val colorList by lazy { ArrayList<Color>() }
    val vehicleList by lazy { ArrayList<Vehicle>() }
    val vehicleType by lazy { ArrayList<VehicleType>() }
    var cityId: String = ""



    //Sign IN
    private val _sendLoginOtp by lazy { SingleLiveEvent<ApiState<BaseResponse<Any>>>() }
    val sendLoginOtp: LiveData<ApiState<BaseResponse<Any>>> get() = _sendLoginOtp

    fun sendLoginOtp(phoneNumber: String, countryCode: String) =  SingleFusedLocation.initialize(SaloneDriver.appContext, object : LocationResultHandler{
        override fun updatedLocation(location: Location) {
            viewModelScope.launch {
                repository.generateLoginOtp(jsonObject = JSONObject().apply {
                    put("phoneNo", phoneNumber)
                    put("countryCode", countryCode)
                    put("latitude", location.latitude)
                    put("longitude", location.longitude)
                }).setApiState(_sendLoginOtp)
            }
        }
    })


    //Verify VM
    private val _verifyData by lazy { SingleLiveEvent<ApiState<BaseResponse<UserDataDC>>>() }
    val verifyData: LiveData<ApiState<BaseResponse<UserDataDC>>> get() = _verifyData


    fun verifyOtp(otpCode: String) = SingleFusedLocation.initialize(SaloneDriver.appContext, object : LocationResultHandler{
        override fun updatedLocation(location: Location) {
            viewModelScope.launch {
                repository.verifyLoginOtp(jsonObject = JSONObject().apply {
                    put("phoneNo", phoneNumber)
                    put("countryCode", countryCode)
                    put("loginOtp", otpCode)
                    put("latitude", location.latitude)
                    put("longitude", location.longitude)
                }).setApiState(_verifyData)
            }
        }
    })


    private val _updateDriverInfo by lazy { SingleLiveEvent<ApiState<BaseResponse<UpdateDriverInfo>>>() }
    val updateDriverInfo: LiveData<ApiState<BaseResponse<UpdateDriverInfo>>> get() = _updateDriverInfo

    fun updateDriverInfo(hashMap: HashMap<String, RequestBody?>) = viewModelScope.launch {
        repository.updateDriverInfo(
            hashMap = hashMap,
            part = if (profilePic.isNullOrEmpty()) null else File(profilePic.orEmpty()).getPartMap("updatedUserImage")
        ).setApiState(_updateDriverInfo)
    }


    private val _fetchRequireDocument by lazy { SingleLiveEvent<ApiState<BaseResponse<List<FetchRequiredDocumentDC>>>>() }
    val fetchRequiredDocumentDC: LiveData<ApiState<BaseResponse<List<FetchRequiredDocumentDC>>>> get() = _fetchRequireDocument

    fun fetchRequiredDocument() = viewModelScope.launch {
        repository.fetchRequiredDocument().setApiState(_fetchRequireDocument)
    }


    fun uploadDocument(
        hashMap: HashMap<String, RequestBody?>,
        part: MultipartBody.Part,
        isLoading: () -> Unit = {},
        isSuccess: () -> Unit = {},
        isError: String.() -> Unit = {}
    ) = viewModelScope.launch {
        repository.uploadDocument(hashMap = hashMap, part = part).onStart {
            isLoading.invoke()
        }.catch { exception ->
            isError.invoke(exception.localizedMessage.orEmpty())
        }.collectLatest {
            if (it.isSuccessful)
                isSuccess.invoke()
            else
                isError.invoke(it.errorBody()?.string().orEmpty())
        }
    }


    private val _cityVehicles by lazy { SingleLiveEvent<ApiState<BaseResponse<CityVehicleDC>>>() }
    val cityVehicles: LiveData<ApiState<BaseResponse<CityVehicleDC>>> get() = _cityVehicles

    fun getCityVehicles(cityId: String) = viewModelScope.launch {
        repository.getCityVehicle(cityId).setApiState(_cityVehicles)
    }


    private val _updateVehiclesInfo by lazy { SingleLiveEvent<ApiState<BaseResponse<Any>>>() }
    val updateVehiclesInfo: LiveData<ApiState<BaseResponse<Any>>> get() = _updateVehiclesInfo

    fun updateVehiclesInfo(jsonObject: JSONObject) = viewModelScope.launch {
        repository.updateVehicleInfo(jsonObject = jsonObject).setApiState(_updateVehiclesInfo)
    }


    private val _payoutInfo by lazy { SingleLiveEvent<ApiState<BaseResponse<Any>>>() }
    val payoutInfo: LiveData<ApiState<BaseResponse<Any>>> get() = _payoutInfo

    fun payoutInfo(jsonObject: JSONObject) = viewModelScope.launch {
        repository.addBankDetail(jsonObject = jsonObject).setApiState(_payoutInfo)
    }





}