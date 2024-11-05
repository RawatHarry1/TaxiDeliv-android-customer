package com.superapp_customer

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.maps.model.LatLng
import com.superapp_customer.di.AppComponent
import com.superapp_customer.di.DaggerAppComponent
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
    }

    override fun onCreate() {
        super.onCreate()
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
}