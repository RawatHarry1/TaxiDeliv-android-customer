package com.superapp_driver.view.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.superapp_driver.R
import com.superapp_driver.customClasses.singleClick.setOnSingleClickListener
import com.superapp_driver.databinding.ActivitySignUpBinding
import com.superapp_driver.dialogs.DialogUtils
import com.superapp_driver.model.api.observeData
import com.superapp_driver.model.dataclassses.clientConfig.ClientConfigDC
import com.superapp_driver.util.AppUtils
import com.superapp_driver.util.ResourceUtils
import com.superapp_driver.util.SharedPreferencesManager
import com.superapp_driver.util.getValue
import com.superapp_driver.view.base.BaseActivity
import com.superapp_driver.view.ui.home_drawer.HomeActivity
import com.superapp_driver.viewmodel.OnBoardingVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUp : BaseActivity<ActivitySignUpBinding>() {

    lateinit var binding: ActivitySignUpBinding
    private val viewModel by viewModels<OnBoardingVM>()
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var enableGpsLauncher: ActivityResultLauncher<IntentSenderRequest>

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
            }
//            else if (binding.etEmail.length() != 10) {
//                showErrorMessage("*Please enter valid mobile number.")
//            }
            else {
                AppUtils.checkAndEnableGPS(
                    this@SignUp,
                    ::onGPSEnabled,
                    ::onGPSDenied,
                    enableGpsLauncher
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
        // Initialize the GPS enable launcher
        enableGpsLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    onGPSEnabled()
                } else {
                    onGPSDenied()
                }
            }
    }

    private fun onGPSEnabled() {
        // GPS is now enabled
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun onGPSDenied() {
        // The user did not enable GPS
        showErrorMessage("GPS is required for this app")
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
//                showPermissionRationaleDialog(this)
                DialogUtils.getPermissionDeniedDialog(
                    this,
                    0,
                    getString(R.string.allow_location_precise),
                    ::onDialogPermissionAllowClick
                )
            } else {
//                showSettingsDialog(this)
                DialogUtils.getPermissionDeniedDialog(
                    this,
                    1,
                    getString(R.string.allow_location_precise),
                    ::onDialogPermissionAllowClick
                )
            }
        }
    }

    private fun onDialogPermissionAllowClick(type: Int) {
        if (type == 0) {
            checkPermissions()
        } else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }
    }

    private fun showSettingsDialog(context: Context) {
        AlertDialog.Builder(context).apply {
            setTitle(ResourceUtils.getString(R.string.location_permission))
            setMessage("Please select precise location for app to work accurately with location")
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
            setMessage("Please select precise location for app to work accurately with location")
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