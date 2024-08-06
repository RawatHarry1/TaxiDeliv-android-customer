package com.venus_customer.repo

import android.content.Context
import com.venus_customer.model.api.ApiInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class UserRepo @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiInterface: ApiInterface
) {


    suspend fun getNotifications(offset: Int) = flow {
        emit(apiInterface.getNotifications(offset = offset))
    }.flowOn(Dispatchers.IO)

    suspend fun addCard(clientSecret: String) = flow {
        emit(apiInterface.addCard(clientSecret))
    }.flowOn(Dispatchers.IO)

    suspend fun confirmCard(clientSecret: String, intentId: String) = flow {
        emit(apiInterface.confirmCard(clientSecret, intentId))
    }.flowOn(Dispatchers.IO)

    suspend fun getCards(type: Int) = flow {
        emit(apiInterface.getCards(type))
    }.flowOn(Dispatchers.IO)

    suspend fun deleteCard(cardId: String) = flow {
        emit(apiInterface.deleteCard(cardId))
    }.flowOn(Dispatchers.IO)


}