package com.ujeff_driver.repo

import com.ujeff_driver.SaloneDriver
import com.ujeff_driver.model.api.ApiInterface
import com.ujeff_driver.model.api.getJsonRequestBody
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

    suspend fun updateDriverLocation() = flow {
        emit(apiInterface.updateDriverLocation(JSONObject().apply {
            put("latitude", SaloneDriver.latLng?.latitude)
            put("longitude", SaloneDriver.latLng?.longitude)
        }.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)

    suspend fun logout() = flow {
        emit(apiInterface.logout())
    }.flowOn(Dispatchers.IO)

    suspend fun deleteAccount() = flow {
        emit(apiInterface.deleteAccount())
    }.flowOn(Dispatchers.IO)


    suspend fun changeAvailability(status: Boolean) = flow {
        emit(apiInterface.changeAvailability(requestBody = JSONObject().apply {
            put("flag", if (status) 1 else 0)
            put("latitude", SaloneDriver.latLng?.latitude ?: 0.0)
            put("longitude", SaloneDriver.latLng?.longitude?: 0.0)
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

    suspend fun addMoney(jsonObject: JSONObject) = flow {
        emit(apiInterface.addMoney(requestBody = jsonObject.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)

    suspend fun addCard(clientSecret: String) = flow {
        emit(apiInterface.addCard(clientSecret))
    }.flowOn(Dispatchers.IO)

    suspend fun confirmCard(clientSecret: String, intentId: String) = flow {
        emit(apiInterface.confirmCard(clientSecret, intentId))
    }.flowOn(Dispatchers.IO)

    suspend fun getCards(type: Int) = flow {
        emit(apiInterface.getCards(type))
    }.flowOn(Dispatchers.IO)

    suspend fun deleteCard(cardId: String) = flow {
        emit(apiInterface.deleteCard(cardId))
    }.flowOn(Dispatchers.IO)

}