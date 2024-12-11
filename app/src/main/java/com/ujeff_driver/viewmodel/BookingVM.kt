package com.ujeff_driver.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ujeff_driver.model.api.ApiState
import com.ujeff_driver.model.api.SingleLiveEvent
import com.ujeff_driver.model.api.setApiState
import com.ujeff_driver.model.dataclassses.base.BaseResponse
import com.ujeff_driver.model.dataclassses.bookingHistory.BookingHistoryDC
import com.ujeff_driver.model.dataclassses.bookingHistory.RideSummaryDC
import com.ujeff_driver.model.dataclassses.earningDC.EarningDC
import com.ujeff_driver.repo.BookingRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingVM @Inject constructor(
    private val bookingRepo: BookingRepo
) : ViewModel() {



    private val _bookingHistoryData by lazy { SingleLiveEvent<ApiState<BaseResponse<List<BookingHistoryDC>>>>() }
    val bookingHistoryData : LiveData<ApiState<BaseResponse<List<BookingHistoryDC>>>> get() = _bookingHistoryData

    fun getBookingHistory() = viewModelScope.launch {
        bookingRepo.bookingHistory().setApiState(_bookingHistoryData)
    }


    private val _rideSummaryData by lazy { SingleLiveEvent<ApiState<BaseResponse<RideSummaryDC>>>() }
    val rideSummaryData : LiveData<ApiState<BaseResponse<RideSummaryDC>>> get() = _rideSummaryData
    fun rideSummary(tripId: String) = viewModelScope.launch {
        bookingRepo.rideSummary(tripId = tripId).setApiState(_rideSummaryData)
    }



    private val _earningData by lazy { SingleLiveEvent<ApiState<BaseResponse<EarningDC>>>() }
    val earningData : LiveData<ApiState<BaseResponse<EarningDC>>> get() = _earningData

    fun getEarningData(filter:Int) = viewModelScope.launch {
        bookingRepo.earningData(filter).setApiState(_earningData)
    }

}