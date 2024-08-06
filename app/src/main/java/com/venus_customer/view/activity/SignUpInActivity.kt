package com.venus_customer.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.venus_customer.R
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.ActivitySignUpInBinding
import com.venus_customer.model.dataClass.base.ClientConfig
import com.venus_customer.util.SharedPreferencesManager
import com.venus_customer.view.activity.sign_in.SignIn

class SignUpInActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySignUpInBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up_in)
        SharedPreferencesManager.getModel<ClientConfig>(SharedPreferencesManager.Keys.CLIENT_CONFIG)
            ?.let {
                binding.tvWalkTitle.text = it.walkThroughTitle ?: "Venus"
                binding.tvWalkDesc.text = it.walkThroughDesc ?: "Clean Air Ride share"
            }
        binding.tvSignIn.setOnSingleClickListener {
            startActivity(Intent(this@SignUpInActivity, SignIn::class.java))
        }
        binding.tvHaveAnAccount.setOnSingleClickListener {
            startActivity(Intent(this@SignUpInActivity, SignIn::class.java).apply {
                putExtra("type", "sign_up")
            })
        }

    }
}