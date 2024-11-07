package com.superapp_driver.model.dataclassses.walletBalance


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class WalletBalanceDC(
    @SerializedName("payment_modes")
    val paymentModes: List<PaymentMode?>? = null,
    @SerializedName("preference")
    val preference: String? = null,
    @SerializedName("venus_balance")
    val venusBalance: String? = null,
    @SerializedName("user_name")
    val userName: String? = null
) {
    @Keep
    data class PaymentMode(
        @SerializedName("enabled")
        val enabled: String? = null,
        @SerializedName("name")
        val name: String? = null
    )
}