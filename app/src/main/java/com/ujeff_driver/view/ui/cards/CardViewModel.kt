package com.ujeff_driver.view.ui.cards

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ujeff_driver.model.api.ApiState
import com.ujeff_driver.model.api.setApiState
import com.ujeff_driver.model.dataclassses.CardData
import com.ujeff_driver.model.dataclassses.SetUpIntentResponse
import com.ujeff_driver.model.dataclassses.base.BaseResponse
import com.ujeff_driver.repo.UserRepo
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

    private var _deleteCardsData = MutableLiveData<ApiState<BaseResponse<Any>>>()
    val deleteCardsData: LiveData<ApiState<BaseResponse<Any>>> = _deleteCardsData
    fun deleteCardsData(cardId: String) {
        viewModelScope.launch {
            userRepo.deleteCard(cardId).setApiState(_deleteCardsData)
        }
    }
}