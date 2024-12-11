package com.ujeff_customer.view.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.model.LatLng
import com.ujeff_customer.R
import com.ujeff_customer.VenusApp
import com.ujeff_customer.customClasses.LocationResultHandler
import com.ujeff_customer.customClasses.SingleFusedLocation
import com.ujeff_customer.customClasses.singleClick.setOnSingleClickListener
import com.ujeff_customer.databinding.ActivitySignUpInBinding
import com.ujeff_customer.dialogs.DialogUtils
import com.ujeff_customer.model.dataClass.base.ClientConfig
import com.ujeff_customer.util.AppUtils
import com.ujeff_customer.util.SharedPreferencesManager
import com.ujeff_customer.util.showSnackBar
import com.ujeff_customer.view.activity.sign_in.SignIn

class SignUpInActivity : AppCompatActivity() {
    private var signInClicked = false
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var enableGpsLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var binding: ActivitySignUpInBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up_in)
        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_color)

        // Optional: Adjust the status bar text color to ensure readability
        // Use dark or light content based on the background color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
//        SharedPreferencesManager.getModel<ClientConfig>(SharedPreferencesManager.Keys.CLIENT_CONFIG)
//            ?.let {
//                binding.tvWalkTitle.text = it.walkThroughTitle ?: "Venus"
//                binding.tvWalkDesc.text = it.walkThroughDesc ?: "Clean Air Ride share"
//            }
        binding.tvSignIn.setOnSingleClickListener {
            signInClicked = true
            AppUtils.checkAndEnableGPS(
                this@SignUpInActivity,
                ::onGPSEnabled,
                ::onGPSDenied,
                enableGpsLauncher
            )
        }
        binding.tvHaveAnAccount.setOnSingleClickListener {
            signInClicked = false
            AppUtils.checkAndEnableGPS(
                this@SignUpInActivity,
                ::onGPSEnabled,
                ::onGPSDenied,
                enableGpsLauncher
            )
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
            SingleFusedLocation.initialize(VenusApp.appContext, object :
                LocationResultHandler {
                override fun updatedLocation(location: Location) {
                    Log.i("CurrentLocation", "OnVenusApp")
                    VenusApp.latLng = LatLng(location.latitude, location.longitude)
                    if (signInClicked)
                        startActivity(Intent(this@SignUpInActivity, SignIn::class.java))
                    else
                        startActivity(Intent(this@SignUpInActivity, SignIn::class.java).apply {
                            putExtra("type", "sign_up")
                        })
                }
            })
        }
    }

    private fun onGPSDenied() {
        // The user did not enable GPS
        showSnackBar("GPS is required for this app")
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
            DialogUtils.getPermissionDeniedDialog(
                this,
                1,
                getString(R.string.allow_location_precise),
                ::onDialogPermissionAllowClick
            )
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

    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            SingleFusedLocation.initialize(VenusApp.appContext, object :
                LocationResultHandler {
                override fun updatedLocation(location: Location) {
                    Log.i("CurrentLocation", "OnVenusApp")
                    VenusApp.latLng = LatLng(location.latitude, location.longitude)
                    if (signInClicked)
                        startActivity(Intent(this@SignUpInActivity, SignIn::class.java))
                    else
                        startActivity(Intent(this@SignUpInActivity, SignIn::class.java).apply {
                            putExtra("type", "sign_up")
                        })
                }
            })
        } else {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                DialogUtils.getPermissionDeniedDialog(
                    this,
                    0,
                    getString(R.string.allow_location_precise),
                    ::onDialogPermissionAllowClick
                )
            } else {
                DialogUtils.getPermissionDeniedDialog(
                    this,
                    1,
                    getString(R.string.allow_location_precise),
                    ::onDialogPermissionAllowClick
                )
//                showSettingsDialog(this)
            }
        }
    }
}