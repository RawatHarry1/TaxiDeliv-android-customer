package com.salonedriver.view.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.salonedriver.R
import com.salonedriver.databinding.ActivitySignInBinding
import com.salonedriver.model.api.observeData
import com.salonedriver.model.dataclassses.clientConfig.ClientConfigDC
import com.salonedriver.util.SharedPreferencesManager
import com.salonedriver.util.getValue
import com.salonedriver.view.base.BaseActivity
import com.salonedriver.viewmodel.OnBoardingVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignIn : BaseActivity<ActivitySignInBinding>() {

    private lateinit var binding: ActivitySignInBinding
    private val viewModel by viewModels<OnBoardingVM>()

    override fun getLayoutId(): Int {
        return R.layout.activity_sign_in
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = getViewDataBinding()
        observerSendOtpResponse()

        binding.ccp.setCountryForNameCode(SharedPreferencesManager.getModel<ClientConfigDC>(SharedPreferencesManager.Keys.CLIENT_CONFIG)?.let {
            it.defaultCountryIso ?: "IN"
        } ?: run { "IN" })

        binding.tvForgotPassword.setOnClickListener {
            startActivity(
                Intent(
                    this, ForgotPassword::class.java
                )
            )
        }
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.tvSignUpBtn.setOnClickListener {
            if (binding.etEmail.getValue().isEmpty()){
                showErrorMessage(getString(R.string.please_enter_phone_number))
            } else viewModel.sendLoginOtp(binding.etEmail.getValue(), binding.ccp.selectedCountryCodeWithPlus)
        }
    }



    /**
     * Observe Send OTP Response
     * */
    private fun observerSendOtpResponse() = viewModel.sendLoginOtp.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        startActivity(Intent(this@SignIn, Verify::class.java).also {
            it.putExtra("countryCode", binding.ccp.selectedCountryCodeWithPlus.toString())
            it.putExtra("phoneNumber", binding.etEmail.getValue())
        })
    }, onError = {
        showErrorMessage(this)
        hideProgressDialog()
    })

}