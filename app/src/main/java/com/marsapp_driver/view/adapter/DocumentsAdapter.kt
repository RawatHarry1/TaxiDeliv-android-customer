package com.marsapp_driver.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.marsapp_driver.R
import com.marsapp_driver.databinding.ItemUploadDocumentBinding
import com.marsapp_driver.model.dataclassses.fetchRequiredDocument.FetchRequiredDocumentDC
import com.marsapp_driver.util.DocumentApprovalDetail

class DocumentsAdapter(val onClickListener: FetchRequiredDocumentDC. () -> Unit) :
    RecyclerView.Adapter<DocumentsAdapter.DocumentsHolder>() {

    private val documentList by lazy { ArrayList<FetchRequiredDocumentDC>() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentsHolder {
        val itemBinding =
            ItemUploadDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DocumentsHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return documentList.size
    }

    override fun onBindViewHolder(holder: DocumentsHolder, position: Int) {
        holder.bind(documentList[position])
    }

    inner class DocumentsHolder(private val itemBinding: ItemUploadDocumentBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(walkData: FetchRequiredDocumentDC) {

            itemBinding.tvReject.isVisible = walkData.docStatus.orEmpty() == DocumentApprovalDetail.REJECTED.type
            itemBinding.tvExpire.isVisible = walkData.docStatus.orEmpty() == DocumentApprovalDetail.EXPIRED.type

            itemBinding.tvTitleUploadLicense.text = walkData.docTypeText.orEmpty().plus(if (walkData.docRequirement == 1) "" else " (Optional)")
            itemBinding.btUploadVerify.text = if (walkData.docUrl.isNullOrEmpty()) {
                itemBinding.root.context.getString(
                    R.string.upload
                )
            } else if ((walkData.imagePosition?.size ?: 0) < walkData.docUrl.size) {
                itemBinding.root.context.getString(
                    R.string.upload
                )
            } else {
                if (itemBinding.tvReject.isVisible || itemBinding.tvExpire.isVisible){
                    itemBinding.root.context.getString(
                        R.string.upload
                    )
                }else if (walkData.docStatus.orEmpty() == DocumentApprovalDetail.APPROVED.type){
                    itemBinding.root.context.getString(
                        R.string.document_approved
                    )
                }else {
                    itemBinding.root.context.getString(
                        R.string.document_uploaded
                    )
                }
            }

            itemBinding.root.setOnClickListener {
                if ((itemBinding.btUploadVerify.text == itemBinding.root.context.getString(R.string.upload)) || itemBinding.tvReject.isVisible || itemBinding.tvExpire.isVisible){
                    onClickListener.invoke(walkData)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<FetchRequiredDocumentDC>) {
        documentList.clear()
        documentList.addAll(list)
        notifyDataSetChanged()
    }


    fun getItems() = documentList

}