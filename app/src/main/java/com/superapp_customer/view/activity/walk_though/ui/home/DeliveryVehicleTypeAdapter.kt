package com.superapp_customer.view.activity.walk_though.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.superapp_customer.R
import com.superapp_customer.customClasses.singleClick.setOnSingleClickListener
import com.superapp_customer.databinding.ItemDeliveryVehicleTypeBinding
import com.superapp_customer.model.dataClass.userData.UserDataDC
import com.superapp_customer.util.SharedPreferencesManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DeliveryVehicleTypeAdapter(
    val context: Context,
    var list: List<UserDataDC.Login.VehicleType>,
    var onClick: (region: UserDataDC.Login.VehicleType?) -> Unit
) : RecyclerView.Adapter<DeliveryVehicleTypeAdapter.DeliveryVehicleTypeHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryVehicleTypeHolder {
        val itemBinding =
            ItemDeliveryVehicleTypeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return DeliveryVehicleTypeHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    override fun onBindViewHolder(holder: DeliveryVehicleTypeHolder, position: Int) {
        list?.get(position)?.let {
            holder.bind(list?.get(position), onClick)
        }
    }

    inner class DeliveryVehicleTypeHolder(private val itemBinding: ItemDeliveryVehicleTypeBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        @SuppressLint("DefaultLocale", "SetTextI18n")
        fun bind(
            carTypeData: UserDataDC.Login.VehicleType?,
            onClick: (region: UserDataDC.Login.VehicleType?) -> Unit
        ) {

//            val drawable =
//                if (SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID) == 2) {
//                    ContextCompat.getDrawable(context, R.drawable.kg)
//                } else {
//                    ContextCompat.getDrawable(context, R.drawable.ic_person)
//                }
//
//            // Set the drawableStart programmatically
//            itemBinding.tvPersonCount.setCompoundDrawablesRelativeWithIntrinsicBounds(
//                drawable,
//                null,
//                null,
//                null
//            )
//            if (carTypeData?.eta == null) {
//                itemBinding.clBackground.alpha = 0.5f
//            } else {
//                itemBinding.clBackground.alpha = 1f
//            }
//            if ((carTypeData?.region_fare?.discount ?: 0.0) > 0.0) {
//                itemBinding.tvOriginalPrice.isVisible = true
//                itemBinding.viewCross.isVisible = true
//                itemBinding.tvOriginalPrice.text =
//                    "${carTypeData?.region_fare?.currency ?: "INR"} ${
//                        String.format(
//                            "%.2f",
//                            carTypeData?.region_fare?.original_fare ?: 0.0
//                        )
//                    }"
//                itemBinding.tvActualPrice.text =
//                    "${carTypeData?.region_fare?.currency ?: "INR"} ${
//                        String.format(
//                            "%.2f",
//                            carTypeData?.region_fare?.fare ?: 0.0
//                        )
//                    }"
//            } else {
//                itemBinding.tvOriginalPrice.isVisible = false
//                itemBinding.viewCross.isVisible = false
//                itemBinding.tvActualPrice.text =
//                    "${carTypeData?.region_fare?.currency ?: "INR"} ${
//                        String.format(
//                            "%.2f",
//                            carTypeData?.region_fare?.fare ?: 0.0
//                        )
//                    }"
//            }
            Glide.with(itemBinding.root).load(carTypeData?.vehicleImage.orEmpty())
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher).into(itemBinding.ivVehicleImage)
            itemBinding.tvVehicleName.text = carTypeData?.regionName.orEmpty()
//
//            itemBinding.tvPersonCount.text = "${carTypeData?.maxPeople ?: 0} ${
//                if (SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID) == 2) "Kg" else ""
//            }"
//
//            itemBinding.tvDesc.text = carTypeData?.vehicle_desc ?: ""
////            itemBinding.tvTime.text = carTypeData?.eta.orEmpty().ifEmpty { "0" }.plus(" min")
//            var dropOffTime = ""
//            if (customerETA.rideDistance != null) {
//                dropOffTime = setTime(carTypeData?.eta.orEmpty().ifEmpty { "0" }
//                    .toInt() + customerETA.rideTime!!.toInt())
//            } else
//                dropOffTime = ""
//
//            itemBinding.tvTime.text =
//                "$dropOffTime | ${carTypeData?.eta.orEmpty().ifEmpty { "0" }.plus(" min away")}"
//
////            itemBinding.tvActualPrice.text =
////                "${carTypeData?.vehicleAmount.formatString()} $currencyCode"
////            itemBinding.tvPersonCount.text = carTypeData?.maxPeople ?: "0"
//
////            itemBinding.noDriverFound.isVisible = carTypeData?.distance.isNullOrEmpty()
//
            if (carTypeData?.isSelected == true) {
                itemBinding.clBackground.strokeWidth =
                    4 // Set stroke width to highlight the selected card
                itemBinding.clBackground.strokeColor =
                    ContextCompat.getColor(context, R.color.theme)
            } else {
                itemBinding.clBackground.strokeWidth = 0 // Remove stroke for unselected cards
            }//
            itemBinding.root.setOnSingleClickListener {
                    onClick(carTypeData)
                    updateListData(carTypeData)
            }
        }
    }

    fun setTime(amount: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, amount)
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
    }


    fun updateListData(carTypeData: UserDataDC.Login.VehicleType?) {
        list?.map {
            it.isSelected = it == carTypeData
        }
        notifyDataSetChanged()
    }


}