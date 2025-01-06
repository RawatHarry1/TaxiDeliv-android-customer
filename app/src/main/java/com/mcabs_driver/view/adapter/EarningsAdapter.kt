package com.mcabs_driver.view.adapter


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mcabs_driver.R
import com.mcabs_driver.databinding.ItemEarningsBinding
import com.mcabs_driver.model.dataclassses.earningDC.EarningDC
import com.mcabs_driver.util.SharedPreferencesManager
import com.mcabs_driver.util.formatAmount

class EarningsAdapter : RecyclerView.Adapter<EarningsAdapter.EarningsHolder>() {

    private val earningList by lazy { mutableListOf<EarningDC.Ride>() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EarningsHolder {
        val itemBinding =
            ItemEarningsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EarningsHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return earningList.size
    }

    override fun onBindViewHolder(holder: EarningsHolder, position: Int) {
        holder.bind(earningList[position])
    }

    class EarningsHolder(private val itemBinding: ItemEarningsBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(data: EarningDC.Ride) {
            itemBinding.tvCustomerName.text = data.customerName.orEmpty().ifEmpty { "N/A" }
            itemBinding.tvRideNo.text = "#${data.engagementId.orEmpty().ifEmpty { "N/A" }}"
            itemBinding.tvAmount.text = "${SharedPreferencesManager.getCurrencySymbol()} ${data.totalEarnings.orEmpty().formatAmount()}"
            Glide.with(itemBinding.ivCustomerImage).load(data.customerImage.orEmpty())
                .error(R.drawable.ic_profile_user).into(itemBinding.ivCustomerImage)
        }
    }


    fun submitList(list: List<EarningDC.Ride>){
        earningList.clear()
        earningList.addAll(list)
        notifyDataSetChanged()
    }

}
