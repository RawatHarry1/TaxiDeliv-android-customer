package com.marsapp_driver.view.adapter


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.marsapp_driver.R
import com.marsapp_driver.databinding.ItemVehicleListingBinding
import com.marsapp_driver.model.dataclassses.VehicleListDC
import com.marsapp_driver.util.SharedPreferencesManager

class VehicleListAdapter() : RecyclerView.Adapter<VehicleListAdapter.VehicleViewHolder>() {

    private val list by lazy { ArrayList<VehicleListDC.VehicleArray>() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val itemBinding =
            ItemVehicleListingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VehicleViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return if (list.isNotEmpty()) 1 else 0
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        holder.bind(list[position])
    }

    inner class VehicleViewHolder(private val itemBinding: ItemVehicleListingBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(data: VehicleListDC.VehicleArray) {
            itemBinding.tvSeats.text =
                if (SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID) == 2) "Capacity" else "Seats"
            itemBinding.tvCarName.text = data.modelName.orEmpty()
            itemBinding.tvCarSeats.text = "${data.noOfSeats.orEmpty().ifEmpty { "0" }} ${
                if (SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID) == 2) "Kg" else "Seater"
            } "
            itemBinding.tvCarNo.text = data.vehicleNo.orEmpty()
            itemBinding.tvInsuranceDeatils.text = data.color.orEmpty()
            itemBinding.tvVehicleType.text = data.vehicleTypeName.orEmpty()
            Glide.with(itemBinding.ivCarImage).load(data.makeImage)
                .placeholder(R.drawable.ic_car_round).error(R.drawable.ic_car_round)
                .into(itemBinding.ivCarImage)
        }
    }


    fun submitList(list: List<VehicleListDC.VehicleArray>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

}
