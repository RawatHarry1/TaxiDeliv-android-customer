package com.salonedriver.socketSetup.locationServices

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import com.salonedriver.customClasses.SingleFusedLocation


class LocationService : LifecycleService() {

    var previousLatLong : LatLng = LatLng(0.0, 0.0)

    companion object {
        const val NOTIFICATION_ID = 787
        const val LOCATION_BROADCAST = "locationBroadcast"
        const val STOP_SERVICE_BROADCAST_ACTON =
            "com.birjuvachhani.locationextensionsample.ServiceStopBroadcastReceiver"
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
        }, 2000)
        return START_STICKY
    }

    private fun start() {
        startForeground(NOTIFICATION_ID, getNotification())
        checkPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            ){
            if (!SingleFusedLocation.isLocationEnabled(applicationContext)) return@checkPermissions
            Locus.configure {
                enableBackgroundUpdates = true
            }
            Locus.startLocationUpdates(this).observe(this) { result ->
                if (
                    result.location?.latitude == previousLatLong.latitude &&
                    result.location?.longitude == previousLatLong.longitude
                ) {
                    return@observe
                }
                Log.e("sfdsfsdfds","sdfsdfsdfsdf  ${result.location?.latitude}")
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent().apply {
                    action = LOCATION_BROADCAST
                    putExtra("latitude", result.location?.latitude.toString())
                    putExtra("longitude", result.location?.longitude.toString())
                    putExtra("bearing", result.location?.bearing.toString())
                })
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
        manager.cancel(NOTIFICATION_ID)
        super.onDestroy()
    }
}