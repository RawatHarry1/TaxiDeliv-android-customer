package com.venus_customer.model.api

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.venus_customer.di.ErrorModel
import com.venus_customer.model.dataClass.base.BaseResponse
import com.venus_customer.util.showSessionExpire
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import retrofit2.Response


/**
 * Observe Data
 * */
fun <T> LiveData<ApiState<BaseResponse<T>>>.observeData(
    lifecycle: LifecycleOwner,
    onLoading: () -> Unit = {},
    onSuccess: T?.() -> Unit = {},
    onError: String.() -> Unit = {}
) {
    observe(lifecycle) {
        when (it.status) {
            //When api in loading state
            Status.LOADING -> onLoading.invoke()

            //When api getting success
            Status.SUCCESS -> when (it.data?.flag) {
                113 -> {
                    showSessionExpire()
                }

                143 -> {
                    onSuccess(it.data.data)
                }

                else -> {
                    onError.invoke(it.data?.message.orEmpty())
                }
            }

            //When api getting error
            Status.ERROR -> {
                if (it.errorModel?.statusCode == "503")
                    onError.invoke("Something went wrong")
                else
                    onError.invoke(it.errorModel?.message.orEmpty())
            }

            //No Status
            else -> Unit
        }
    }
}


/**
 * Fetch API Data FLow and set in Mutable Live Data
 * */
suspend fun <T> Flow<Response<T>>.setApiState(mutableLiveData: MutableLiveData<ApiState<T>>) =
    onStart {
        mutableLiveData.value = ApiState.loading()
    }.catch { error ->
        mutableLiveData.value =
            ApiState.error(ErrorModel(message = error.localizedMessage.orEmpty()))
    }.collectLatest {
        if (it.isSuccessful)
            mutableLiveData.value = ApiState.success(it.body())
        else
            mutableLiveData.value = ApiState.error(
                ErrorModel(
                    message = it.errorBody()?.string(),
                    statusCode = it.code().toString()
                )
            )
    }