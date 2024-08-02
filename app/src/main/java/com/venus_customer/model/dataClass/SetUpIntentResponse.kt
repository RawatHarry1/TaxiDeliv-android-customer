package com.venus_customer.model.dataClass

import com.google.errorprone.annotations.Keep

@Keep
data class SetUpIntentResponse(
    val client_secret: String,
    val setupIntent: SetupIntent
)
@Keep
data class SetupIntent(
    val application: Any,
    val automatic_payment_methods: Any,
    val cancellation_reason: Any,
    val client_secret: String,
    val created: Int,
    val customer: Any,
    val description: Any,
    val flow_directions: Any,
    val id: String,
    val last_setup_error: Any,
    val latest_attempt: Any,
    val livemode: Boolean,
    val mandate: Any,
    val metadata: Metadata,
    val next_action: Any,
    val `object`: String,
    val on_behalf_of: Any,
    val payment_method: Any,
    val payment_method_configuration_details: Any,
    val payment_method_options: PaymentMethodOptions,
    val payment_method_types: List<String>,
    val single_use_mandate: Any,
    val status: String,
    val usage: String
)

data class Metadata(
    val user_id: String
)

data class PaymentMethodOptions(
    val card: Card
)

data class Card(
    val mandate_options: Any,
    val network: Any,
    val request_three_d_secure: String
)