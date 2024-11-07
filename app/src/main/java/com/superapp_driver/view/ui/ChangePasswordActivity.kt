package com.superapp_driver.view.ui

import android.os.Bundle
import com.superapp_driver.R
import com.superapp_driver.databinding.ActivityChangePasswordBinding
import com.superapp_driver.view.base.BaseActivity

class ChangePasswordActivity : BaseActivity<ActivityChangePasswordBinding>() {

    lateinit var binding: ActivityChangePasswordBinding

    override fun getLayoutId(): Int {
        return R.layout.activity_change_password
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()

    }

    override fun onResume() {
        super.onResume()

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.tvUpdateBtn.setOnClickListener {
            finish()
        }
    }


}