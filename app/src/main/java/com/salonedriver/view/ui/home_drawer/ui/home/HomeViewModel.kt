package com.salonedriver.view.ui.home_drawer.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salonedriver.model.api.ApiState
import com.salonedriver.model.api.SingleLiveEvent
import com.salonedriver.model.api.setApiState
import com.salonedriver.model.dataclassses.base.BaseResponse
import com.salonedriver.model.dataclassses.changeStatus.ChangeStatusDC
import com.salonedriver.model.dataclassses.userData.UserDataDC
import com.salonedriver.repo.PreLoginRepo
import com.salonedriver.repo.UserRepo
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