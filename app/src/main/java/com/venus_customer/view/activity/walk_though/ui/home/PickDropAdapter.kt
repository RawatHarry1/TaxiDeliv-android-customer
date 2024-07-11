package com.venus_customer.view.activity.walk_though.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.ItemAddressListBinding
import com.venus_customer.viewmodel.rideVM.CreateRideData

class PickDropAdapter( private val onClick: CreateRideData.LocationData.() -> Unit) : RecyclerView.Adapter<PickDropAdapter.ViewHolder>() {

    private val list by lazy { ArrayList<CreateRideData.LocationData>() }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = ItemAddressListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.bind(data)

        holder.itemBinding.root.setOnSingleClickListener {
            onClick(data)
        }
    }

    class ViewHolder(val itemBinding: ItemAddressListBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(pickDropData: CreateRideData.LocationData) {
            itemBinding.tvAddressTitle.text = pickDropData.address
        }
    }


    fun submitList(list: List<CreateRideData.LocationData>){
        this.list.clear()
        this.list.addAll(list.take(5))
        notifyDataSetChanged()
    }

}