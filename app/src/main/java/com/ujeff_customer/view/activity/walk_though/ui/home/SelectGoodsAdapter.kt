package com.ujeff_customer.view.activity.walk_though.ui.home

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class SelectGoodsAdapter(
    private val goodsTypes: List<String>,
    private val onItemClick: (String) -> Unit
) :
    RecyclerView.Adapter<SelectGoodsAdapter.GoodsTypeViewHolder>() {
    private var selectedPosition = RecyclerView.NO_POSITION
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoodsTypeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return GoodsTypeViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoodsTypeViewHolder, position: Int) {
        val item = goodsTypes[position]
        holder.bind(item, position == HomeFragment.selectedGoodType, onItemClick) {
            // Update selected position and refresh items
            val previousPosition = HomeFragment.selectedGoodType
            HomeFragment.selectedGoodType = holder.absoluteAdapterPosition
            notifyItemChanged(previousPosition)
            notifyItemChanged(HomeFragment.selectedGoodType)
        }
    }

    override fun getItemCount(): Int = goodsTypes.size

    class GoodsTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(
            item: String,
            isSelected: Boolean,
            onItemClick: (String) -> Unit,
            onSelected: () -> Unit
        ) {
            textView.text = item
            textView.setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)

            itemView.setOnClickListener {
                onItemClick(item)
                onSelected()
            }
        }
    }
}
