package com.venus_customer

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.maps.model.LatLng
import com.venus_customer.di.AppComponent
import com.venus_customer.di.DaggerAppComponent
import com.venus_customer.socketSetup.SocketSetup
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp

class VenusApp: Application() {
    companion object {
        lateinit var instance: VenusApp
        lateinit var appContext: Context
        lateinit var applicationComponent: AppComponent
        var latLng: LatLng = LatLng(0.0, 0.0)
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

//        SocketSetup.connectSocket()

    }

    override fun onLowMemory() {
        super.onLowMemory()
//        System.gc()
    }
}