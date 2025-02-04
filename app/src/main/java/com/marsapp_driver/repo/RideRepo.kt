package com.marsapp_driver.repo

import com.marsapp_driver.SaloneDriver
import com.marsapp_driver.model.api.ApiInterface
import com.marsapp_driver.model.api.getJsonRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class RideRepo @Inject constructor(
    private val apiInterface: ApiInterface
) {
    suspend fun uploadPackageImage(
        part: MultipartBody.Part,
        hashMap: HashMap<String, RequestBody?>
    ) =
        flow {
            emit(apiInterface.uploadPackageImage(multipartBody = part, hashMap))
        }.flowOn(Dispatchers.IO)

    suspend fun rejectRide(tripId: String) = flow {
        emit(
            apiInterface.rejectRide(
                requestBody = JSONObject().apply {
                    put("tripId", tripId)
                }.getJsonRequestBody()
            )
        )
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


    suspend fun endTrip(
        customerId: String,
        tripId: String,
        dropLatitude: String,
        dropLongitude: String,
        distanceTravelled: String,
        rideTime: String,
        waitTime: String
    ) = flow {
        emit(apiInterface.endTrip(requestBody = JSONObject().apply {
            put("customerId", customerId)
            put("tripId", tripId)
            put("dropLatitude", SaloneDriver.latLng?.latitude)
            put("dropLongitude", SaloneDriver.latLng?.longitude)
            put("distanceTravelled", "10")
            put("rideTime", "12")
            put("waitTime", "3")
        }.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)

    suspend fun updatePackageStatus(
        sessionId: String,
        driverId: String,
        packageId: String,
        cancellationReason: String? = null,
        packageImages: List<String>? = null,
        isEnd: Boolean,
        currentLat: String? = null,
        currentLan: String? = null,
        dropLat: String? = null,
        dropLan: String? = null,
        cityId: String? = null,
        isRestrictionEnabled: Int? = null,
        distance: String? = null
    ) = flow {
        emit(apiInterface.updatePackageStatus(requestBody = JSONObject().apply {
            put("trip_id", sessionId)
            put("driver_id", driverId)
            put("package_id", packageId)

            if (cancellationReason != null)
            put("cancelltion_reason", cancellationReason)

            if (!packageImages.isNullOrEmpty())
                put("package_images", JSONArray(packageImages))
//            put("notes", "")

            if (isEnd) {
                put("is_for_end", "1")
                put("current_latitude", currentLat)
                put("current_longitude", currentLan)
                put("drop_latitude", dropLat)
                put("drop_longitude", dropLan)
                put("city_id", cityId)
                put("package_delivery_restriction_enabled", isRestrictionEnabled)
                val d = if (distance.isNullOrEmpty()) 0.0 else distance.toDouble()
                put("maximum_distance", d)
            }
            else
                put("is_for_pickup", "1")

        }.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)


    suspend fun ongoingTrip() = flow {
        emit(apiInterface.ongoingTrip())
    }.flowOn(Dispatchers.IO)


    suspend fun cancelTrip(jsonObject: JSONObject) = flow {
        emit(apiInterface.cancelTrip(jsonObject.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)

    suspend fun rateCustomer(jsonObject: JSONObject) = flow {
        emit(apiInterface.rateCustomer(jsonObject.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)

    suspend fun generateSupportTicket(jsonObject: JSONObject) = flow {
        emit(apiInterface.generateSupportTicket(jsonObject.getJsonRequestBody()))
    }.flowOn(Dispatchers.IO)

    suspend fun raiseATicket(jsonObject: JSONObject) = flow {
        emit(
            apiInterface.generateTicket(
                requestBody = jsonObject.getJsonRequestBody()
            )
        )
    }.flowOn(Dispatchers.IO)

    suspend fun getTicketsList() = flow {
        emit(apiInterface.getRaisedListing())
    }.flowOn(Dispatchers.IO)

    suspend fun uploadTicketFile(part: MultipartBody.Part, hashMap: HashMap<String, RequestBody?>) =
        flow {
            emit(apiInterface.uploadTicketFile(multipartBody = part, hashMap))
        }.flowOn(Dispatchers.IO)
}