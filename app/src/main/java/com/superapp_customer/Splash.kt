package com.superapp_customer

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.superapp_customer.customClasses.LocationResultHandler
import com.superapp_customer.customClasses.SingleFusedLocation
import com.superapp_customer.databinding.ActivitySplashBinding
import com.superapp_customer.dialogs.DialogUtils
import com.superapp_customer.model.api.observeData
import com.superapp_customer.model.dataClass.userData.UserDataDC
import com.superapp_customer.util.AppUtils
import com.superapp_customer.util.ResourceUtils
import com.superapp_customer.util.SharedPreferencesManager
import com.superapp_customer.util.showSnackBar
import com.superapp_customer.view.activity.SignUpInActivity
import com.superapp_customer.view.activity.walk_though.Home
import com.superapp_customer.view.activity.walk_though.MainHome
import com.superapp_customer.view.activity.walk_though.WalkThrough
import com.superapp_customer.view.base.BaseActivity
import com.superapp_customer.viewmodel.base.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class Splash : BaseActivity<ActivitySplashBinding>() {

    private val mViewModel by viewModels<SplashViewModel>()
    lateinit var binding: ActivitySplashBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var enableGpsLauncher: ActivityResultLauncher<IntentSenderRequest>
    override fun getLayoutId(): Int {
        return R.layout.activity_splash
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        setObservers()

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
        val userData =
            SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
        if (userData?.login?.isCustomerProfileComplete == 1) {
            AppUtils.checkAndEnableGPS(
                this,
                ::onGPSEnabled,
                ::onGPSDenied,
                enableGpsLauncher
            )
        } else
            callGetOperatorToken()
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
        showSnackBar("GPS is required for this app")
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
//                Handler(Looper.getMainLooper()).postDelayed({
                    callGetOperatorToken()
//                }, 2000)
                }
            })
        } else {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
//                showSettingsDialog(this)
                DialogUtils.getPermissionDeniedDialog(
                    this,
                    0,
                    getString(R.string.allow_location_precise),
                    ::onDialogPermissionAllowClick
                )
            } else {
//                showPermissionRationaleDialog(this)
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
            finish()
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
                finish()
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
                finish()
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
            SingleFusedLocation.initialize(VenusApp.appContext, object :
                LocationResultHandler {
                override fun updatedLocation(location: Location) {
                    Log.i("CurrentLocation", "OnVenusApp")
                    VenusApp.latLng = LatLng(location.latitude, location.longitude)
//                Handler(Looper.getMainLooper()).postDelayed({
                    callGetOperatorToken()
//                }, 2000)
                }
            })
        }

    }


    private fun callGetOperatorToken() {
        mViewModel.fetchOperatorToken()
    }


    private fun setObservers() {
        mViewModel.fetchTokenResponseLiveData.observeData(this,
            onLoading = {
                showProgressDialog()
            }, onSuccess = {
                hideProgressDialog()
                SharedPreferencesManager.putModel(SharedPreferencesManager.Keys.CLIENT_CONFIG, this)
                VenusApp.googleMapKey = this?.googleMapKey ?: ""
                SharedPreferencesManager.put(
                    SharedPreferencesManager.Keys.ONLY_FOR_ONE_TYPE,
                    (this?.enabledService ?: 3) != 3
                )
                SharedPreferencesManager.put(
                    SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID,
                    this?.enabledService ?: 1
                )
                callWalkThrough()
            }, onError = {
                hideProgressDialog()
                showToastLong(this)
            })
    }


    private fun callWalkThrough() {
        val userData =
            SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
        if (userData?.login?.isCustomerProfileComplete == 1) {
            if (!SharedPreferencesManager.getBoolean(SharedPreferencesManager.Keys.ONLY_FOR_ONE_TYPE))
                startActivity(Intent(this, MainHome::class.java))
            else
                startActivity(Intent(this, Home::class.java))
        } else {
            val walkThroughShown =
                SharedPreferencesManager.getBoolean(SharedPreferencesManager.Keys.WALKTHROUGH)
            val intent = if (walkThroughShown)
                Intent(this, SignUpInActivity::class.java)
            else
                Intent(this, WalkThrough::class.java)
            startActivity(intent)
        }
        finishAffinity()
    }
}