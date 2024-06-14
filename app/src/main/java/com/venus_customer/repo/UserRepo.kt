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


}