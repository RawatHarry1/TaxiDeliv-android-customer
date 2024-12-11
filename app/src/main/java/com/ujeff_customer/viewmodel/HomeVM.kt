package com.ujeff_customer.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.ujeff_customer.VenusApp
import com.ujeff_customer.customClasses.LocationResultHandler
import com.ujeff_customer.customClasses.SingleFusedLocation
import com.ujeff_customer.model.api.ApiState
import com.ujeff_customer.model.api.setApiState
import com.ujeff_customer.model.dataClass.base.BaseResponse
import com.ujeff_customer.model.dataClass.userData.UserDataDC
import com.ujeff_customer.repo.PreLoginRepo
import com.ujeff_customer.viewmodel.base.BaseViewModel
import com.ujeff_customer.viewmodel.base.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class HomeVM @Inject constructor(
    private val preLoginRepo: PreLoginRepo
) : BaseViewModel() {

    private val _loginViaToken by lazy { SingleLiveEvent<ApiState<BaseResponse<UserDataDC>>>() }
    val loginViaToken: LiveData<ApiState<BaseResponse<UserDataDC>>> get() = _loginViaToken

    fun loginViaToken() = SingleFusedLocation.initialize(VenusApp.appContext, object :
        LocationResultHandler {
        override fun updatedLocation(location: Location) {
            Log.i("loginViaToken", "loginViaToken")
            VenusApp.latLng = LatLng(location.latitude, location.longitude)
            viewModelScope.launch {
                preLoginRepo.loginViaAccessToken(jsonObject = JSONObject().apply {
                    put("latitude", location.latitude)
                    put("longitude", location.longitude)
                }).setApiState(_loginViaToken)
            }
        }
    })


    fun loginViaToken2() = viewModelScope.launch {
        preLoginRepo.loginViaAccessToken(jsonObject = JSONObject().apply {
            put("latitude", VenusApp.latLng.latitude)
            put("longitude", VenusApp.latLng.longitude)
        }).setApiState(_loginViaToken)
    }


}