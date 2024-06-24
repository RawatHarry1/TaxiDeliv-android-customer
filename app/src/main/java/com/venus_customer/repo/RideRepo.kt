package com.venus_customer.repo

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.venus_customer.R
import com.venus_customer.VenusApp
import com.venus_customer.model.api.ApiInterface
import com.venus_customer.model.api.getJsonRequestBody
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject
import javax.inject.Inject

class RideRepo @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiInterface: ApiInterface
) {

    suspend fun searchPlaces(
        search: String
    ) = flow {
        emit(
            apiInterface.searchPlaces(
                inputText = search,
                key = context.getString(R.string.map_api_key)
            )
        )
    }.flowOn(Dispatchers.IO)

    suspend fun getDistanceFromGoogle(
        originId: String,
        destinationId: String
    ) = flow {
        emit(
            apiInterface.getDistanceFromGoogle(
                destination = "place_id:$destinationId",
                origin = "place_id:$originId",
                key = context.getString(R.string.map_api_key)
            )
        )
    }

    suspend fun findDriver(
        latLng: LatLng,
        opLatLng: LatLng,
        isSchedule: Boolean
    ) = flow {
        emit(apiInterface.findDriver(JSONObject().apply {
            put("latitude", latLng.latitude)
            put("longitude", latLng.longitude)
//            if (isSchedule) {
            put("op_drop_latitude", opLatLng.latitude)
            put("op_drop_longitude", opLatLng.longitude)
//            }
        }.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)


    suspend fun requestTrip(jsonObject: JSONObject) = flow {
        emit(
            apiInterface.requestTrip(
                requestBody = jsonObject.getJsonRequestBody()
            )
        )
    }.flowOn(Dispatchers.IO)

    suspend fun requestSchedule(jsonObject: JSONObject) = flow {
        emit(
            apiInterface.requestSchedule(
                requestBody = jsonObject.getJsonRequestBody()
            )
        )
    }.flowOn(Dispatchers.IO)


    suspend fun cancelTrip(sessionId: String, jsonObject: JSONObject) = flow {
        emit(
            apiInterface.cancelTrip(
                sessionId = sessionId,
                requestBody = jsonObject.getJsonRequestBody()
            )
        )
    }.flowOn(Dispatchers.IO)


    suspend fun fareEstimate(jsonObject: JSONObject) = flow {
        emit(apiInterface.fareEstimate(requestBody = jsonObject.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)


    suspend fun rateDriver(jsonObject: JSONObject) = flow {
        emit(apiInterface.rateDriver(requestBody = jsonObject.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)


    suspend fun fetchOngoingTrip() = flow {
        emit(apiInterface.fetchOngoingTrip())
    }.flowOn(Dispatchers.IO)

    suspend fun fetchAddedAddresses() = flow {
        emit(apiInterface.fetchAddresses())
    }.flowOn(Dispatchers.IO)


    suspend fun getAllTrips() = flow {
        emit(apiInterface.getAllRides())
    }.flowOn(Dispatchers.IO)

    suspend fun getAllSchedules() = flow {
        emit(apiInterface.getAllScheduleRides())
    }.flowOn(Dispatchers.IO)

    suspend fun removeSchedules(jsonObject: JSONObject) = flow {
        emit(apiInterface.removeSchedule(requestBody = jsonObject.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)


    suspend fun getRideSummary(
        tripId: String,
        driverId: String
    ) = flow {
        emit(apiInterface.getTripSummary(tripId, driverId))
    }.flowOn(Dispatchers.IO)


    suspend fun findNearDriver(latitude: Double, longitude: Double) = flow {
        emit(
            apiInterface.findNearDriver(
                JSONObject().apply {
                    put("latitude", VenusApp.latLng.latitude)
                    put("longitude", VenusApp.latLng.longitude)
                }.getJsonRequestBody()
            )
        )
    }

    suspend fun addAddress(request: okhttp3.RequestBody) = flow {
        emit(apiInterface.addAddress(request))
    }

}