package com.salonedriver.view.fragment.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salonedriver.model.api.ApiState
import com.salonedriver.model.api.SingleLiveEvent
import com.salonedriver.model.api.setApiState
import com.salonedriver.model.dataclassses.base.BaseResponse
import com.salonedriver.model.dataclassses.notificationDC.NotificationDC
import com.salonedriver.repo.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsVM @Inject constructor(
    private val userRepo: UserRepo
): ViewModel() {

    var currentPage: Int = 1
    var isLoading = false
    var isLastPage = false

    private var _notificationData = MutableLiveData<ApiState<BaseResponse<List<NotificationDC>>>>()
    val notificationData : LiveData<ApiState<BaseResponse<List<NotificationDC>>>> = _notificationData


    init {
        getNotifications()
    }

    fun getNotifications() = viewModelScope.launch {
        userRepo.getNotifications(currentPage).setApiState(_notificationData)
    }

}