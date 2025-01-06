package com.mcabs_driver.view.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.mcabs_driver.R
import com.mcabs_driver.databinding.ActivityWelcomeBinding
import com.mcabs_driver.view.base.BaseActivity

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