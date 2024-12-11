package com.ujeff_customer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ujeff_customer.di.ErrorModel
import com.ujeff_customer.model.api.ApiState
import com.ujeff_customer.model.api.SingleLiveEvent
import com.ujeff_customer.repo.RideRepo
import com.ujeff_customer.viewmodel.rideVM.CreateRideData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class SearchLocationVM @Inject constructor(
    private val rideRepo: RideRepo
) : ViewModel() {


    private val _searchLocationData by lazy { SingleLiveEvent<ApiState<List<CreateRideData.LocationData>>>() }
    val searchLocationData: LiveData<ApiState<List<CreateRideData.LocationData>>> =
        _searchLocationData


    fun searchPlaces(searchText: String) = viewModelScope.launch {
        rideRepo.searchPlaces(search = searchText).onStart {
            _searchLocationData.value = ApiState.loading()
        }.catch { error ->
            _searchLocationData.value =
                ApiState.error(ErrorModel(message = error.localizedMessage.orEmpty()))
        }.collectLatest {
            if (it.isSuccessful) {
                val array = ArrayList<CreateRideData.LocationData>()
                JSONObject(it.body().toString()).apply {
                    if (has("results")) {
                        getJSONArray("results").let { predictions ->
                            for (index in 0 until predictions.length()) {
                                val prediction = predictions.getJSONObject(index)
                                val address = prediction.optString("formatted_address")
                                val latitude =
                                    prediction.optJSONObject("geometry")?.optJSONObject("location")
                                        ?.optString("lat").orEmpty()
                                val longitude =
                                    prediction.optJSONObject("geometry")?.optJSONObject("location")
                                        ?.optString("lng").orEmpty()
                                val placeId = prediction.optString("place_id").orEmpty()
                                CreateRideData.LocationData(
                                    latitude = latitude,
                                    longitude = longitude,
                                    address = address,
                                    placeId = placeId
                                ).apply {
                                    array.add(this)
                                }
                            }
                        }
                    }
                }
                _searchLocationData.value = ApiState.success(array)
            } else {
                _searchLocationData.value =
                    ApiState.error(ErrorModel(message = it.errorBody()?.string().toString()))
            }
        }
    }


    private val _addAddress by lazy { SingleLiveEvent<ApiState<Any>>() }
    val addAddress: LiveData<ApiState<Any>> = _addAddress

    fun addAddress(requestBody: RequestBody) = viewModelScope.launch {
        rideRepo.addAddress(requestBody).onStart {
            _addAddress.value = ApiState.loading()
        }.catch { error ->
            _addAddress.value =
                ApiState.error(ErrorModel(message = error.localizedMessage.orEmpty()))
        }.collectLatest {
            if (it.isSuccessful) {
                _addAddress.value = ApiState.success(it)
            } else {
                _addAddress.value =
                    ApiState.error(ErrorModel(message = it.errorBody()?.string().toString()))
            }
        }
    }

}