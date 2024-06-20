package com.venus_customer.view.activity.walk_though.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.ItemSavedAddressBinding
import com.venus_customer.model.dataClass.addedAddresses.SavedAddresse

class AddedAddressAdapter( val context: Context,private val onClick: (SavedAddresse,Boolean) -> Unit) :
    RecyclerView.Adapter<AddedAddressAdapter.ViewHolder>() {

    private val list by lazy { ArrayList<SavedAddresse>() }
    private var deleteVisiblePosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding =
            ItemSavedAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.bind(data)

        holder.itemBinding.root.setOnSingleClickListener {
            onClick(data,false)
        }
    }

    class ViewHolder(val itemBinding: ItemSavedAddressBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(address: SavedAddresse) {
            itemBinding.tvAddressDesc.text = address.addr
            itemBinding.tvAddressType.text = address.type
        }
    }


    fun submitList(list: List<SavedAddresse>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }
    fun removeItem(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
        onClick(list[position],true)
    }
    fun setDeleteVisible(position: Int, visible: Boolean) {
        if (visible) {
            deleteVisiblePosition = position
            notifyItemChanged(position)
        } else {
            deleteVisiblePosition = null
            notifyItemChanged(position)
        }
    }
}