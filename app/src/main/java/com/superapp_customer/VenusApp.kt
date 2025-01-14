package com.superapp_customer

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.superapp_customer.di.AppComponent
import com.superapp_customer.di.DaggerAppComponent
import com.superapp_customer.util.showSnackBar
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VenusApp : Application() {
    companion object {
        lateinit var instance: VenusApp
        lateinit var appContext: Context
        lateinit var applicationComponent: AppComponent
        var latLng: LatLng = LatLng(0.0, 0.0)
        var onChatScreen = false
        var googleMapKey = ""
        var offerApplied = 0
        var offerTitle = ""
        var isReferee = false
        var referralMsg = ""
        var isServiceTypeDefault = true
        var primaryColor: Int = Color.parseColor("#03DAC5")
        var secondaryColor: Int = Color.parseColor("#03DAC5")
        var backgroundColor: Int = Color.parseColor("#FFFFFF")
        var textColor: Int = Color.parseColor("#000000")
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
//        fetchRemoteConfigColors()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        instance = this
        appContext = applicationContext
        applicationComponent = DaggerAppComponent.builder().build()

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                appContext = activity
            }

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {
                appContext = activity
            }

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {}

        })
    }

    override fun onLowMemory() {
        super.onLowMemory()
//        System.gc()
    }

    private fun fetchRemoteConfigColors() {
        val remoteConfig = Firebase.remoteConfig
        remoteConfig.setDefaultsAsync(
            mapOf(
                "colorPrimary" to "#FF3B30",
            )
        )
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("colorPrimary","${remoteConfig.getString("colorPrimary")}")
                    primaryColor = Color.parseColor(
                        if (remoteConfig.getString("colorPrimary")
                                .contains("#")
                        ) remoteConfig.getString("colorPrimary") else "#${
                            remoteConfig.getString(
                                "colorPrimary"
                            )
                        }"
                    )
                } else {
                    task.exception?.let { it.localizedMessage?.let { it1 -> showSnackBar(it1) } }
                }
            }
    }
}