package com.ujeff_driver.view.ui

import android.content.Intent
import android.os.Bundle
import com.ujeff_driver.R
import com.ujeff_driver.databinding.ActivityUploadMunicipleBinding
import com.ujeff_driver.view.base.BaseActivity

class UploadMuniciple : BaseActivity<ActivityUploadMunicipleBinding>() {

    lateinit var binding: ActivityUploadMunicipleBinding

    override fun getLayoutId(): Int {
        return R.layout.activity_upload_municiple

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        binding.ivBackMuniUpload.setOnClickListener { finish() }
        binding.tvNextMuni.setOnClickListener {
            startActivity(Intent(this@UploadMuniciple, VehicleInfo::class.java))
        }
    }
}