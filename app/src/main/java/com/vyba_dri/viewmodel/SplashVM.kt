package com.vyba_dri.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyba_dri.model.api.ApiState
import com.vyba_dri.model.api.SingleLiveEvent
import com.vyba_dri.model.api.setApiState
import com.vyba_dri.model.dataclassses.base.BaseResponse
import com.vyba_dri.model.dataclassses.clientConfig.ClientConfigDC
import com.vyba_dri.repo.PreLoginRepo
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