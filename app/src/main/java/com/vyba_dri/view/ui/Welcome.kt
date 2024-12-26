package com.vyba_dri.view.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.vyba_dri.R
import com.vyba_dri.databinding.ActivityWelcomeBinding
import com.vyba_dri.view.base.BaseActivity

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