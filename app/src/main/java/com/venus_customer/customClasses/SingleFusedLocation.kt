package com.venus_customer.customClasses

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.birjuvachhani.locus.Locus
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.mukesh.photopicker.utils.checkPermissions
import com.venus_customer.R
import com.venus_customer.VenusApp
import com.venus_customer.dialogs.CustomProgressDialog
import com.venus_customer.util.showSnackBar
import java.lang.ref.WeakReference

object SingleFusedLocation {

    private var context: WeakReference<Context>? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationResultHandler: LocationResultHandler? = null
    private val progressBar by lazy { CustomProgressDialog() }


    fun initialize(context: Context, locationResultHandler: LocationResultHandler){
        SingleFusedLocation.context = WeakReference(context)
        SingleFusedLocation.locationResultHandler = locationResultHandler
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        checkPermissions()
    }


    private fun checkPermissions(){
        context?.get()?.checkPermissions(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ){
            context?.get()?.let {
                if (!isLocationEnabled(it)) {
                    AlertDialog.Builder(it).apply {
                        setTitle(context.getString(R.string.location_permission))
                        setMessage(context.getString(R.string.please_turn_on_device_location_and_gps))
                        setPositiveButton("ok"
                        ) { dialog, which -> dialog?.dismiss() }
                        create()
                        show()
                    }
                    return@checkPermissions
                }
                requestSingleTimeLocation()
            }

        }
    }


    private fun requestSingleTimeLocation() {
        context?.get()?.let { context ->
//            progressBar.show(VenusApp.appContext)
            Locus.getCurrentLocation(context){
                hideProcessDialog()
                it.location?.let { it1 ->
//                    it1.latitude = 40.234926
//                    it1.longitude = -75.287734
                    VenusApp.latLng = LatLng(it1.latitude, it1.longitude)
                    locationResultHandler?.updatedLocation(it1)
                } ?: run {
                    showSnackBar(it.error?.localizedMessage.orEmpty())
                }
            }
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


    /**
     * Hide Progress Bar
     * */
    private fun hideProcessDialog(){
        try {
            progressBar.dismiss()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

}