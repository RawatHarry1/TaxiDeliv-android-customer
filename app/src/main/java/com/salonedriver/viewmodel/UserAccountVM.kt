package com.salonedriver.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salonedriver.model.api.ApiState
import com.salonedriver.model.api.SingleLiveEvent
import com.salonedriver.model.api.setApiState
import com.salonedriver.model.dataclassses.VehicleListDC
import com.salonedriver.model.dataclassses.base.BaseResponse
import com.salonedriver.model.dataclassses.userData.Login
import com.salonedriver.model.dataclassses.userData.UserDataDC
import com.salonedriver.repo.UserRepo
import com.salonedriver.util.SharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserAccountVM @Inject constructor(
    private val repository: UserRepo
) : ViewModel() {


    private val _getProfile by lazy { SingleLiveEvent<ApiState<BaseResponse<Login>>>() }
    val getProfile: LiveData<ApiState<BaseResponse<Login>>> get() = _getProfile

    fun getProfile() = viewModelScope.launch {
        repository.getProfile().setApiState(_getProfile)
    }


    private val _logout by lazy { SingleLiveEvent<ApiState<BaseResponse<Any>>>() }
    val logout: LiveData<ApiState<BaseResponse<Any>>> get() = _logout

    fun logout() = viewModelScope.launch {
        repository.logout().setApiState(_logout)
    }

    private val _deleteAccount by lazy { SingleLiveEvent<ApiState<BaseResponse<Any>>>() }
    val deleteAccount: LiveData<ApiState<BaseResponse<Any>>> get() = _deleteAccount

    fun deleteAccount() = viewModelScope.launch {
        repository.deleteAccount().setApiState(_deleteAccount)
    }


    private val _vehicleListData by lazy { SingleLiveEvent<ApiState<BaseResponse<VehicleListDC>>>() }
    val vehicleListData: LiveData<ApiState<BaseResponse<VehicleListDC>>> get() = _vehicleListData

    fun vehicleListData() = viewModelScope.launch {
        repository.getVehicleList(
            SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)?.login?.city.orEmpty()
        ).setApiState(_vehicleListData)
    }

    private val _updateDriverLocation by lazy { SingleLiveEvent<ApiState<BaseResponse<Any>>>() }
    val updateDriverLocation: LiveData<ApiState<BaseResponse<Any>>> get() = _updateDriverLocation

    fun updateDriverLocation() = viewModelScope.launch {
        repository.updateDriverLocation().setApiState(_updateDriverLocation)
    }
}