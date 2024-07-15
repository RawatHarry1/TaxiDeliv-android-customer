package com.salonedriver.view.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.salonedriver.databinding.ItemChatBinding
import com.salonedriver.model.dataclassses.MessageData
import com.salonedriver.model.dataclassses.userData.UserDataDC
import com.salonedriver.util.SharedPreferencesManager
import com.salonedriver.util.gone
import com.salonedriver.util.visible

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
                userId = it.login?.userId?.toIntOrNull() ?: 0
            }
        holder.bind(list[position])
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
        }
    }
}