package com.venus_customer.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.databinding.DataBindingUtil
import com.venus_customer.R
import com.venus_customer.databinding.ActivityWelcomeBinding
import com.venus_customer.view.activity.walk_though.Home

class Welcome : AppCompatActivity() {

    lateinit var binding: ActivityWelcomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_welcome)


        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this,Home::class.java))
            finishAffinity()
        }, 2000)
    }
}