package com.ujeff_driver.view.ui.home.cancelTrip

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ujeff_driver.databinding.CancelTripItemBinding

class CancelTripAdapter : RecyclerView.Adapter<CancelTripAdapter.ViewHolder>() {

    private val arrayList by lazy { ArrayList<CancelTripItem>() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(CancelTripItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(arrayList[position])
    }

    override fun getItemCount() = arrayList.size

    inner class ViewHolder(val binding: CancelTripItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: CancelTripItem){
            binding.cbItem.text = item.itemName.orEmpty()
            binding.cbItem.isChecked = item.isSelected

            binding.cbItem.setOnClickListener {
                arrayList.map {
                    if (it.isSelected && it == item){
                        it.isSelected = !it.isSelected
                    } else {
                        it.isSelected = it == item
                    }
                }
                notifyDataSetChanged()
            }
        }
    }


    fun submitList(list: List<String>){
        arrayList.clear()
        arrayList.addAll(list.map { CancelTripItem(itemName = it) })
        notifyDataSetChanged()
    }


    fun getSelectedItemName(): String? = arrayList.find { it.isSelected }?.itemName


    data class CancelTripItem(
        val itemName: String? = null,
        var isSelected: Boolean = false
    )

}