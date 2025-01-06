package com.mcabs_driver.model.dataclassses



data class MessageData(
    val attachment: Any? = null,
    val attachment_type: String? = null,
    val chat_message_id: Int? = null,
    val created_at: String? = null,
    val engagement_id: Int? = null,
    val is_sender_read: Int? = null,
    val message: String? = null,
    val receiverDeviceType: Int? = null,
    val receiver_device_token: String? = null,
    val receiver_id: Int? = null,
    val sender_id: Int? = null,
    val sender_user_name: String? = null,
    val status: Int? = null,
    val thread: Int? = null,
    val thumbnail: Any? = null,
    val updated_at: String? = null,
    val utc_offset: String? = null
)

data class SocketMessage(
    val attachment: String? = null,
    val attachment_type: String? = null,
    val engagement_id: String? = null,
    val message: String? = null,
    val receiver_id: String? = null,
    val sender_id: String? = null,
    val thumbnail: String? = null
)
