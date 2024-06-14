package com.venus_customer.view.activity.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.venus_customer.databinding.ItemChatBinding

class ChatAdapter(var list: MutableList<ChatActivity.ChatData>) : RecyclerView.Adapter<ChatAdapter.ChatAdapter>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatAdapter {
        val itemBinding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatAdapter(itemBinding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: ChatAdapter, position: Int) {
        val chatData: ChatActivity.ChatData = list[position]
        holder.bind(chatData)
    }

    class ChatAdapter(private val itemBinding: ItemChatBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(chatData: ChatActivity.ChatData) {

            if(chatData.senderId=="0") {
                if(chatData.image!=null)
                {
                    itemBinding.tvReceiver.visibility = View.GONE
                    itemBinding.ivReceiverImage.visibility = View.VISIBLE
                    itemBinding.ivReceiverImage.setImageDrawable(chatData.image)
                }
                else
                {
                    itemBinding.tvReceiver.visibility = View.VISIBLE
                    itemBinding.ivReceiverImage.visibility = View.GONE
                    itemBinding.tvReceiver.text = chatData.message
                }
                itemBinding.tvReceiverTime.text = chatData.time
                itemBinding.tvReceiverTime.visibility = View.VISIBLE
                itemBinding.tvSender.visibility = View.GONE
                itemBinding.tvSenderTime.visibility = View.GONE
            }
            else
            {
                if(chatData.image!=null)
                {
                    itemBinding.tvSender.visibility = View.GONE
                    itemBinding.ivSenderImage.visibility = View.VISIBLE
                    itemBinding.ivSenderImage.setImageDrawable(chatData.image)
                }
                else
                {
                    itemBinding.tvSender.visibility = View.VISIBLE
                    itemBinding.ivSenderImage.visibility = View.GONE
                    itemBinding.tvSender.text = chatData.message
                }
                itemBinding.tvSender.text = chatData.message
                itemBinding.tvSenderTime.text = chatData.time
                itemBinding.tvReceiver.visibility = View.GONE
                itemBinding.tvReceiverTime.visibility = View.GONE
                itemBinding.tvSenderTime.visibility = View.VISIBLE
            }
        }
    }

}