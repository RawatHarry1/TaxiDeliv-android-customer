package com.superapp_customer.view.activity

import android.os.Bundle
import android.view.View
import com.superapp_customer.R
import com.superapp_customer.customClasses.singleClick.setOnSingleClickListener
import com.superapp_customer.databinding.ActivityForgotPasswordBinding
import com.superapp_customer.view.base.BaseActivity

class ForgotPassword : BaseActivity<ActivityForgotPasswordBinding>() {

    private lateinit var binding: ActivityForgotPasswordBinding
    var screenType = "forgot_password"
    override fun getLayoutId(): Int {
        return R.layout.activity_forgot_password
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        binding.ivBack.setOnSingleClickListener { setVisibility(true) }
        binding.ivCross.setOnSingleClickListener { setVisibility(true) }
        binding.tvResetPassBtn.setOnSingleClickListener {
            setVisibility(false)
        }
    }

    private fun setVisibility(backClicked:Boolean) {

        when(screenType)
        {
            "forgot_password" -> if(backClicked) finish() else screenType = "check_mail"
            "check_mail" -> screenType = if(backClicked) "forgot_password" else "reset_password"
            "reset_password" -> if(backClicked) screenType = "check_mail" else finish()
        }

        binding.ivBack.visibility = if(screenType=="check_mail") View.GONE else View.VISIBLE
        binding.ivCross.visibility = if(screenType=="check_mail") View.VISIBLE else View.GONE
        binding.clForgotPassword.visibility = if(screenType=="forgot_password") View.VISIBLE else View.GONE
        binding.clResetPassword.visibility = if(screenType=="reset_password") View.VISIBLE else View.GONE
        binding.clCheckMail.visibility = if(screenType=="check_mail") View.VISIBLE else View.GONE
        binding.tvResetPassBtn.text = if(screenType=="reset_password") getString(R.string.txt_update_password) else getString(R.string.txt_reset_password)

    }
}