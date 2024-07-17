package com.venus_customer.view.activity.sign_in

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.venus_customer.R
import com.venus_customer.VenusApp
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.ActivitySignInBinding
import com.venus_customer.dialogs.DialogUtils
import com.venus_customer.model.api.observeData
import com.venus_customer.util.SharedPreferencesManager
import com.venus_customer.util.showSnackBar
import com.venus_customer.view.activity.CreateProfile
import com.venus_customer.view.activity.ForgotPassword
import com.venus_customer.view.activity.verifyOtp.VerifyOtp
import com.venus_customer.view.activity.walk_though.Home
import com.venus_customer.view.base.BaseActivity
import com.venus_customer.viewmodel.base.SignInViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject

@AndroidEntryPoint
class SignIn : BaseActivity<ActivitySignInBinding>() {

    private val viewModel by viewModels<SignInViewModel>()
    private lateinit var binding: ActivitySignInBinding
    private var screenType = "sign_in"
    private var dialog: Dialog? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_sign_in
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        if (intent.hasExtra("type")){
            screenType = intent.getStringExtra("type") ?: ""
        }
        clicks()
        setVisibility()
        observeSignIn()
        observerOtpData()
       Log.i("DEFAULTCODE","${ binding.ccp.defaultCountryCode}")
    }

    private fun clicks() {
        binding.tvForgotPassword.setOnSingleClickListener {
            startActivity(Intent(this@SignIn, ForgotPassword::class.java))
        }
        binding.ivBack.setOnSingleClickListener {
            finish()
        }
        binding.tvSignUp.setOnSingleClickListener {
            screenType = "sign_up"
            setVisibility()
        }
        binding.tvSignIn.setOnSingleClickListener {
            screenType = "sign_in"
            setVisibility()
        }
        binding.tvSignUpBtn.setOnSingleClickListener {
            if (binding.etEmail.text?.trim().isNullOrEmpty()) {
                showSnackBar("*Please enter mobile number.")
            }
//            else if (binding.etEmail.length() != 10) {
//                showSnackBar("*Please enter valid mobile number.")
//            }
            else {
                viewModel.signIn(jsonObject = JSONObject().apply {
                    put("countryCode", binding.ccp.selectedCountryCodeWithPlus)
                    put("phoneNo", binding.etEmail.text.toString())
                    put("loginType",  if (screenType == "sign_in") "2" else "1")
                })
            }
        }
    }


    private fun setVisibility() {
        binding.tvSignUpBtn.text =
            if (screenType == "sign_in") getString(R.string.txt_sign_in) else getString(R.string.txt_sign_up)
        binding.tvWelcome.text =
            if (screenType == "sign_in") getString(R.string.txt_welcome_back) else getString(R.string.txt_create_account)
        binding.tvThanksForGreen.text =
            if (screenType == "sign_in") getString(R.string.txt_thanks_for_going_green) else getString(R.string.txt_enter_email_phone)
        binding.tvSignIn.setTextColor(
            ContextCompat.getColorStateList(
                this,
                if (screenType == "sign_in") R.color.theme_button else R.color.border_color_dark
            )
        )
        binding.tvThanksForGreen.isVisible = true
        binding.viewSignIn.setBackgroundColor(
            ContextCompat.getColor(
                this,
                if (screenType == "sign_in") R.color.theme_button else R.color.border_color_dark
            )
        )
        binding.tvSignUp.setTextColor(
            ContextCompat.getColorStateList(
                this,
                if (screenType == "sign_in") R.color.border_color_dark else R.color.theme_button
            )
        )
        binding.viewSignUp.setBackgroundColor(
            ContextCompat.getColor(
                this,
                if (screenType == "sign_in") R.color.border_color_dark else R.color.theme_button
            )
        )

        binding.viewSignIn.isVisible = screenType == "sign_in"
        binding.viewSignUp.isVisible = screenType != "sign_in"
    }


    private fun observeSignIn() = viewModel.signInData.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        dialog?.let {
            return@let
        } ?: run {
            dialog = DialogUtils.verifyOtpDialog(
                this@SignIn,
                binding.ccp.selectedCountryCodeWithPlus,
                binding.etEmail.text.toString(),
                dismissDialog = { dialog ->
                                dialog.dismiss()
                    this@SignIn.dialog = null
                },
                verify = { otp, dialog ->
                    this@SignIn.dialog = dialog
                    viewModel.verifyOtp(jsonObject = JSONObject().apply {
                        put("countryCode", binding.ccp.selectedCountryCodeWithPlus)
                        put("phoneNo", binding.etEmail.text.toString())
                        put("loginOtp", otp)
                        put("latitude", VenusApp.latLng.latitude)
                        put("longitude", VenusApp.latLng.longitude)
                    })
                }, resend = {
                    this@SignIn.dialog = dialog
                    viewModel.signIn(jsonObject = JSONObject().apply {
                        put("countryCode", binding.ccp.selectedCountryCodeWithPlus)
                        put("phoneNo", binding.etEmail.text.toString())
                        put("loginType",  if (screenType == "sign_in") "2" else "1")
                    })
                }
            )
        }
    }, onError = {
        hideProgressDialog()
        showToastShort(this)
    })



    private fun observerOtpData() = viewModel.verifyOtp.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        SharedPreferencesManager.putModel(SharedPreferencesManager.Keys.USER_DATA, this)
        dialog?.dismiss()
        dialog = null
        if (this?.login?.isCustomerProfileComplete == 1){
            startActivity(Intent(this@SignIn, Home::class.java))
            finishAffinity()
        }else {
            startActivity(Intent(this@SignIn, CreateProfile::class.java))
            finish()
        }
    }, onError = {
        hideProgressDialog()
        showToastShort(this)
    })


}