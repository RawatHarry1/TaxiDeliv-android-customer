package com.venus_customer.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.venus_customer.R
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.ItemFaqsBinding
import com.venus_customer.model.dataClass.FaqX
import com.venus_customer.util.gone
import com.venus_customer.util.visible
import kotlin.collections.ArrayList

class FAQAdapter(val context: Context,private val faqArrayList: ArrayList<FaqX>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return GroupViewHolder(
            ItemFaqsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return faqArrayList.size

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as GroupViewHolder).onBind(faqArrayList[position])
    }

    fun refreshData() {

        notifyDataSetChanged()
    }

    inner class GroupViewHolder(private val binding: ItemFaqsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(faq: FaqX) {
            binding.tvQuestion.text = faq.ques
            binding.tvAnswer.text = faq.ans
            binding.clQuestion.setOnSingleClickListener {
                if (faq.isShown == true) {
                    binding.ivArrow.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.arrow_up
                        )
                    )
                    binding.tvAnswer.visible()
                    faq.isShown = false
                } else {
                    binding.ivArrow.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.arrow_down
                        )
                    )
                    binding.tvAnswer.gone()
                    faq.isShown = true
                }
            }

            binding.tvQuestion.setOnSingleClickListener {
                if (faq.isShown == true) {
                    binding.ivArrow.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.arrow_up
                        )
                    )
                    binding.tvAnswer.visible()
                    faq.isShown = false
                } else {
                    binding.ivArrow.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.arrow_down
                        )
                    )
                    binding.tvAnswer.gone()
                    faq.isShown = true
                }
            }
        }
    }

}


