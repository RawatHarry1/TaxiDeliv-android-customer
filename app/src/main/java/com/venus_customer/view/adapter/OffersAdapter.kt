package com.venus_customer.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.venus_customer.databinding.ItemPromoCodesBinding
import com.venus_customer.model.dataClass.Coupon

class OffersAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val list by lazy { ArrayList<Coupon>() }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OfferViewHolder(
            ItemPromoCodesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as OfferViewHolder).onBind(list[position])
    }

    fun refreshData() {
        notifyDataSetChanged()
    }

    inner class OfferViewHolder(private val binding: ItemPromoCodesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(coupon: Coupon) {
            binding.tvTitle.text = coupon.title
            binding.tvDesc.text = coupon.description
        }
    }

    fun submitList(list: List<Coupon>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }
}


