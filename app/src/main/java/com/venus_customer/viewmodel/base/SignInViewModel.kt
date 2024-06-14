package com.venus_customer.viewmodel.base

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venus_customer.VenusApp
import com.venus_customer.customClasses.LocationResultHandler
import com.venus_customer.customClasses.SingleFusedLocation
import com.venus_customer.model.api.ApiState
import com.venus_customer.model.api.setApiState
import com.venus_customer.model.dataClass.base.BaseResponse
import com.venus_customer.model.dataClass.userData.UserDataDC
import com.venus_customer.repo.PreLoginRepo
import com.venus_customer.view.activity.verifyOtp.VerifyOtp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val repository: PreLoginRepo
) : ViewModel()  {


    private val _signInData by lazy { SingleLiveEvent<ApiState<BaseResponse<Any>>>() }
    val signInData : LiveData<ApiState<BaseResponse<Any>>> get() = _signInData


    fun signIn(jsonObject: JSONObject) = SingleFusedLocation.initialize(VenusApp.appContext, object :
        LocationResultHandler {
        override fun updatedLocation(location: Location) {
            viewModelScope.launch {
                viewModelScope.launch {
                    repository.sendLoginOtp(jsonObject).setApiState(_signInData)
                }
            }
        }
    })



    private val _verifyOtp by lazy { SingleLiveEvent<ApiState<BaseResponse<UserDataDC>>>() }
    val verifyOtp: LiveData<ApiState<BaseResponse<UserDataDC>>> get() = _verifyOtp

    fun verifyOtp(jsonObject: JSONObject) {
        viewModelScope.launch {
            repository.verifyOtp(jsonObject).setApiState(_verifyOtp)
        }
    }

}