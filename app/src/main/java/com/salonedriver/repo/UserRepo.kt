package com.salonedriver.repo

import com.salonedriver.SaloneDriver
import com.salonedriver.model.api.ApiInterface
import com.salonedriver.model.api.getJsonRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject
import javax.inject.Inject

class UserRepo @Inject constructor(
    private val apiInterface: ApiInterface
) {

    suspend fun getProfile() = flow {
        emit(apiInterface.getProfile())
    }.flowOn(Dispatchers.IO)


    suspend fun logout() = flow {
        emit(apiInterface.logout())
    }.flowOn(Dispatchers.IO)


    suspend fun changeAvailability(status: Boolean) = flow {
        emit(apiInterface.changeAvailability(requestBody = JSONObject().apply {
            put("flag", if (status) 1 else 0)
            put("latitude", SaloneDriver.latLng?.latitude)
            put("longitude", SaloneDriver.latLng?.longitude)
        }.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)


    suspend fun getNotifications(offset: Int) = flow {
        emit(apiInterface.getNotifications(offset = offset))
    }.flowOn(Dispatchers.IO)


    suspend fun getWalletBalance() = flow {
        emit(apiInterface.getWalletBalance())
    }.flowOn(Dispatchers.IO)



    suspend fun getVehicleList(cityId: String) = flow {
        emit(apiInterface.getVehicleData(cityId = cityId))
    }.flowOn(Dispatchers.IO)


    suspend fun getTransactionHistory() = flow {
        emit(apiInterface.getTransactionHistory())
    }.flowOn(Dispatchers.IO)

}