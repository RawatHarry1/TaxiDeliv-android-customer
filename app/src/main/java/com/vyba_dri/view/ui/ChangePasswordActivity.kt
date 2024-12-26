package com.vyba_dri.view.ui

import android.os.Bundle
import com.vyba_dri.R
import com.vyba_dri.databinding.ActivityChangePasswordBinding
import com.vyba_dri.view.base.BaseActivity

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