package com.venus_customer.view.activity.walk_though

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.venus_customer.BuildConfig
import com.venus_customer.databinding.ItemSalonWalkthroughBinding
import com.venus_customer.databinding.ItemWalkthroughBinding
import com.venus_customer.util.constants.AppConstants

class WalkAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var listData: ArrayList<WalkThrough.WalkData> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            1 -> {
                val itemBinding =
                    ItemSalonWalkthroughBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SalonWalkthrough(itemBinding)
            }

            else -> {
                val itemBinding =
                    ItemWalkthroughBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                WalkThroughHolder(itemBinding)
            }
        }
    }

    override fun getItemCount(): Int {
        return listData.size
    }


    override fun getItemViewType(position: Int): Int {
        return if (BuildConfig.APPLICATION_ID == AppConstants.SALON_PACKAGE_NAME) 1 else 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val walkData: WalkThrough.WalkData = listData[position]
        if (holder is WalkThroughHolder){
            holder.bind(walkData)
        } else if(holder is SalonWalkthrough){
            holder.bind(walkData)
        }
    }

    class WalkThroughHolder(private val itemBinding: ItemWalkthroughBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(walkData: WalkThrough.WalkData) {
            itemBinding.tvWalkDesc.text = walkData.subTitle
            itemBinding.tvWalkTitle.text = walkData.title
            itemBinding.ivWalkImage.setImageDrawable(walkData.drawable)
        }
    }


    class SalonWalkthrough(private val binding: ItemSalonWalkthroughBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(walkData: WalkThrough.WalkData) {
            binding.ivImage.setImageDrawable(walkData.drawable)
        }
    }


    fun submitList(list: List<WalkThrough.WalkData>){
        listData.clear()
        listData.addAll(list)
        notifyDataSetChanged()
    }

}
