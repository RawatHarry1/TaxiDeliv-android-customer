package com.superapp_customer.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.databinding.DataBindingUtil
import com.superapp_customer.R
import com.superapp_customer.databinding.ActivityWelcomeBinding
import com.superapp_customer.util.SharedPreferencesManager
import com.superapp_customer.view.activity.walk_though.Home
import com.superapp_customer.view.activity.walk_though.MainHome

class Welcome : AppCompatActivity() {

    lateinit var binding: ActivityWelcomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_welcome)
        Handler(Looper.getMainLooper()).postDelayed({
            if (!SharedPreferencesManager.getBoolean(SharedPreferencesManager.Keys.ONLY_FOR_ONE_TYPE))
                startActivity(Intent(this, MainHome::class.java))
            else
                startActivity(Intent(this, Home::class.java))
            finishAffinity()
        }, 2000)
    }
}