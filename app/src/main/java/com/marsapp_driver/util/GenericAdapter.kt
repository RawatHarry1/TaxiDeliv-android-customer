package com.marsapp_driver.util

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.AnimRes
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView


abstract class GenericAdapter<M : Any>(
    @LayoutRes private val layoutId: Int,
    private val limit: Int? = null
) :
    ListAdapter<M, GenericAdapter.ViewHolder>(GenericDiffUtil<M>()) {
    private var items = arrayListOf<M>()
    private var rvAttached: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        rvAttached = recyclerView
    }

    fun updateItem(index: Int, item: M?) {
        item ?: return
        items[index] = item
//        submitList(items)
        notifyItemChanged(index, item)
    }

    fun deleteItemAt(index: Int): M {
        val item = items.removeAt(index)
        notifyItemRemoved(index)
        return item
    }

    fun deleteAll() {
        val itemCount = this.itemCount
        items.clear()
        submitList(items)
    }

    fun insertItemAt(item: M, position: Int = itemCount) {
        items.add(position, item)
        submitList(items)
    }

    override fun submitList(list: List<M>?) {
        items.clear()
        items.addAll(list ?: emptyList())
        super.submitList(items)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (limit != null) {
            if (limit < items.size) limit else items.size
        } else items.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshAdapter() {
        notifyDataSetChanged()
    }

//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        try {
//            holder.view.setAnimation()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)


//    abstract fun onBindHolder(holder: T, dataClass: M)

    /** Animation Function */
    fun View.setAnimation(@AnimRes anim: Int) {
        val animation: Animation = AnimationUtils.loadAnimation(this.context, anim)
        this.startAnimation(animation)
    }

    fun moveItem(initialPosition: Int, finalPosition: Int) = kotlin.runCatching {
        val temp = items.removeAt(initialPosition)
        insertItemAt(temp, finalPosition)
        submitList(items)
    }

    fun getAllItems(): ArrayList<M> {
        return items
    }

    override fun getItem(position: Int): M {
        return super.getItem(position)!!
    }

    fun getsingleItem(position: Int): M {
        return items[position]
    }

    fun insertItemAtTop(item: M) {
        items.add(0, item)
        submitList(items)
        notifyItemInserted(0)
    }
}