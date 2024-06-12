package com.salonedriver.view.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.mukesh.mukeshotpview.completeListener.MukeshOtpCompleteListener
import com.salonedriver.R
import com.salonedriver.databinding.ActivityVerifyBinding
import com.salonedriver.model.api.observeData
import com.salonedriver.model.api.profileStatusHandling
import com.salonedriver.util.SharedPreferencesManager
import com.salonedriver.util.getValue
import com.salonedriver.view.base.BaseActivity
import com.salonedriver.viewmodel.OnBoardingVM
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
        binding.tvEmailVerify.text = "${viewModel.countryCode} ${viewModel.phoneNumber}"
        binding.ivBackVerify.setOnClickListener { finish() }
        observeVerifyOtp()
        otpCompleteListener()
        observerSendOtpResponse()
    }


    private fun otpCompleteListener() = try{
        binding.simpleOtpView.setOtpCompletionListener(object : MukeshOtpCompleteListener{
            override fun otpCompleteListener(otp: String?) {
                viewModel.verifyOtp(otp.orEmpty())
            }
        })

        binding.tvNotReceived.setOnClickListener {
            viewModel.sendLoginOtp(phoneNumber = viewModel.phoneNumber.orEmpty(), countryCode = viewModel.countryCode.orEmpty())
        }
    }catch (e:Exception){
        e.printStackTrace()
    }


    private fun observeVerifyOtp() = viewModel.verifyData.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        SharedPreferencesManager.putModel(SharedPreferencesManager.Keys.USER_DATA, this)
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
        hideProgressDialog()
    }, onError = {
        showErrorMessage(this)
        hideProgressDialog()
    })

}