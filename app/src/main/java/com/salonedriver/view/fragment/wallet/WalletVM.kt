package com.salonedriver.view.fragment.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salonedriver.model.api.ApiState
import com.salonedriver.model.api.SingleLiveEvent
import com.salonedriver.model.api.setApiState
import com.salonedriver.model.dataclassses.base.BaseResponse
import com.salonedriver.model.dataclassses.transactionHistory.TransactionHistoryDC
import com.salonedriver.model.dataclassses.walletBalance.WalletBalanceDC
import com.salonedriver.repo.UserRepo
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