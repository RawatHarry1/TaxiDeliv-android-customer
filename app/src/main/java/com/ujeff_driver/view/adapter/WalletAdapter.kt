package com.ujeff_driver.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ujeff_driver.R
import com.ujeff_driver.databinding.ItemWalletBinding
import com.ujeff_driver.model.dataclassses.transactionHistory.TransactionHistoryDC
import com.ujeff_driver.util.SharedPreferencesManager
import com.ujeff_driver.util.formatAmount
import com.ujeff_driver.util.getTime

class WalletAdapter : RecyclerView.Adapter<WalletAdapter.WalletViewHolder>() {

    private val list by lazy { mutableListOf<TransactionHistoryDC.Transaction>() }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WalletAdapter.WalletViewHolder {
        return WalletViewHolder(
            ItemWalletBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size

    }

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    fun submitList(list: List<TransactionHistoryDC.Transaction>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class WalletViewHolder(private val binding: ItemWalletBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun onBind(transaction: TransactionHistoryDC.Transaction) {
            binding.titleAmountAdded.text = "Amount ${transaction.txnType.orEmpty()}"
            if (transaction.txnType.equals("Debited", true)) {
                binding.tvAmountAdded.text = "- ${SharedPreferencesManager.getCurrencySymbol()} ${
                    transaction.amount.orEmpty().ifEmpty { "0.0" }.formatAmount()
                }"
                binding.tvAmountAdded.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.red_text_color
                    )
                )
            } else {
                binding.tvAmountAdded.text = "+ ${SharedPreferencesManager.getCurrencySymbol()} ${
                    transaction.amount.orEmpty().ifEmpty { "0.0" }.formatAmount()
                }"
                binding.tvAmountAdded.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.green_text_color
                    )
                )
            }
            binding.tvDate.text = transaction.loggedOn.orEmpty().getTime(
                input = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                output = "MMM dd, yyyy",
                applyTimeZone = true
            )
            binding.tvTime.text = transaction.loggedOn.orEmpty().getTime(
                input = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                output = "HH:mm a",
                applyTimeZone = true
            )
            binding.tvProcessNo.text = transaction.referenceId.orEmpty()
        }
    }

}


