package com.salonedriver.view.ui.chat

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.salonedriver.R
import com.salonedriver.databinding.ActivityChatBinding
import com.salonedriver.view.base.BaseActivity

class ChatActivity : BaseActivity<ActivityChatBinding>() {
    override fun getLayoutId(): Int {
        return R.layout.activity_chat
    }

    lateinit var binding: ActivityChatBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()


        setChatAdapter()
    }

    private fun setChatAdapter() {
        val list: MutableList<ChatData> = mutableListOf()
        list.add(
            ChatData(
                getString(R.string.txt_hey_there_how_are_you),
                null,
                "1",
                "0",
                "4:20 PM",
                true
            )
        )
        list.add(
            ChatData(
                getString(R.string.txt_okay_give_me_some_tine),
                null,
                "0",
                "1",
                "4:20 PM",
                true
            )
        )
        list.add(ChatData(getString(R.string.txt_great_thank_you), null, "1", "0", "4:20 PM", true))
        list.add(
            ChatData(
                "",
                ContextCompat.getDrawable(this, R.drawable.ic_banner_image),
                "0",
                "1",
                "4:21 PM",
                true
            )
        )
        list.add(
            ChatData(
                "",
                ContextCompat.getDrawable(this, R.drawable.ic_banner_image),
                "1",
                "0",
                "4:22 PM",
                true
            )
        )
        binding.rvChat.adapter = ChatAdapter(list)
    }


    data class ChatData(
        var message: String = "",
        var image: Drawable?,
        var senderId: String = "",
        var receiverId: String = "",
        var time: String = "",
        var seen: Boolean = false,
    )
}