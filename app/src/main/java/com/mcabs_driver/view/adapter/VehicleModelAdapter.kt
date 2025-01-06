package com.mcabs_driver.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.mcabs_driver.databinding.ItemVehicleModelBinding

class VehicleModelAdapter(context: Context, private val list: List<Pair<String, String>>) : BaseAdapter() {

    private val inflater by lazy { LayoutInflater.from(context) }

    override fun getCount() = list.size

    override fun getItem(position: Int) = list[position]

    override fun getItemId(p0: Int) = 0L

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val binding = ItemVehicleModelBinding.inflate(inflater, p2, false)
        binding.tvHeading.text = getItem(p0).second.orEmpty()
        binding.tvSubHeading.text = getItem(p0).first.orEmpty()
        return binding.root
    }

}