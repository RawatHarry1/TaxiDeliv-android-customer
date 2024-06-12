package com.salonedriver.view.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.salonedriver.databinding.ItemRatingsBinding

class RatingsAdapter() : RecyclerView.Adapter<RatingsAdapter.RatingsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingsViewHolder {
        val itemBinding =
            ItemRatingsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RatingsViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return 4
    }

    override fun onBindViewHolder(holder: RatingsViewHolder, position: Int) {
        holder.bind()
    }

    inner class RatingsViewHolder(private val itemBinding: ItemRatingsBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind() {
            itemBinding.root.setOnClickListener {
            }
        }
    }

}
