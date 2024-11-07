package com.superapp_driver.view.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.superapp_driver.R
import com.superapp_driver.databinding.ActivityWelcomeBinding
import com.superapp_driver.view.base.BaseActivity

class Welcome : BaseActivity<ActivityWelcomeBinding>() {

    lateinit var binding: ActivityWelcomeBinding

    override fun getLayoutId(): Int {
        return R.layout.activity_welcome
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
    }


    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this,VehicleInfo::class.java))
            finishAffinity()
        }, 2000)
    }
}