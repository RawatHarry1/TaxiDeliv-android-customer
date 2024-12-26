package com.matter.companion.model.dataclassses.base

class FailureResponse {
    var errorCode: Int = 0
    var errorMessage: CharSequence? = null
        private set

    constructor() {}

    constructor(errorCode: Int, errorMessage: CharSequence) {
        this.errorCode = errorCode
        this.errorMessage = errorMessage
    }

    fun setErrorMessage(errorMessage: String) {
        this.errorMessage = errorMessage
    }
}