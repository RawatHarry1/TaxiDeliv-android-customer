package com.salonedriver.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salonedriver.model.api.ApiState
import com.salonedriver.model.api.SingleLiveEvent
import com.salonedriver.model.api.setApiState
import com.salonedriver.model.dataclassses.base.BaseResponse
import com.salonedriver.model.dataclassses.clientConfig.ClientConfigDC
import com.salonedriver.repo.PreLoginRepo
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