package com.marsapp_driver.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marsapp_driver.model.api.ApiState
import com.marsapp_driver.model.api.SingleLiveEvent
import com.marsapp_driver.model.api.setApiState
import com.marsapp_driver.model.dataclassses.base.BaseResponse
import com.marsapp_driver.model.dataclassses.clientConfig.ClientConfigDC
import com.marsapp_driver.repo.PreLoginRepo
import com.marsapp_driver.util.SharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashVM @Inject constructor(
    private val repository: PreLoginRepo
) : ViewModel() {


    private val _clientConfig by lazy { SingleLiveEvent<ApiState<BaseResponse<ClientConfigDC>>>() }
    val clientConfig: LiveData<ApiState<BaseResponse<ClientConfigDC>>> get() = _clientConfig


    init {
        getClientConfig()
    }

    fun getClientConfig() = viewModelScope.launch {
        repository.getClientConfig(
            SharedPreferencesManager.getString(SharedPreferencesManager.Keys.PASSCODE))
            .setApiState(_clientConfig)
    }


}