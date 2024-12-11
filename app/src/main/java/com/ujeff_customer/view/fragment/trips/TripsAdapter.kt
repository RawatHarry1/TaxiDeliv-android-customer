package com.ujeff_customer.view.fragment.trips

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ujeff_customer.customClasses.singleClick.setOnSingleClickListener
import com.ujeff_customer.databinding.ItemTripsBinding
import com.ujeff_customer.model.dataClass.tripsDC.TripListDC
import com.ujeff_customer.util.getTime

class TripsAdapter(val onClickAdapterLambda: (data: TripListDC) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list by lazy { ArrayList<TripListDC>() }

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

    inner class TripsViewHolder(private val binding: ItemTripsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(data: TripListDC) {
            binding.tvRideStatus.text = data.autosStatusText.orEmpty()
            binding.tvRideDate.text = data.date.getTime(output = "dd MMMM yyyy, HH:mm", applyTimeZone = true)
            binding.tvStartAdress.text = data.pickupAddress.orEmpty()
            binding.tvEndAddress.text = data.dropAddress.orEmpty()

            binding.tvStartTime.text = data.pickupTime.getTime(output = "HH:mm", applyTimeZone = true)
            binding.tvEndTime.text = data.dropTime.getTime(output = "HH:mm", applyTimeZone = true)

            binding.root.setOnSingleClickListener {
                onClickAdapterLambda(data)
            }

            try {
                binding.tvRideStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor(data.autosStatusColor))
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }


    fun submitList(list: List<TripListDC>){
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

}


