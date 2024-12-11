package com.ujeff_driver.repo

import com.ujeff_driver.model.api.ApiInterface
import com.ujeff_driver.model.api.getJsonRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject
import javax.inject.Inject

class BookingRepo @Inject constructor(
    private val apiInterface: ApiInterface
) {

    suspend fun bookingHistory() = flow {
        emit(apiInterface.bookingHistory())
    }.flowOn(Dispatchers.IO)


    suspend fun rideSummary(tripId: String) = flow {
        emit(apiInterface.rideSummary(tripId = tripId))
    }.flowOn(Dispatchers.IO)


    suspend fun earningData(filter: Int) = flow {
        emit(
            apiInterface.getEarnings(
                JSONObject().apply {
                    put("filter", filter)
                }.getJsonRequestBody()
            )
        )
    }.flowOn(Dispatchers.IO)

}