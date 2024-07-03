package com.salonedriver.view.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.salonedriver.R
import com.salonedriver.customClasses.singleClick.setOnSingleClickListener
import com.salonedriver.databinding.ActivitySignUpBinding
import com.salonedriver.model.api.observeData
import com.salonedriver.model.dataclassses.clientConfig.ClientConfigDC
import com.salonedriver.util.ResourceUtils
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
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>


    override fun getLayoutId(): Int {
        return R.layout.activity_sign_up
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()

        binding.ccp.setCountryForNameCode(
            SharedPreferencesManager.getModel<ClientConfigDC>(
                SharedPreferencesManager.Keys.CLIENT_CONFIG
            )?.let {
                it.defaultCountryIso ?: "IN"
            } ?: run { "IN" })
        binding.tvSignIn.setOnSingleClickListener {
            startActivity(Intent(this@SignUp, SignIn::class.java))
            finish()
        }
        binding.ivBack.setOnSingleClickListener {
            finish()
        }
        binding.tvSignUpBtn.setOnSingleClickListener {
            if (binding.etEmail.getValue().isEmpty()) {
                showErrorMessage(getString(R.string.please_enter_phone_number))
            } else if (binding.etEmail.length() != 10) {
                showErrorMessage("*Please enter valid mobile number.")
            } else {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }


        }
        observerSendOtpResponse()
        observeVerifyOtp()
        // Setup the permission launcher
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                handlePermissionResult(permissions)
            }
    }

    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.sendLoginOtp(
                binding.etEmail.getValue(),
                binding.ccp.selectedCountryCodeWithPlus
            )
        } else {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                showPermissionRationaleDialog(this)
            } else {
                showSettingsDialog(this)
            }
        }
    }

    private fun showSettingsDialog(context: Context) {
        AlertDialog.Builder(context).apply {
            setTitle(ResourceUtils.getString(R.string.location_permission))
            setMessage(ResourceUtils.getString(R.string.please_turn_on_device_location_and_gps))
            setPositiveButton("Settings") { _, _ ->
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null)
                )
                context.startActivity(intent)
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    private fun showPermissionRationaleDialog(context: Context) {
        AlertDialog.Builder(context).apply {
            setTitle(ResourceUtils.getString(R.string.location_permission))
            setMessage(ResourceUtils.getString(R.string.please_turn_on_device_location_and_gps))
            setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                checkPermissions()
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            create()
            show()
        }
    }


    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            viewModel.sendLoginOtp(
                binding.etEmail.getValue(),
                binding.ccp.selectedCountryCodeWithPlus
            )
        }

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