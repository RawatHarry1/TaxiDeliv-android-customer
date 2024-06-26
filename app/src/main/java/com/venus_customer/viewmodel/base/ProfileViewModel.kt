package com.venus_customer.viewmodel.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venus_customer.VenusApp
import com.venus_customer.model.api.ApiState
import com.venus_customer.model.api.getPartMap
import com.venus_customer.model.api.setApiState
import com.venus_customer.model.dataClass.AboutAppDC
import com.venus_customer.model.dataClass.CouponAndPromos
import com.venus_customer.model.dataClass.WalletTransaction
import com.venus_customer.model.dataClass.base.BaseResponse
import com.venus_customer.model.dataClass.base.ClientConfig
import com.venus_customer.model.dataClass.userData.UserDataDC
import com.venus_customer.repo.PreLoginRepo
import com.venus_customer.util.SharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: PreLoginRepo
) : ViewModel() {

    private val operatorId by lazy {
        SharedPreferencesManager.getModel<ClientConfig>(
            SharedPreferencesManager.Keys.CLIENT_CONFIG
        )?.operatorId
    }
    private val cityId by lazy {
        SharedPreferencesManager.getModel<UserDataDC>(
            SharedPreferencesManager.Keys.USER_DATA
        )?.login?.city.orEmpty()
    }
    var data: AboutAppDC? = null
    var needToUploadImage: Boolean = true
    var imagePath: String? = null


    private val _updateProfile by lazy { SingleLiveEvent<ApiState<BaseResponse<Any>>>() }
    val updateProfile: LiveData<ApiState<BaseResponse<Any>>> get() = _updateProfile

    fun updateProfile(hashMap: HashMap<String, RequestBody?>) = viewModelScope.launch {
        repository.updateProfile(
            hashMap = hashMap,
            part = if (!needToUploadImage) null else File(imagePath.orEmpty()).getPartMap("updatedUserImage")
        ).setApiState(_updateProfile)
    }


    private val _logout by lazy { SingleLiveEvent<ApiState<BaseResponse<Any>>>() }
    val logout: LiveData<ApiState<BaseResponse<Any>>> get() = _logout

    fun logout() = viewModelScope.launch {
        repository.logout().setApiState(_logout)
    }


    private val _aboutApp by lazy { SingleLiveEvent<ApiState<BaseResponse<AboutAppDC>>>() }
    val aboutApp: LiveData<ApiState<BaseResponse<AboutAppDC>>> get() = _aboutApp

    fun aboutApp() = viewModelScope.launch {
        repository.aboutApp(operatorId = operatorId.toString(), cityId = cityId)
            .setApiState(_aboutApp)
    }

    private val _transactionHistoryData by lazy { SingleLiveEvent<ApiState<BaseResponse<WalletTransaction>>>() }
    val transactionHistoryData: LiveData<ApiState<BaseResponse<WalletTransaction>>> get() = _transactionHistoryData
    fun getTransactions() = viewModelScope.launch {
        repository.getTransactions(jsonObject = JSONObject().apply { put("start_from", 0) })
            .setApiState(_transactionHistoryData)
    }

    private val _promoData by lazy { SingleLiveEvent<ApiState<BaseResponse<CouponAndPromos>>>() }
    val promoData: LiveData<ApiState<BaseResponse<CouponAndPromos>>> get() = _promoData
    fun getCouponAndPromotions() = viewModelScope.launch {
        repository.getCouponAndPromo(jsonObject = JSONObject().apply {
            put("latitude", VenusApp.latLng.latitude ?: 0.0)
            put("longitude", VenusApp.latLng.longitude ?: 0.0)
        }).setApiState(_promoData)
    }
}