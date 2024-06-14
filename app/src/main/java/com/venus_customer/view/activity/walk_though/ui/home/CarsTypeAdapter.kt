package com.venus_customer.view.activity.walk_though.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.venus_customer.R
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.ItemCarsTypeBinding
import com.venus_customer.model.dataClass.findDriver.FindDriverDC
import com.venus_customer.util.AppUtils.CurrencyCode
import com.venus_customer.util.formatString

class CarsTypeAdapter(var list: List<FindDriverDC.Region>?, var currencyCode: String, var onClick: (region: FindDriverDC.Region?) -> Unit) : RecyclerView.Adapter<CarsTypeAdapter.CarsTypeAdapter>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarsTypeAdapter {
        val itemBinding = ItemCarsTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarsTypeAdapter(itemBinding)
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    override fun onBindViewHolder(holder: CarsTypeAdapter, position: Int) {
        list?.get(position)?.let {
            holder.bind(list?.get(position), onClick)
        }
    }

    inner class CarsTypeAdapter(private val itemBinding: ItemCarsTypeBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(carTypeData: FindDriverDC.Region?, onClick: (region: FindDriverDC.Region?) -> Unit) {
            Glide.with(itemBinding.root).load(carTypeData?.images?.rideNowNormal2x.orEmpty()).error(R.mipmap.ic_launcher).into(itemBinding.ivCar)
            itemBinding.tvTitle.text = carTypeData?.regionName.orEmpty()
            itemBinding.tvPersonCount.text = "${carTypeData?.maxPeople ?: 0}"
            itemBinding.tvDesc.text = carTypeData?.disclaimerText.orEmpty()
            itemBinding.tvTime.text = carTypeData?.eta.orEmpty().ifEmpty { "0" }.plus(" min")
            itemBinding.tvActualPrice.text = "${carTypeData?.vehicleAmount.formatString()} $currencyCode"
//            itemBinding.noDriverFound.isVisible = carTypeData?.distance.isNullOrEmpty()

            itemBinding.clBackground.setBackgroundResource(if (carTypeData?.isSelected == true) R.drawable.bg_select_round_where_to else R.drawable.bg_rounded_where_to)

            itemBinding.root.setOnSingleClickListener {
                if (!itemBinding.noDriverFound.isVisible){
                    onClick(carTypeData)
                    updateListData(carTypeData)
                }
            }
        }
    }


    fun updateListData(carTypeData: FindDriverDC.Region?){
        list?.map {
            it.isSelected = it == carTypeData
        }
        notifyDataSetChanged()
    }

}