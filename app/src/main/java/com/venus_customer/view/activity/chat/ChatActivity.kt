package com.venus_customer.view.activity.chat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.venus_customer.R
import com.venus_customer.VenusApp
import com.venus_customer.databinding.ActivityChatBinding
import com.venus_customer.databinding.ItemAutoMessagesBinding
import com.venus_customer.firebaseSetup.NotificationInterface
import com.venus_customer.model.dataClass.MessageData
import com.venus_customer.socketSetup.SocketInterface
import com.venus_customer.socketSetup.SocketSetup
import com.venus_customer.util.AppUtils
import com.venus_customer.util.AppUtils.isInternetAvailable
import com.venus_customer.util.showSnackBar
import com.venus_customer.view.activity.walk_though.Home
import com.venus_customer.view.base.BaseActivity

class ChatActivity : BaseActivity<ActivityChatBinding>(), SocketInterface, NotificationInterface {
    override fun getLayoutId(): Int {
        return R.layout.activity_chat
    }

    companion object {
        var notificationInterface: NotificationInterface? = null
    }

    override fun rideEnd(
        tripId: String,
        driverId: String,
        driverName: String,
        engagementId: String
    ) {
        super.rideEnd(tripId, driverId, driverName, engagementId)
        runOnUiThread {
            try {
                val resultIntent = Intent()
                resultIntent.apply {
                    putExtra("tripId", tripId)
                    putExtra("driverId", driverId)
                    putExtra("driverName", driverName)
                    putExtra("engagementId", engagementId)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } catch (e: Exception) {
            }

        }
    }

    override fun rideRejectedByDriver() {
        super.rideRejectedByDriver()
        runOnUiThread {
            try {
                Log.i("PUSHNOTI", "IN RIDE REJCT CHAT")
                val resultIntent = Intent()
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } catch (e: Exception) {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        notificationInterface = this
    }

    lateinit var binding: ActivityChatBinding
    private var driverId = ""
    private var customerId = ""
    private var tripId = ""
    private var driverName = ""
    private var driverImage = ""
    private var chatArrayList = ArrayList<MessageData>()
    private var automaticMessagesArrayList = ArrayList<String>()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        VenusApp.onChatScreen = true
        Home.isFromMsgNotification = false
        automaticMessagesArrayList.add("Hello?")
        automaticMessagesArrayList.add("Where are you?")
        automaticMessagesArrayList.add("Have you reached at pickup?")
        automaticMessagesArrayList.add("I am at pickup point.")
        SocketSetup.initializeInterface(this)
        SocketSetup.connectSocket()
        customerId = intent.getStringExtra("customerId") ?: ""
        driverId = intent.getStringExtra("driverId") ?: ""
        tripId = intent.getStringExtra("engagementId") ?: ""
        driverName = intent.getStringExtra("driverName") ?: ""
        driverImage = intent.getStringExtra("driverImage") ?: ""
        SocketSetup.listenToMessage(tripId, customerId)
        SocketSetup.getAllMsg(customerId, tripId)
        setChatAdapter()
        binding.tvName.text = driverName
        Glide.with(this).load(driverImage)
            .error(R.drawable.circleimage).into(binding.ivProfileImage)
        binding.tvSend.setOnClickListener {
            if (binding.etMessage.text.toString().trim().isEmpty())
                showSnackBar("Please enter your message.")
            else {
                if (isInternetAvailable(this)) {
                    SocketSetup.startMsgEmit(
                        binding.etMessage.text.toString().trim(),
                        senderId = customerId,
                        receiverId = driverId,
                        engagementId = tripId,
                        type = "text"
                    )
                    chatArrayList.add(
                        MessageData(
                            message = binding.etMessage.text.toString().trim(),
                            sender_id = customerId.toInt(),
                            receiver_id = driverId.toInt(),
                            attachment_type = "text",
                            engagement_id = tripId.toInt(),
                            created_at = AppUtils.convertUtcToLocal(AppUtils.currentUtcTimeAsString())
                        )
                    )
                    binding.etMessage.setText("")
                    chatAdapter.notifyDataSetChanged()
                    binding.rvChat.smoothScrollToPosition(chatArrayList.size - 1)
                }
                else{
                    showSnackBar("No internet connection")
                }
            }
        }
        binding.ivBack.setOnClickListener { finish() }
    }

    override fun onDestroy() {
        super.onDestroy()
        SocketSetup.listenerOffOnMessage(tripId, customerId)
        VenusApp.onChatScreen = false
    }

    override fun driverMessage(message: MessageData) {
        super.driverMessage(message)
        Log.e("SocketPrint", "Socket listen on Activity ${Gson().toJson(message)}")
        chatArrayList.add(message)
        chatAdapter.notifyDataSetChanged()
        binding.rvChat.smoothScrollToPosition(chatArrayList.size - 1)
    }

    override fun allMessages(messages: ArrayList<MessageData>) {
        super.allMessages(messages)
        messages.reverse()
        chatArrayList.addAll(messages)
        chatAdapter.notifyDataSetChanged()
        binding.rvChat.smoothScrollToPosition(chatArrayList.size - 1)
    }

    private fun setChatAdapter() {
        chatAdapter = ChatAdapter(chatArrayList)
        binding.rvChat.adapter = chatAdapter
        binding.rvAutoMessages.adapter = AutoMaticMessages()
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
                    if (isInternetAvailable(this@ChatActivity)) {
                        SocketSetup.startMsgEmit(
                            string,
                            senderId = customerId,
                            receiverId = driverId,
                            engagementId = tripId,
                            type = "text"
                        )
                        chatArrayList.add(
                            MessageData(
                                message = string,
                                sender_id = customerId.toInt(),
                                receiver_id = driverId.toInt(),
                                attachment_type = "text",
                                engagement_id = tripId.toInt(),
                                created_at = AppUtils.convertUtcToLocal(AppUtils.currentUtcTimeAsString())
                            )
                        )
                        chatAdapter.notifyDataSetChanged()
                        binding.rvChat.smoothScrollToPosition(chatArrayList.size - 1)
                    }
                    else{
                        showSnackBar("No internet connection")
                    }
                }
            }
        }
    }

}

