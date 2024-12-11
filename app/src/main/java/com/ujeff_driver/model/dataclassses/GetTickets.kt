package com.ujeff_driver.model.dataclassses

data class Ticket(
    val admin_id: Any,
    val admin_response: Any,
    val created_at: String,
    val description: String,
    val id: Int,
    val image: String,
    val response_at: Any,
    val ride_id: Int,
    val status: Int,
    val subject: String,
    val updated_at: String,
    val user_id: Int,
    val user_type: Int
)