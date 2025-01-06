package com.mcabs_driver.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mcabs_driver.databinding.ItemDocImageBinding
import com.mcabs_driver.model.dataclassses.fetchRequiredDocument.ImagePosition
import java.lang.Exception

class UploadDocImageAdapter(private val adapterClick: AdapterClick) : RecyclerView.Adapter<UploadDocImageAdapter.ViewHolder>() {

    private val documentImageList by lazy { ArrayList<ImagePosition>() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemDocImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(documentImageList[position], position)
    }

    override fun getItemCount() = documentImageList.size

    inner class ViewHolder(private val binding: ItemDocImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(imagePosition: ImagePosition, position: Int) {
            binding.tvFront.text = imagePosition.name.orEmpty()

            Glide.with(binding.ivFrontView).load(imagePosition.imageUrl.orEmpty()).into(binding.ivFrontView)

            binding.root.setOnClickListener {
                adapterClick.onClick(position, imagePosition)
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<ImagePosition>) {
        documentImageList.clear()
        documentImageList.addAll(list)
        notifyDataSetChanged()
    }


    fun changeItem(item: ImagePosition, position: Int) {
        try {
            documentImageList[position] = item
            notifyItemChanged(position)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun interface AdapterClick{
        fun onClick(position: Int, dataClass: ImagePosition)
    }



    fun getItems() = documentImageList

}