package com.vyba_dri.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vyba_dri.model.api.ApiState
import com.vyba_dri.model.api.SingleLiveEvent
import com.vyba_dri.model.api.setApiState
import com.vyba_dri.model.dataclassses.base.BaseResponse
import com.vyba_dri.model.dataclassses.bookingHistory.BookingHistoryDC
import com.vyba_dri.model.dataclassses.bookingHistory.RideSummaryDC
import com.vyba_dri.model.dataclassses.earningDC.EarningDC
import com.vyba_dri.repo.BookingRepo
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

    fun getEarningData() = viewModelScope.launch {
        bookingRepo.earningData().setApiState(_earningData)
    }

}