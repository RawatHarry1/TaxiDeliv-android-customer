package com.marsapp_driver.view.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.marsapp_driver.databinding.ItemRejectPackageReasonBinding

class RejectionReasonAdapter : RecyclerView.Adapter<RejectionReasonAdapter.ViewHolder>() {

    private val arrayList by lazy { ArrayList<CancelTripItem>() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemRejectPackageReasonBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(arrayList[position])
    }

    override fun getItemCount() = arrayList.size

    inner class ViewHolder(val binding: ItemRejectPackageReasonBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CancelTripItem) {
            binding.cbItem.text = item.itemName.orEmpty()
            binding.cbItem.isChecked = item.isSelected

            binding.cbItem.setOnClickListener {
                arrayList.map {
                    if (it.isSelected && it == item) {
                        it.isSelected = !it.isSelected
                    } else {
                        it.isSelected = it == item
                    }
                }
                notifyDataSetChanged()
            }
        }
    }


    fun submitList(list: List<String>) {
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