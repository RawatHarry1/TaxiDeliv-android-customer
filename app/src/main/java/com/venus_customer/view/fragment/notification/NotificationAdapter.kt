package com.venus_customer.view.fragment.notification

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.salonedriver.model.dataclassses.notificationDC.NotificationDC
import com.venus_customer.R
import com.venus_customer.databinding.ItemFaqsBinding
import com.venus_customer.databinding.ItemNotificationsBinding
import com.venus_customer.util.gone
import com.venus_customer.util.visible
import kotlin.collections.ArrayList

class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private val itemList by lazy { mutableListOf<NotificationDC>() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val itemBinding =
            ItemNotificationsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    inner class NotificationViewHolder(private val itemBinding: ItemNotificationsBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(item: NotificationDC) {

            itemBinding.tvMessage.text = item.message.orEmpty()
            itemBinding.root.setOnClickListener {
            }
        }
    }


    fun submitList(list: List<NotificationDC>){
        itemList.clear()
        itemList.addAll(list)
        notifyDataSetChanged()
    }


    fun addMoreItems(list: List<NotificationDC>){
        itemList.addAll(list)
        notifyDataSetChanged()
    }
}


