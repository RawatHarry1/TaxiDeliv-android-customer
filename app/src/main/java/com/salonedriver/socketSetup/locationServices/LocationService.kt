package com.salonedriver.socketSetup.locationServices

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.birjuvachhani.locus.Locus
import com.birjuvachhani.locus.LocusResult
import com.google.android.gms.maps.model.LatLng
import com.mukesh.photopicker.utils.checkPermissions
import com.salonedriver.R
import com.salonedriver.SaloneDriver
import com.salonedriver.customClasses.SingleFusedLocation
import com.salonedriver.socketSetup.SocketSetup
import com.salonedriver.util.AppLifecycleObserver
import com.salonedriver.util.AppUtils


class LocationService : LifecycleService() {
    var previousLatLong: LatLng = LatLng(0.0, 0.0)
    companion object {
        const val NOTIFICATION_ID = 787
        const val LOCATION_BROADCAST = "locationBroadcast"
        const val STOP_SERVICE_BROADCAST_ACTON =
            "com.birjuvachhani.locationextensionsample.ServiceStopBroadcastReceiver"
        const val RESTART_ACTION = "com.salonedriver.RESTART_SERVICE"
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    private val manager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: throw Exception("No notification manager found")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Handler(Looper.getMainLooper()).postDelayed({
            start()
            Log.d("LocationService", "<<<<<<<<<<<<<<<<<<Service started>>>>>>>>>>>>>>>>")

        }, 2000)
        return START_STICKY
    }

    private fun start() {
        startForeground(NOTIFICATION_ID, getNotification())
        checkPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) {
            if (!SingleFusedLocation.isLocationEnabled(applicationContext)) return@checkPermissions
            Locus.configure {
                enableBackgroundUpdates = true
            }
            Locus.startLocationUpdates(this).observe(this) { result ->
                Log.d(
                    "LocationService",
                    "<<<<<<<<<<<<<<<<<<Getting Location Update>>>>>>>>>>>>>>>>"
                )
//                if (
//                    result.location?.latitude == previousLatLong.latitude &&
//                    result.location?.longitude == previousLatLong.longitude
//                ) {
//                    return@observe
//                }


                val currentLocation = result.location ?: return@observe
                // Set a threshold for considering significant location changes (in meters)
                val thresholdDistance = 10 // 10 meters
                val distance = FloatArray(1)
                Location.distanceBetween(
                    previousLatLong.latitude, previousLatLong.longitude,
                    currentLocation.latitude, currentLocation.longitude,
                    distance
                )
                if (distance[0] < thresholdDistance) {
                    return@observe // If the change in location is less than the threshold, ignore it
                }

                // Update the previous location with the current location
                previousLatLong = LatLng(currentLocation.latitude, currentLocation.longitude)
                Log.e(
                    "LocationUpdate",
                    "OnService   Lat: ${currentLocation.latitude}, Lng: ${currentLocation.longitude}"
                )
                if (AppLifecycleObserver.isAppInForeground()) {
                    Log.e(
                        "LocationUpdate",
                        "OnService  App is running Lat: ${currentLocation.latitude}, Lng: ${currentLocation.longitude}"
                    )
                    LocalBroadcastManager.getInstance(this).sendBroadcast(Intent().apply {
                        action = LOCATION_BROADCAST
                        putExtra("latitude", result.location?.latitude.toString())
                        putExtra("longitude", result.location?.longitude.toString())
                        putExtra("bearing", result.location?.bearing.toString())
                    })
                } else {
                    Log.e(
                        "LocationUpdate",
                        "OnService  App is killed Lat: ${currentLocation.latitude}, Lng: ${currentLocation.longitude}"
                    )
                    if (AppUtils.tripId.isNotEmpty()) {
                        SaloneDriver.latLng =
                            LatLng(
                                result.location?.latitude ?: 0.0,
                                result.location?.longitude ?: 0.0
                            )
                        SocketSetup.emitLocation(
                            LatLng(
                                result.location?.latitude ?: 0.0,
                                result.location?.longitude ?: 0.0
                            ),
                            result.location?.bearing.toString(),
                            AppUtils.tripId
                        )
                    }
                }
            }
        }
        manager.notify(NOTIFICATION_ID, getNotification())
    }

    private fun getNotification(result: LocusResult? = null): Notification {
        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: throw Exception("No notification manager found")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    "location",
                    "Location Updates",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
        return with(NotificationCompat.Builder(this, "location")) {
            setContentTitle("Location Service")
            result?.apply {
                location?.let {
                    setContentText("We are accessing your location for tracking.")
                } ?: setContentText("Error: ${error?.message}")
            } ?: setContentText("Trying to get location updates")
            setSmallIcon(R.drawable.ic_location)
            setAutoCancel(false)
            setOnlyAlertOnce(true)
            build()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d("LocationService", "<<<<<<<<<<<<<<<<<<Destroyed Service Called>>>>>>>>>>>>>>>>")
        // Schedule the restart if the trip ID is not empty
        if (AppUtils.tripId.isNotEmpty()) {
            Handler(Looper.getMainLooper()).postDelayed({
                Log.d("LocationService", "Restarting Service")
                startService(Intent(this, LocationService::class.java))
            }, 1000) // Delay to ensure service has time to restart properly
        } else {
            manager.cancel(NOTIFICATION_ID)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("LocationService", "Service Created")
    }
}