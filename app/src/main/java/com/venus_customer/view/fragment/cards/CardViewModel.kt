package com.venus_customer.view.fragment.cards

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venus_customer.model.api.ApiState
import com.venus_customer.model.api.setApiState
import com.venus_customer.model.dataClass.base.BaseResponse
import com.venus_customer.repo.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(
    private val userRepo: UserRepo
) : ViewModel() {

    private var _addCardData = MutableLiveData<ApiState<BaseResponse<Any>>>()
    val addCardData: LiveData<ApiState<BaseResponse<Any>>> = _addCardData
    fun addCard(secret: String) = viewModelScope.launch {
        userRepo.addCard(secret).setApiState(_addCardData)
    }
}