package com.venus_customer.model.dataClass

import com.google.errorprone.annotations.Keep

@Keep
data class CreateProfileResponse(
    val address: String,
    val first_name: String,
    val is_referee: Boolean? = false,
    val last_name: String,
    val message: String? = "",
    val user_email: String,
    val user_image: String,
    val user_name: String
)