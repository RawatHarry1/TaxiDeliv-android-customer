package com.salonedriver.viewmodel

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.salonedriver.R
import com.salonedriver.model.api.ApiState
import com.salonedriver.model.api.SingleLiveEvent
import com.salonedriver.model.api.setApiState
import com.salonedriver.model.dataclassses.AboutUsDC
import com.salonedriver.model.dataclassses.base.BaseResponse
import com.salonedriver.model.dataclassses.clientConfig.ClientConfigDC
import com.salonedriver.model.dataclassses.userData.UserDataDC
import com.salonedriver.repo.PreLoginRepo
import com.salonedriver.util.SharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Objects
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