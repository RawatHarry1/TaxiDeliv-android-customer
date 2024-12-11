package com.ujeff_driver.model.dataclassses

import com.google.errorprone.annotations.Keep

@Keep
data class CardData(
    val brand: String,
    val card_id: String,
    val created_at: String,
    val customer_id: String,
    val exp_month: Int,
    val exp_year: Int,
    val funding: String,
    val id: Int,
    val is_active: Int,
    val last_4: String,
    val operator_id: Int,
    val preference: Int,
    val tokenization_method: Int,
    val updated_at: String,
    val user_id: Int,
    var isSelected: Boolean? = false
)