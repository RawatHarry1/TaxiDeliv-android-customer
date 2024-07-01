package com.salonedriver.customClasses

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import com.birjuvachhani.locus.Locus
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mukesh.photopicker.utils.checkPermissions
import com.salonedriver.R
import com.salonedriver.SaloneDriver
import com.salonedriver.dialogs.CustomProgressDialog
import com.salonedriver.util.ResourceUtils.getString
import java.lang.ref.WeakReference


object SingleFusedLocation {

    private var context: WeakReference<Context>? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationResultHandler: LocationResultHandler? = null
    private val progressBar by lazy { CustomProgressDialog() }
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>


    fun initialize(
        context: Context,
        locationResultHandler: LocationResultHandler
    ) {
        this.context = WeakReference(context)
        this.locationResultHandler = locationResultHandler
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
//        setupPermissionLauncher(context)
        checkPermissions()
    }







        private fun checkPermissions() {
        context?.get()?.checkPermissions(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) {

            requestSingleTimeLocation()

        }
    }



    private fun requestSingleTimeLocation() {
        context?.get()?.let { context ->
            progressBar.show(SaloneDriver.appContext)
            context.checkPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) {
                if (!isLocationEnabled(context)) {
                    AlertDialog.Builder(context).apply {
                        setTitle(context.getString(R.string.location_permission))
                        setMessage(context.getString(R.string.please_turn_on_device_location_and_gps))
                        setPositiveButton(
                            "ok"
                        ) { dialog, which -> dialog?.dismiss() }
                        create()
                        show()
                    }
                    return@checkPermissions
                }
                Locus.getCurrentLocation(context) {
                    hideProcessDialog()
                    it.location?.let { it1 ->
//                    it1.latitude = 40.234926
//                    it1.longitude = -75.287734
                        Log.e("Location", "Latitude: ${it1.latitude}, Longitude: ${it1.longitude}")
                        locationResultHandler?.updatedLocation(it1)
                        locationResultHandler = null
                    } ?: run {
                        Toast.makeText(
                            context,
                            it.error?.localizedMessage.orEmpty(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }


    /**
     * Hide Progress Bar
     * */
    private fun hideProcessDialog() {
        try {
            progressBar.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isLocationEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is a new method provided in API 28
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.isLocationEnabled
        } else {
            // This was deprecated in API 28
            val mode: Int = Settings.Secure.getInt(
                context.contentResolver, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            )
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }

}