package com.ujeff_customer.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ujeff_customer.R
import com.ujeff_customer.databinding.ItemWalletBinding
import com.ujeff_customer.model.dataClass.TransactionData

class WalletAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list by lazy { ArrayList<TransactionData>() }
    private var currency = "INR"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return WalletViewHolder(
            ItemWalletBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as WalletViewHolder).onBind(list[position])
    }

    fun refreshData() {
        notifyDataSetChanged()
    }

    fun setCurrency(currency: String) {
        this.currency = currency
    }

    inner class WalletViewHolder(private val binding: ItemWalletBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun onBind(data: TransactionData) {
            binding.titleAmountAdded.text = "Amount ${data.txn_type}"
            if (data.txn_type.equals("Debited", true))
                binding.tvAmountAdded.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.cancel_btn_red_color
                    )
                )
            else
                binding.tvAmountAdded.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.green_text_color
                    )
                )

            binding.tvAmountAdded.text = "$currency ${data.amount ?: 0.0}"
            binding.tvDate.text = "${data.txn_date ?: ""}"
            binding.tvTime.text = "${data.txn_time ?: ""}"
            binding.tvProcessNo.text = "${data.txn_id ?: ""}"
        }
    }

    fun submitList(list: List<TransactionData>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

}


