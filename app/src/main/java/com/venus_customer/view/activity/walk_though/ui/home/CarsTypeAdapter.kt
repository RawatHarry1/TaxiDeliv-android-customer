package com.venus_customer.view.activity.walk_though.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.venus_customer.R
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.ItemCarsTypeBinding
import com.venus_customer.model.dataClass.findDriver.FindDriverDC
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CarsTypeAdapter(
    var list: List<FindDriverDC.Region>?,
    var currencyCode: String,
    var customerETA: FindDriverDC.CustomerETA,
    var onClick: (region: FindDriverDC.Region?) -> Unit
) : RecyclerView.Adapter<CarsTypeAdapter.CarsTypeAdapter>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarsTypeAdapter {
        val itemBinding =
            ItemCarsTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class CarsTypeAdapter(private val itemBinding: ItemCarsTypeBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(
            carTypeData: FindDriverDC.Region?,
            onClick: (region: FindDriverDC.Region?) -> Unit
        ) {
            Glide.with(itemBinding.root).load(carTypeData?.images?.rideNowNormal2x.orEmpty())
                .error(R.mipmap.ic_launcher).into(itemBinding.ivCar)
            itemBinding.tvTitle.text = carTypeData?.regionName.orEmpty()
            itemBinding.tvPersonCount.text = "${carTypeData?.maxPeople ?: 0}"
            itemBinding.tvDesc.text = carTypeData?.vehicle_desc ?: ""
//            itemBinding.tvTime.text = carTypeData?.eta.orEmpty().ifEmpty { "0" }.plus(" min")
            var dropOffTime = ""
            if (customerETA.rideDistance != null) {
                dropOffTime = setTime(carTypeData?.eta.orEmpty().ifEmpty { "0" }
                    .toInt() + customerETA.rideTime!!.toInt())
            }

            itemBinding.tvTime.text =
                "$dropOffTime | ${carTypeData?.eta.orEmpty().ifEmpty { "0" }.plus(" min away")}"

//            itemBinding.tvActualPrice.text =
//                "${carTypeData?.vehicleAmount.formatString()} $currencyCode"

            itemBinding.tvActualPrice.text =
                "${carTypeData?.region_fare?.currency?: "INR"} ${
                    String.format(
                        "%.2f",
                        carTypeData?.region_fare?.fare?: 0.0
                    )
                }"
//            itemBinding.noDriverFound.isVisible = carTypeData?.distance.isNullOrEmpty()

            itemBinding.clBackground.setBackgroundResource(if (carTypeData?.isSelected == true) R.drawable.bg_select_round_where_to else R.drawable.bg_rounded_where_to)

            itemBinding.root.setOnSingleClickListener {
                if (!itemBinding.noDriverFound.isVisible) {
                    onClick(carTypeData)
                    updateListData(carTypeData)
                }
            }
        }
    }

    fun setTime(amount: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, amount)
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
    }


    fun updateListData(carTypeData: FindDriverDC.Region?) {
        list?.map {
            it.isSelected = it == carTypeData
        }
        notifyDataSetChanged()
    }

}