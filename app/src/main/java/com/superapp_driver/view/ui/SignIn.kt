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
import com.superapp_driver.databinding.ActivitySignInBinding
import com.superapp_driver.dialogs.DialogUtils
import com.superapp_driver.model.api.observeData
import com.superapp_driver.model.dataclassses.clientConfig.ClientConfigDC
import com.superapp_driver.util.AppUtils
import com.superapp_driver.util.ResourceUtils
import com.superapp_driver.util.SharedPreferencesManager
import com.superapp_driver.util.getValue
import com.superapp_driver.view.base.BaseActivity
import com.superapp_driver.viewmodel.OnBoardingVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignIn : BaseActivity<ActivitySignInBinding>() {

    private lateinit var binding: ActivitySignInBinding
    private val viewModel by viewModels<OnBoardingVM>()
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var enableGpsLauncher: ActivityResultLauncher<IntentSenderRequest>
    override fun getLayoutId(): Int {
        return R.layout.activity_sign_in
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = getViewDataBinding()
        observerSendOtpResponse()

        binding.ccp.setCountryForNameCode(
            SharedPreferencesManager.getModel<ClientConfigDC>(
                SharedPreferencesManager.Keys.CLIENT_CONFIG
            )?.let {
                it.defaultCountryIso ?: "IN"
            } ?: run { "IN" })

        binding.tvForgotPassword.setOnSingleClickListener {
            startActivity(
                Intent(
                    this@SignIn, ForgotPassword::class.java
                )
            )
        }
        binding.tvSignUp.setOnSingleClickListener {
            startActivity(Intent(this@SignIn, SignUp::class.java))
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
                    this@SignIn,
                    ::onGPSEnabled,
                    ::onGPSDenied,
                    enableGpsLauncher
                )
            }
//                viewModel.sendLoginOtp(
//                binding.etEmail.getValue(),
//                binding.ccp.selectedCountryCodeWithPlus
//            )
        }


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