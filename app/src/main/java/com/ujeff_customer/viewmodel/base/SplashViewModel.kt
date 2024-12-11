package com.ujeff_customer.viewmodel.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ujeff_customer.model.api.ApiState
import com.ujeff_customer.model.api.setApiState
import com.ujeff_customer.model.dataClass.base.BaseResponse
import com.ujeff_customer.model.dataClass.base.ClientConfig
import com.ujeff_customer.repo.PreLoginRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repository: PreLoginRepo
) : ViewModel() {

    private val _fetchTokenLiveData = SingleLiveEvent<ApiState<BaseResponse<ClientConfig>>>()
    val fetchTokenResponseLiveData: LiveData<ApiState<BaseResponse<ClientConfig>>>
        get() = _fetchTokenLiveData

//
//  private val _whiteLabelLiveData = SingleLiveEvent<ApiState<WhiteLabelResponse>>()
//    val whiteLabelResponseLiveData: LiveData<ApiState<WhiteLabelResponse>>
//        get() = _whiteLabelLiveData

    fun fetchOperatorToken() = viewModelScope.launch {
        repository.fetchOperatorToken().setApiState(_fetchTokenLiveData)
    }
//
//
//    fun getWhiteLabelDetails(map : HashMap<String,Any>) = viewModelScope.launch {
//        repository.getWhiteLabelDetails(map).onStart {
//            _whiteLabelLiveData.value = ApiState.loading()
//        }.catch {
//            _whiteLabelLiveData.value = ApiState.error(AppUtils.errorFormatter(it))
//        }.collect {
//            _whiteLabelLiveData.value = ApiState.success(it.data)
//        }
//    }


}