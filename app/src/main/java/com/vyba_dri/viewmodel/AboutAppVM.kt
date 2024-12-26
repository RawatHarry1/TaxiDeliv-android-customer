package com.vyba_dri.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyba_dri.model.api.ApiState
import com.vyba_dri.model.api.SingleLiveEvent
import com.vyba_dri.model.api.setApiState
import com.vyba_dri.model.dataclassses.AboutUsDC
import com.vyba_dri.model.dataclassses.base.BaseResponse
import com.vyba_dri.model.dataclassses.clientConfig.ClientConfigDC
import com.vyba_dri.model.dataclassses.userData.UserDataDC
import com.vyba_dri.repo.PreLoginRepo
import com.vyba_dri.util.SharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AboutAppVM @Inject constructor(
    private val repository: PreLoginRepo
): ViewModel() {

    private val operatorId by lazy { SharedPreferencesManager.getModel<ClientConfigDC>(SharedPreferencesManager.Keys.CLIENT_CONFIG)?.operatorId.orEmpty() }
    private val cityId by lazy { SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)?.login?.city.orEmpty() }
    var data: AboutUsDC? = null



    private val _aboutUsLinksData by lazy { SingleLiveEvent<ApiState<BaseResponse<AboutUsDC>>>() }
    val aboutUsLinkData: LiveData<ApiState<BaseResponse<AboutUsDC>>> get() = _aboutUsLinksData

    fun getAboutUsData() = viewModelScope.launch {
        repository.aboutUsData(operatorId, cityId,2).setApiState(_aboutUsLinksData)
    }

}