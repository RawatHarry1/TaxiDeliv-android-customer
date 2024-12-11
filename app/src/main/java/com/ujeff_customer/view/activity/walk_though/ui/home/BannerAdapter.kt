package com.ujeff_customer.view.activity.walk_though.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ujeff_customer.databinding.ItemBannerBinding

class BannerAdapter(var listData: MutableList<HomeFragment.BannerData>) : RecyclerView.Adapter<BannerAdapter.BannerAdapter>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerAdapter {
        val itemBinding = ItemBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BannerAdapter(itemBinding)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun onBindViewHolder(holder: BannerAdapter, position: Int) {
        val bannerData: HomeFragment.BannerData = listData[position]
        holder.bind(bannerData)
    }

    class BannerAdapter(private val itemBinding: ItemBannerBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(bannerData: HomeFragment.BannerData) {
            itemBinding.ivBanner.setImageDrawable(bannerData.drawable)
        }
    }

}