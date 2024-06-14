package com.venus_customer.view.activity.verifyOtp

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.mukesh.mukeshotpview.completeListener.MukeshOtpCompleteListener
import com.venus_customer.R
import com.venus_customer.VenusApp
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.VerifyOtpBinding
import com.venus_customer.model.api.observeData
import com.venus_customer.util.SharedPreferencesManager
import com.venus_customer.view.activity.CreateProfile
import com.venus_customer.view.activity.walk_though.Home
import com.venus_customer.view.base.BaseActivity
import com.venus_customer.viewmodel.base.SignInViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject

@AndroidEntryPoint
class VerifyOtp : BaseActivity<VerifyOtpBinding>() {

    lateinit var binding: VerifyOtpBinding
    private val countryCode by lazy { intent.getStringExtra("countryCode") }
    private val phoneNumber by lazy { intent.getStringExtra("phoneNo") }
    private val viewModel by viewModels<SignInViewModel>()

    override fun getLayoutId(): Int {
        return R.layout.verify_otp
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        setContentView(binding.root)
        clickHandler()
        observerOtpData()
    }


    private fun clickHandler() {
        binding.ivBackVerify.setOnSingleClickListener {
            finish()
        }


        binding.tvNotReceived.setOnSingleClickListener {
            viewModel.signIn(jsonObject = JSONObject().apply {
                put("countryCode", countryCode)
                put("phoneNo", phoneNumber)
            })
        }


        binding.simpleOtpView.setOtpCompletionListener(object : MukeshOtpCompleteListener {
            override fun otpCompleteListener(otp: String?) {
                viewModel.verifyOtp(jsonObject = JSONObject().apply {
                    put("countryCode", countryCode)
                    put("phoneNo", phoneNumber)
                    put("loginOtp", otp)
                    put("latitude", VenusApp.latLng.latitude)
                    put("longitude", VenusApp.latLng.longitude)
                })
            }
        })
    }



    private fun observerOtpData() = viewModel.verifyOtp.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        SharedPreferencesManager.putModel(SharedPreferencesManager.Keys.USER_DATA, this)
        if (this?.login?.isCustomerProfileComplete == 1){
            startActivity(Intent(this@VerifyOtp, Home::class.java))
            finishAffinity()
        }else {
            startActivity(Intent(this@VerifyOtp, CreateProfile::class.java))
            finish()
        }
    }, onError = {
        hideProgressDialog()
        showToastShort(this)
    })
}