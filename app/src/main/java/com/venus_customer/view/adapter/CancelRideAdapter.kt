package com.venus_customer.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.CancelReasonItemBinding

class CancelRideAdapter(private val list: List<String>) : RecyclerView.Adapter<CancelRideAdapter.ViewHolder>() {

    companion object {
        var selectedText: String? = null
    }

    init {
        selectedText = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(CancelReasonItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }


    inner class ViewHolder(private val binding: CancelReasonItemBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(string: String){
            binding.tvText.text = string
            binding.cbDriverLate.isChecked = string == selectedText

            binding.cbDriverLate.setOnSingleClickListener {
                selectedText = if (string == selectedText) "" else string
                notifyItemRangeChanged(0, list.size)
            }
        }
    }

}