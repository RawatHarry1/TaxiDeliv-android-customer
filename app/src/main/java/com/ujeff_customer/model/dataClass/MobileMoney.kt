package com.ujeff_customer.model.dataClass

data class MobileMoney(
    val access_code: String,
    val authorization_url: String,
    val reference: String,
    val payment_status:Int
)