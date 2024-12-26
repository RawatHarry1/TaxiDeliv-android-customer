package com.vyba_dri.view.ui

import android.content.Intent
import android.os.Bundle
import com.vyba_dri.R
import com.vyba_dri.databinding.ActivityUploadMunicipleBinding
import com.vyba_dri.view.base.BaseActivity

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