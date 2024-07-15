package com.venus_customer.view.activity.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.venus_customer.databinding.ItemChatBinding
import com.venus_customer.model.dataClass.MessageData
import com.venus_customer.model.dataClass.userData.UserDataDC
import com.venus_customer.util.SharedPreferencesManager
import com.venus_customer.util.gone
import com.venus_customer.util.visible

class ChatAdapter(var list: ArrayList<MessageData>) :
    RecyclerView.Adapter<ChatAdapter.ChatAdapter>() {
    var userId = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatAdapter {
        val itemBinding =
            ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatAdapter(itemBinding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: ChatAdapter, position: Int) {
        SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
            ?.let {
                userId = it.login?.userId ?: 0
            }
        val chatData = list[position]
        holder.bind(chatData)
    }

    inner class ChatAdapter(private val itemBinding: ItemChatBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(chatData: MessageData) {

            if (chatData.sender_id == userId) {
                itemBinding.tvSenderTime.visible()
                itemBinding.tvSender.visible()
                itemBinding.tvReceiver.gone()
                itemBinding.tvReceiverTime.gone()
                itemBinding.tvSender.text = chatData.message
                itemBinding.tvSenderTime.text = chatData.created_at
            } else {
                itemBinding.tvSenderTime.gone()
                itemBinding.tvSender.gone()
                itemBinding.tvReceiver.visible()
                itemBinding.tvReceiverTime.visible()
                itemBinding.tvReceiver.text = chatData.message
                itemBinding.tvReceiverTime.text = chatData.created_at
            }

//            if (chatData.senderId == "0") {
//                if (chatData.image != null) {
//                    itemBinding.tvReceiver.visibility = View.GONE
//                    itemBinding.ivReceiverImage.visibility = View.VISIBLE
//                    itemBinding.ivReceiverImage.setImageDrawable(chatData.image)
//                } else {
//                    itemBinding.tvReceiver.visibility = View.VISIBLE
//                    itemBinding.ivReceiverImage.visibility = View.GONE
//                    itemBinding.tvReceiver.text = chatData.message
//                }
//                itemBinding.tvReceiverTime.text = chatData.time
//                itemBinding.tvReceiverTime.visibility = View.VISIBLE
//                itemBinding.tvSender.visibility = View.GONE
//                itemBinding.tvSenderTime.visibility = View.GONE
//            } else {
//                if (chatData.image != null) {
//                    itemBinding.tvSender.visibility = View.GONE
//                    itemBinding.ivSenderImage.visibility = View.VISIBLE
//                    itemBinding.ivSenderImage.setImageDrawable(chatData.image)
//                } else {
//                    itemBinding.tvSender.visibility = View.VISIBLE
//                    itemBinding.ivSenderImage.visibility = View.GONE
//                    itemBinding.tvSender.text = chatData.message
//                }
//                itemBinding.tvSender.text = chatData.message
//                itemBinding.tvSenderTime.text = chatData.time
//                itemBinding.tvReceiver.visibility = View.GONE
//                itemBinding.tvReceiverTime.visibility = View.GONE
//                itemBinding.tvSenderTime.visibility = View.VISIBLE
//            }
        }
    }

}