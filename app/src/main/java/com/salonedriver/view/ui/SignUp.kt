package com.salonedriver.view.ui

import android.Manifest
import android.content.Intent
import android.location.Location
import android.os.Bundle
import androidx.activity.viewModels
import com.google.android.gms.maps.model.LatLng
import com.mukesh.photopicker.utils.checkPermissions
import com.salonedriver.R
import com.salonedriver.customClasses.LocationResultHandler
import com.salonedriver.customClasses.SingleFusedLocation
import com.salonedriver.databinding.ActivitySignUpBinding
import com.salonedriver.dialogs.DialogUtils
import com.salonedriver.model.api.observeData
import com.salonedriver.model.dataclassses.clientConfig.ClientConfigDC
import com.salonedriver.util.SharedPreferencesManager
import com.salonedriver.util.getValue
import com.salonedriver.view.base.BaseActivity
import com.salonedriver.view.ui.home_drawer.HomeActivity
import com.salonedriver.viewmodel.OnBoardingVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUp : BaseActivity<ActivitySignUpBinding>() {

    lateinit var binding: ActivitySignUpBinding
    private val viewModel by viewModels<OnBoardingVM>()


    override fun getLayoutId(): Int {
        return R.layout.activity_sign_up
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()

        binding.ccp.setCountryForNameCode(SharedPreferencesManager.getModel<ClientConfigDC>(SharedPreferencesManager.Keys.CLIENT_CONFIG)?.let {
            it.defaultCountryIso ?: "IN"
        } ?: run { "IN" })
        binding.tvSignIn.setOnClickListener {
            startActivity(Intent(this, SignIn::class.java))
            finish()
        }
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.tvSignUpBtn.setOnClickListener {
            if (binding.etEmail.getValue().isEmpty()) {
                showErrorMessage(getString(R.string.please_enter_phone_number))
            } else viewModel.sendLoginOtp(
                binding.etEmail.getValue(),
                binding.ccp.selectedCountryCodeWithPlus
            )
        }
        observerSendOtpResponse()
        observeVerifyOtp()
    }


    /**
     * On Click Dialog Lambda
     * */
    private fun onClickDialogLambda(otp: String?, isResend: Boolean?, phoneNumber: String) {
        if (isResend == true) {
            viewModel.sendLoginOtp(phoneNumber, binding.ccp.selectedCountryCodeWithPlus)
        } else {
            viewModel.verifyOtp(otpCode = otp.orEmpty())
        }
    }


    /**
     * Observe Send OTP Response
     * */
    private fun observerSendOtpResponse() = viewModel.sendLoginOtp.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        viewModel.phoneNumber = binding.etEmail.getValue()
        startActivity(Intent(this@SignUp, Verify::class.java).also {
            it.putExtra("countryCode", binding.ccp.selectedCountryCodeWithPlus.toString())
            it.putExtra("phoneNumber", binding.etEmail.getValue())
        })
    }, onError = {
        showErrorMessage(this)
        hideProgressDialog()
    })


    /**
     * Observe Verify OTP
     * */
    private fun observeVerifyOtp() = viewModel.verifyData.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        SharedPreferencesManager.putModel(SharedPreferencesManager.Keys.USER_DATA, this)
        if (this?.login?.isRegistrationComplete == true) {
            startActivity(Intent(this@SignUp, HomeActivity::class.java))
        } else {
            startActivity(Intent(this@SignUp, CreateProfile::class.java))
        }
    }, onError = {
        showErrorMessage(this)
        hideProgressDialog()
    })

}