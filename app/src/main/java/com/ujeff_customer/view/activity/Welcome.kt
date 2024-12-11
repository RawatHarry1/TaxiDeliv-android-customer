package com.ujeff_customer.view.activity

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowInsetsController
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.ujeff_customer.R
import com.ujeff_customer.databinding.ActivityWelcomeBinding
import com.ujeff_customer.util.SharedPreferencesManager
import com.ujeff_customer.view.activity.walk_though.Home
import com.ujeff_customer.view.activity.walk_though.MainHome

class Welcome : AppCompatActivity() {

    lateinit var binding: ActivityWelcomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_welcome)
        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_color)

        // Optional: Adjust the status bar text color to ensure readability
        // Use dark or light content based on the background color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        Handler(Looper.getMainLooper()).postDelayed({
            if (!SharedPreferencesManager.getBoolean(SharedPreferencesManager.Keys.ONLY_FOR_ONE_TYPE))
                startActivity(Intent(this, MainHome::class.java))
            else
                startActivity(Intent(this, Home::class.java))
            finishAffinity()
        }, 2000)
    }
}