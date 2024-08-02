package com.venus_customer.view.fragment.cards

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venus_customer.model.api.ApiState
import com.venus_customer.model.api.setApiState
import com.venus_customer.model.dataClass.CardData
import com.venus_customer.model.dataClass.SetUpIntentResponse
import com.venus_customer.model.dataClass.base.BaseResponse
import com.venus_customer.repo.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(
    private val userRepo: UserRepo
) : ViewModel() {

    private var _addCardData = MutableLiveData<ApiState<BaseResponse<SetUpIntentResponse>>>()
    val addCardData: LiveData<ApiState<BaseResponse<SetUpIntentResponse>>> = _addCardData
    fun addCard(secret: String) = viewModelScope.launch {
        userRepo.addCard(secret).setApiState(_addCardData)
    }

    private var _confirmCardData = MutableLiveData<ApiState<BaseResponse<Any>>>()
    val confirmCardData: LiveData<ApiState<BaseResponse<Any>>> = _confirmCardData
    fun confirmCard(secret: String, intentId: String) = viewModelScope.launch {
        userRepo.confirmCard(secret, intentId).setApiState(_confirmCardData)
    }


    private var _getCardsData = MutableLiveData<ApiState<BaseResponse<List<CardData>>>>()
    val getCardsData: LiveData<ApiState<BaseResponse<List<CardData>>>> = _getCardsData
    fun getCardsData(type: Int) = viewModelScope.launch {
        userRepo.getCards(type).setApiState(_getCardsData)
    }
}