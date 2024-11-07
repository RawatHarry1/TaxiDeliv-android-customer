package com.superapp_driver.view.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.superapp_driver.databinding.ItemDocumentsBinding

class UploadedDocsAdapter() : RecyclerView.Adapter<UploadedDocsAdapter.UploadedDocsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UploadedDocsViewHolder {
        val itemBinding =
            ItemDocumentsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UploadedDocsViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return 4
    }

    override fun onBindViewHolder(holder: UploadedDocsViewHolder, position: Int) {
        holder.bind()
    }

    inner class UploadedDocsViewHolder(private val itemBinding: ItemDocumentsBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind() {
            itemBinding.root.setOnClickListener {
            }
        }
    }

}
