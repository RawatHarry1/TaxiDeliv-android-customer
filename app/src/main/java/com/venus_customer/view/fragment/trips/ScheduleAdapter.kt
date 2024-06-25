package com.venus_customer.view.fragment.trips

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.venus_customer.R
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.ItemTripsBinding
import com.venus_customer.model.dataClass.ScheduleList
import com.venus_customer.util.getTime

class ScheduleAdapter(
    private val context: Context,
    val onClickAdapterLambda: (data: ScheduleList) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list by lazy { ArrayList<ScheduleList>() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TripsViewHolder(
            ItemTripsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as TripsViewHolder).onBind(list[position])
    }

    inner class TripsViewHolder(private val binding: ItemTripsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(data: ScheduleList) {
            if (data.status == 3) {
                binding.tvCancelSchedule.isEnabled = false
                binding.tvCancelSchedule.isClickable = false
                binding.tvCancelSchedule.alpha = .5f
            }
            binding.tvCancelSchedule.isVisible = true
            binding.tvRideStatus.text = when (data.status) {
                0 -> "In Queue"
                1 -> "In Progress"
                2 -> "Ride Completed"
                3 -> "Ride Cancelled"
                else -> ""
            }
            binding.tvRideDate.text =
                data.pickup_time.getTime(output = "dd MMMM yyyy, HH:mm", applyTimeZone = true)
            binding.tvStartAdress.text = data.pickup_location_address.orEmpty()
            binding.tvEndAddress.text = data.drop_location_address.orEmpty()
//
//            binding.tvStartTime.text = data.pickupTime.getTime(output = "HH:mm", applyTimeZone = true)
//            binding.tvEndTime.text = data.dropTime.getTime(output = "HH:mm", applyTimeZone = true)

            binding.tvCancelSchedule.setOnSingleClickListener {
                onClickAdapterLambda(data)
//                data.status = 3
//                notifyItemChanged(absoluteAdapterPosition)
            }

            try {
                val color = when (data.status) {
                    0 -> R.color.theme
                    1 -> R.color.in_progress
                    2 -> R.color.green_text_color
                    3 -> R.color.cancel_btn_red_color
                    else -> R.color.theme
                }
                binding.tvRideStatus.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(context, color))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun submitList(list: List<ScheduleList>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

}