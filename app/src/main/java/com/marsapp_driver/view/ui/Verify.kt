package com.marsapp_driver.view.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import com.marsapp_driver.R
import com.marsapp_driver.customClasses.singleClick.setOnSingleClickListener
import com.marsapp_driver.databinding.ActivityVerifyBinding
import com.marsapp_driver.model.api.observeData
import com.marsapp_driver.model.api.profileStatusHandling
import com.marsapp_driver.util.SharedPreferencesManager
import com.marsapp_driver.view.base.BaseActivity
import com.marsapp_driver.viewmodel.OnBoardingVM
import com.mukesh.mukeshotpview.completeListener.MukeshOtpCompleteListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Verify : BaseActivity<ActivityVerifyBinding>() {
    private lateinit var binding: ActivityVerifyBinding
    private val viewModel by viewModels<OnBoardingVM>()

    override fun getLayoutId(): Int {
        return R.layout.activity_verify
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        viewModel.phoneNumber = intent.getStringExtra("phoneNumber").orEmpty()
        viewModel.countryCode = intent.getStringExtra("countryCode").orEmpty()
        viewModel.passcode = intent.getStringExtra("passcode").orEmpty()
        binding.tvEmailVerify.text = "${viewModel.countryCode} ${viewModel.phoneNumber}"
        binding.ivBackVerify.setOnSingleClickListener { finish() }
        observeVerifyOtp()
        otpCompleteListener()
        observerSendOtpResponse()
    }


    private fun otpCompleteListener() = try {
        binding.simpleOtpView.setOtpCompletionListener(object : MukeshOtpCompleteListener {
            override fun otpCompleteListener(otp: String?) {
                viewModel.verifyOtp(otp.orEmpty())
            }
        })

        binding.tvNotReceived.setOnSingleClickListener {
            viewModel.sendLoginOtp(
                phoneNumber = viewModel.phoneNumber.orEmpty(),
                countryCode = viewModel.countryCode.orEmpty()
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }


    private fun observeVerifyOtp() = viewModel.verifyData.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        SharedPreferencesManager.putModel(SharedPreferencesManager.Keys.USER_DATA, this)
        if (this?.login?.clientConfig != null) {
            SharedPreferencesManager.put(
                SharedPreferencesManager.Keys.PASSCODE,
                this.login?.clientConfig?.passcode ?: ""
            )
            SharedPreferencesManager.putModel(
                SharedPreferencesManager.Keys.CLIENT_CONFIG,
                this.login?.clientConfig
            )
        }
        profileStatusHandling(this?.login?.registrationStepCompleted)
    }, onError = {
        hideProgressDialog()
        showErrorMessage(this)
    })


    /**
     * Observe Send OTP Response
     * */
    private fun observerSendOtpResponse() = viewModel.sendLoginOtp.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        showErrorMessage("4-digit code has been sent")
        hideProgressDialog()
    }, onError = {
        showErrorMessage(this)
        hideProgressDialog()
    })

}