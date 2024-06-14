package com.venus_customer.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.venus_customer.R
import com.venus_customer.databinding.ItemFaqsBinding
import com.venus_customer.databinding.ItemPromoCodesBinding
import com.venus_customer.util.gone
import com.venus_customer.util.visible
import kotlin.collections.ArrayList

class OffersAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OfferViewHolder(
            ItemPromoCodesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return 4

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as OfferViewHolder).onBind(position)
    }

    fun refreshData() {

        notifyDataSetChanged()
    }

    inner class OfferViewHolder(private val binding: ItemPromoCodesBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(position1: Int) {

        }
    }

}


