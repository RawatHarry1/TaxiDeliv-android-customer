package com.ujeff_driver.model.api

import com.ujeff_driver.model.dataclassses.base.ErrorModel

data class ApiState<out T>(val status: Status, val data: T?, val errorModel: ErrorModel?) {
    companion object {

        // In case of Success,set status as
        // Success and data as the response
        fun <T> success(data: T?): ApiState<T> {
            return ApiState(Status.SUCCESS, data, null)
        }

        // In case of failure ,set state to Error ,
        // add the error message,set data to null
        fun <T> error(errorModel: ErrorModel?): ApiState<T> {
            return ApiState(Status.ERROR, null, errorModel)
        }

        // When the call is loading set the state
        // as Loading and rest as null
        fun <T> loading(): ApiState<T> {
            return ApiState(Status.LOADING, null, null)
        }

        // By Default Pass state as Empty
        fun <T> empty(): ApiState<T> {
            return ApiState(Status.EMPTY, null, null)
        }
    }
}

// An enum to store the
// current state of api call
enum class Status {
    SUCCESS,
    ERROR,
    LOADING,
    EMPTY
}