package com.ujeff_customer.repo

import com.ujeff_customer.model.api.ApiInterface
import com.ujeff_customer.model.api.getJsonRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import javax.inject.Inject

class PreLoginRepo @Inject constructor(
    private val apiInterface: ApiInterface

) {

    suspend fun fetchOperatorToken() = flow {
        emit(apiInterface.fetchUserToken())
    }.flowOn(Dispatchers.IO)


    suspend fun sendLoginOtp(
        jsonObject: JSONObject
    ) = flow {
        emit(apiInterface.sendLoginOtp(body = jsonObject.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)


    suspend fun verifyOtp(
        jsonObject: JSONObject
    ) = flow {
        emit(apiInterface.verifyOtp(body = jsonObject.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)


    suspend fun loginViaAccessToken(
        jsonObject: JSONObject
    ) = flow {
        emit(apiInterface.loginViaAccessToken(requestBody = jsonObject.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)


    suspend fun updateProfile(
        hashMap: HashMap<String, RequestBody?>, part: MultipartBody.Part?
    ) = flow {
        emit(apiInterface.updateProfile(hashMap, part))
    }.flowOn(Dispatchers.IO)


    suspend fun logout() = flow {
        emit(apiInterface.logout())
    }.flowOn(Dispatchers.IO)

    suspend fun deleteAccount() = flow {
        emit(apiInterface.deleteAccount())
    }.flowOn(Dispatchers.IO)


    suspend fun aboutApp(operatorId: String, cityId: String, type: Int) = flow {
        emit(apiInterface.aboutApp(operatorId = operatorId, cityId = cityId, type = type))
    }.flowOn(Dispatchers.IO)

    suspend fun getTransactions(jsonObject: JSONObject) = flow {
        emit(apiInterface.getTransactions(requestBody = jsonObject.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)

    suspend fun addMoney(jsonObject: JSONObject) = flow {
        emit(apiInterface.addMoney(requestBody = jsonObject.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)

    suspend fun getCouponAndPromo(jsonObject: JSONObject) = flow {
        emit(apiInterface.getCouponAndPromo(requestBody = jsonObject.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)

}