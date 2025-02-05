package com.mb_driver.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mb_driver.model.api.ApiState
import com.mb_driver.model.api.SingleLiveEvent
import com.mb_driver.model.api.setApiState
import com.mb_driver.model.dataclassses.base.BaseResponse
import com.mb_driver.model.dataclassses.clientConfig.ClientConfigDC
import com.mb_driver.repo.PreLoginRepo
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
        repository.getClientConfig().setApiState(_clientConfig)
    }


}