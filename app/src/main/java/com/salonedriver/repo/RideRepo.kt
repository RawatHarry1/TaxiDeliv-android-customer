package com.salonedriver.repo

import com.salonedriver.SaloneDriver
import com.salonedriver.model.api.ApiInterface
import com.salonedriver.model.api.getJsonRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject
import javax.inject.Inject

class RideRepo @Inject constructor(
    private val apiInterface: ApiInterface
){

    suspend fun rejectRide(tripId: String) = flow {
        emit(apiInterface.rejectRide(
            requestBody = JSONObject().apply {
                put("tripId", tripId)
            }.getJsonRequestBody()
        ))
    }.flowOn(Dispatchers.IO)


    suspend fun acceptRide(customerId: String, tripId: String) = flow {
        emit(apiInterface.acceptRide(requestBody = JSONObject().apply {
            put("longitude", SaloneDriver.latLng?.longitude)
            put("latitude", SaloneDriver.latLng?.latitude)
            put("customerId", customerId)
            put("tripId", tripId)
        }.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)


    suspend fun markArrived(customerId: String, tripId: String) = flow {
        emit(apiInterface.markArrived(requestBody = JSONObject().apply {
            put("pickupLatitude", SaloneDriver.latLng?.latitude)
            put("pickupLongitude", SaloneDriver.latLng?.longitude)
            put("customerId", customerId)
            put("tripId", tripId)
        }.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)


    suspend fun startTrip(customerId: String, tripId: String) = flow {
        emit(apiInterface.startTrip(requestBody = JSONObject().apply {
            put("pickupLatitude", SaloneDriver.latLng?.latitude)
            put("pickupLongitude", SaloneDriver.latLng?.longitude)
            put("customerId", customerId)
            put("tripId", tripId)
        }.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)


    suspend fun endTrip(customerId: String, tripId: String, dropLatitude: String, dropLongitude: String, distanceTravelled: String, rideTime: String, waitTime: String) = flow {
        emit(apiInterface.endTrip(requestBody = JSONObject().apply {
            put("customerId",customerId)
            put("tripId",tripId)
            put("dropLatitude",SaloneDriver.latLng?.latitude)
            put("dropLongitude",SaloneDriver.latLng?.longitude)
            put("distanceTravelled","10")
            put("rideTime","12")
            put("waitTime","3")
        }.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)


    suspend fun ongoingTrip() = flow {
        emit(apiInterface.ongoingTrip())
    }.flowOn(Dispatchers.IO)


    suspend fun cancelTrip(jsonObject: JSONObject) = flow {
        emit(apiInterface.cancelTrip(jsonObject.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)
}