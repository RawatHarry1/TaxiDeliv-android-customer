package com.superapp_customer.model.dataClass


data class WalletTransaction(
    val balance: Double?,
    val banner: String,
    val currency: String?,
    val num_txns: Int,
    val page_size: Int,
    val transactions: List<TransactionData>,
    val user_name: String
)

data class TransactionData(
    val txn_id: Int,
    val txn_type: String,
    val amount: Double?,
    val txn_date: String,
    val txn_time: String,
    val logged_on: String,
    val wallet_txn: Int?,
    val paytm: Int?,
    val mobikwik: Int?,
    val freecharge: Int?,
    val reference_id: Int?,
    val event: Int?
)