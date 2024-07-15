package com.salonedriver.view.ui.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.salonedriver.R
import com.salonedriver.databinding.ActivityChatBinding
import com.salonedriver.databinding.ItemAutoMessagesBinding
import com.salonedriver.model.dataclassses.MessageData
import com.salonedriver.socketSetup.SocketInterface
import com.salonedriver.socketSetup.SocketSetup
import com.salonedriver.util.AppUtils
import com.salonedriver.view.base.BaseActivity

class ChatActivity : BaseActivity<ActivityChatBinding>(), SocketInterface {
    override fun getLayoutId(): Int {
        return R.layout.activity_chat
    }

    lateinit var binding: ActivityChatBinding
    private var driverId = ""
    private var customerId = ""
    private var tripId = ""
    private var customerName = ""
    private var customerImage = ""
    private var chatArrayList = ArrayList<MessageData>()
    private lateinit var chatAdapter: ChatAdapter
    private var automaticMessagesArrayList = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        automaticMessagesArrayList.add("Hello?")
        automaticMessagesArrayList.add("Where are you?")
        automaticMessagesArrayList.add("Reached at pickup?")
        automaticMessagesArrayList.add("Waiting at pickup.")
        SocketSetup.initializeInterface(this)
        SocketSetup.connectSocket()
        customerId = intent.getStringExtra("customerId") ?: ""
        driverId = intent.getStringExtra("driverId") ?: ""
        tripId = intent.getStringExtra("engagementId") ?: ""
        customerName = intent.getStringExtra("customerName") ?: ""
        customerImage = intent.getStringExtra("customerImage") ?: ""

        Log.i("ChatData", "$customerId  $driverId $tripId ")
        SocketSetup.listenToMessage(tripId, driverId)
        SocketSetup.getAllMsg(driverId, tripId)
        setChatAdapter()
        binding.tvName.text = customerName
        Glide.with(this).load(customerImage)
            .error(R.drawable.circleimage).into(binding.ivProfileImage)
        binding.tvSend.setOnClickListener {
            if (binding.etMessage.text.toString().trim().isEmpty())
                showErrorMessage("Please enter your message.")
            else {
                SocketSetup.startMsgEmit(
                    binding.etMessage.text.toString().trim(),
                    senderId = driverId,
                    receiverId = customerId,
                    engagementId = tripId,
                    type = "text"
                )
                chatArrayList.add(
                    MessageData(
                        message = binding.etMessage.text.toString().trim(),
                        sender_id = driverId.toInt(),
                        receiver_id = customerId.toInt(),
                        attachment_type = "text",
                        engagement_id = tripId.toInt(),
                        created_at = AppUtils.convertUtcToLocal(AppUtils.currentUtcTimeAsString())
                    )
                )
                binding.etMessage.setText("")
                chatAdapter.notifyDataSetChanged()
                binding.rvChat.smoothScrollToPosition(chatArrayList.size - 1)
            }
        }

        binding.ivBack.setOnClickListener { finish() }

    }

    override fun onDestroy() {
        super.onDestroy()
        SocketSetup.listenerOffOnMessage(tripId, driverId)
    }

    private fun setChatAdapter() {
        chatAdapter = ChatAdapter(chatArrayList)
        binding.rvChat.adapter = chatAdapter
        binding.rvAutoMessages.adapter = AutoMaticMessages()
    }

    override fun onCustomerMsg(msg: MessageData) {
        Log.e("SocketPrint", "Socket listen on Activity ${Gson().toJson(msg)}")
        chatArrayList.add(msg)
        chatAdapter.notifyDataSetChanged()
        binding.rvChat.smoothScrollToPosition(chatArrayList.size - 1)
    }

    override fun allMsg(msgs: ArrayList<MessageData>) {
        super.allMsg(msgs)
        msgs.reverse()
        chatArrayList.addAll(msgs)
        chatAdapter.notifyDataSetChanged()
        binding.rvChat.smoothScrollToPosition(chatArrayList.size - 1)
    }


    inner class AutoMaticMessages() :
        RecyclerView.Adapter<AutoMaticMessages.AutoAdapter>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): AutoMaticMessages.AutoAdapter {
            val itemBinding =
                ItemAutoMessagesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return AutoAdapter(itemBinding)
        }

        override fun onBindViewHolder(holder: AutoMaticMessages.AutoAdapter, position: Int) {
            holder.bind(automaticMessagesArrayList[position])
        }

        override fun getItemCount(): Int {
            return automaticMessagesArrayList.size
        }

        inner class AutoAdapter(private val itemBinding: ItemAutoMessagesBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            fun bind(string: String) {
                itemBinding.tvMsgName.text = string
                itemBinding.root.setOnClickListener {
                    SocketSetup.startMsgEmit(
                        string,
                        senderId = driverId,
                        receiverId = customerId,
                        engagementId = tripId,
                        type = "text"
                    )
                    chatArrayList.add(
                        MessageData(
                            message = string,
                            sender_id = driverId.toInt(),
                            receiver_id = customerId.toInt(),
                            attachment_type = "text",
                            engagement_id = tripId.toInt(),
                            created_at = AppUtils.convertUtcToLocal(AppUtils.currentUtcTimeAsString())
                        )
                    )
                    chatAdapter.notifyDataSetChanged()
                    binding.rvChat.smoothScrollToPosition(chatArrayList.size - 1)
                }
            }
        }
    }
}