package com.mb_driver.repo

import com.mb_driver.model.api.ApiInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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


    suspend fun earningData() = flow {
        emit(apiInterface.getEarnings())
    }.flowOn(Dispatchers.IO)

}