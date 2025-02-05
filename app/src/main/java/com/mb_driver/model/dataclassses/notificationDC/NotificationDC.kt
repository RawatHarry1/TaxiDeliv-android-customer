package com.mb_driver.model.dataclassses.notificationDC


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class NotificationDC(
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("notification_id")
    val notificationId: String? = null,
    @SerializedName("title")
    val title: String? = null
)