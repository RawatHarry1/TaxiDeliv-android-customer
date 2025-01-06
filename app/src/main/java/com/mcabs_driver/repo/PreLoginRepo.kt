package com.mcabs_driver.repo

import com.mcabs_driver.model.api.ApiInterface
import com.mcabs_driver.model.api.getJsonRequestBody
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

    suspend fun getClientConfig() = flow {
        emit(apiInterface.getClientConfig())
    }.flowOn(Dispatchers.IO)



    suspend fun generateLoginOtp(jsonObject: JSONObject) = flow {
        emit(apiInterface.generateLoginOtp(requestBody = jsonObject.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)


    suspend fun verifyLoginOtp(jsonObject: JSONObject) = flow {
        emit(apiInterface.verifyLoginOtp(requestBody = jsonObject.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)


    suspend fun updateDriverInfo(
        hashMap: HashMap<String, RequestBody?>,
        part: MultipartBody.Part?
    ) = flow {
        emit(apiInterface.updateDriverInfo(partMap = hashMap, multipartBody = part))
    }.flowOn(Dispatchers.IO)


    suspend fun fetchRequiredDocument() = flow {
        emit(apiInterface.fetchRequiredDocument())
    }.flowOn(Dispatchers.IO)


    suspend fun uploadDocument(hashMap: HashMap<String, RequestBody?>, part: MultipartBody.Part) =
        flow {
            emit(apiInterface.uploadDocument(partMap = hashMap, multipartBody = part))
        }.flowOn(Dispatchers.IO)


    suspend fun getCityVehicle(cityId: String,rideType:Int) = flow {
        emit(apiInterface.getCityVehicles(cityId,rideType))
    }.flowOn(Dispatchers.IO)


    suspend fun updateVehicleInfo(jsonObject: JSONObject) = flow {
        emit(apiInterface.updateDriverVehicle(requestBody = jsonObject.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)


    suspend fun addBankDetail(jsonObject: JSONObject) = flow {
        emit(apiInterface.addBankDetail(requestBody = jsonObject.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)


    suspend fun loginViaAccessToken(latitude: Double, longitude: Double) = flow {
        emit(apiInterface.loginViaAccessToken(JSONObject().apply {
            put("latitude", latitude)
            put("longitude", longitude)
        }.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)


    suspend fun aboutUsData(operatorId: String, cityId: String,type:Int) = flow {
        emit(
            apiInterface.aboutUs(
                operatorId, cityId,type
            )
        )
    }.flowOn(Dispatchers.IO)

}