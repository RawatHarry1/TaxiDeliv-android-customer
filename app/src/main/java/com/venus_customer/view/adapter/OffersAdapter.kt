package com.venus_customer.view.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.venus_customer.VenusApp
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.ItemPromoCodesBinding
import com.venus_customer.model.dataClass.Promotion

class OffersAdapter(private val mContext: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val list by lazy { ArrayList<Promotion>() }
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
        fun onBind(coupon: Promotion) {

            if (VenusApp.offerApplied == coupon.promo_id) {
                binding.tvApply.text = "Applied"
                binding.tvApply.isEnabled = false
//                binding.tvApply.isClickable = false
            } else {
                binding.tvApply.isEnabled = true
                binding.tvApply.text = "Apply"
            }
            binding.tvTitle.text = coupon.title
            binding.tvDesc.text = coupon.validity_text

            binding.tvApply.setOnSingleClickListener {
                VenusApp.offerApplied = coupon.promo_id
                notifyDataSetChanged()
                mContext.sendBroadcast(Intent("offer"))
                findNavController().popBackStack()
            }
        }
    }

    fun submitList(list: List<Promotion>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }
}


