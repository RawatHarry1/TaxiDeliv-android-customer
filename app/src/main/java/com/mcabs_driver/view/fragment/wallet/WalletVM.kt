package com.mcabs_driver.view.fragment.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mcabs_driver.model.api.ApiState
import com.mcabs_driver.model.api.SingleLiveEvent
import com.mcabs_driver.model.api.setApiState
import com.mcabs_driver.model.dataclassses.base.BaseResponse
import com.mcabs_driver.model.dataclassses.transactionHistory.TransactionHistoryDC
import com.mcabs_driver.repo.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class WalletVM @Inject constructor(
    private val userRepo: UserRepo
): ViewModel() {


    private val _walletData by lazy { SingleLiveEvent<ApiState<BaseResponse<TransactionHistoryDC>>>() }
    val walletData : LiveData<ApiState<BaseResponse<TransactionHistoryDC>>> get() = _walletData


//
//    private fun getWalletData() = viewModelScope.launch {
//        userRepo.getWalletBalance().setApiState(_walletData)
//    }


    fun getWalletTransactions() = viewModelScope.launch {
        userRepo.getTransactionHistory().setApiState(_walletData)
    }


    private val _addMoneyData by lazy { SingleLiveEvent<ApiState<BaseResponse<Any>>>() }
    val addMoneyData: LiveData<ApiState<BaseResponse<Any>>> get() = _addMoneyData
    fun addMoney(cardId: String, currency: String, amount: String) {
        viewModelScope.launch {
            userRepo.addMoney(
                jsonObject = JSONObject().apply {
                    put("stripe_3d_enabled", "1")
                    put("card_id", cardId)
                    put("amount", amount)
                    put("currency", currency)
                }
            ).setApiState(_addMoneyData)
        }
    }


}