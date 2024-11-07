package com.superapp_driver.view.ui.home_drawer.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superapp_driver.model.api.ApiState
import com.superapp_driver.model.api.SingleLiveEvent
import com.superapp_driver.model.api.setApiState
import com.superapp_driver.model.dataclassses.base.BaseResponse
import com.superapp_driver.model.dataclassses.changeStatus.ChangeStatusDC
import com.superapp_driver.model.dataclassses.userData.UserDataDC
import com.superapp_driver.repo.PreLoginRepo
import com.superapp_driver.repo.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PreLoginRepo,
    private val userRepo: UserRepo
) : ViewModel() {

    private val _loginViaAccessToken by lazy { SingleLiveEvent<ApiState<BaseResponse<UserDataDC>>>() }
    val loginViaAccessToken: LiveData<ApiState<BaseResponse<UserDataDC>>> get() = _loginViaAccessToken

    fun loginViaAccessToken(latitude: Double, longitude: Double) = viewModelScope.launch {
        repository.loginViaAccessToken(latitude, longitude).setApiState(_loginViaAccessToken)
    }


    private val _changeStatusData by lazy { SingleLiveEvent<ApiState<BaseResponse<ChangeStatusDC>>>() }
    val changeStatusData: LiveData<ApiState<BaseResponse<ChangeStatusDC>>> get() = _changeStatusData

    fun changeStatus(boolean: Boolean) = viewModelScope.launch {
        userRepo.changeAvailability(boolean).setApiState(_changeStatusData)
    }

}