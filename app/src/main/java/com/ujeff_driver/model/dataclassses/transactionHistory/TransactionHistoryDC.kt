package com.ujeff_driver.model.dataclassses.transactionHistory


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class TransactionHistoryDC(
    @SerializedName("balance")
    val balance: String? = null,
    @SerializedName("banner")
    val banner: String? = null,
    @SerializedName("num_txns")
    val numTxns: String? = null,
    @SerializedName("page_size")
    val pageSize: String? = null,
    @SerializedName("user_name")
    val userName: String? = null,
    @SerializedName("currency")
    val currency: String? = null,
    @SerializedName("transactions")
    val transactions: List<Transaction>? = null
) {
    @Keep
    data class Transaction(
        @SerializedName("amount")
        val amount: String? = null,
        @SerializedName("event")
        val event: String? = null,
        @SerializedName("freecharge")
        val freecharge: String? = null,
        @SerializedName("logged_on")
        val loggedOn: String? = null,
        @SerializedName("mobikwik")
        val mobikwik: String? = null,
        @SerializedName("paytm")
        val paytm: String? = null,
        @SerializedName("reference_id")
        val referenceId: String? = null,
        @SerializedName("txn_date")
        val txnDate: String? = null,
        @SerializedName("txn_id")
        val txnId: String? = null,
        @SerializedName("txn_time")
        val txnTime: String? = null,
        @SerializedName("txn_type")
        val txnType: String? = null,
        @SerializedName("wallet_txn")
        val walletTxn: String? = null
    )
}