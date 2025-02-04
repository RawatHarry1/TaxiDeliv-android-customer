package com.marsapp_driver.model.dataclassses.updateDriverInfo


import com.google.gson.annotations.SerializedName

data class UpdateDriverInfo(
    @SerializedName("address")
    val address: String? = null,
    @SerializedName("date_of_birth")
    val dateOfBirth: String? = null,
    @SerializedName("driver_id")
    val driverId: Int? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("emergency_phone_number")
    val emergencyPhoneNumber: String? = null,
    @SerializedName("first_name")
    val firstName: String? = null,
    @SerializedName("last_name")
    val lastName: String? = null,
    @SerializedName("phone_no")
    val phoneNo: String? = null,
    @SerializedName("profile_image")
    val profileImage: String? = null,
    @SerializedName("user_name")
    val userName: String? = null
)