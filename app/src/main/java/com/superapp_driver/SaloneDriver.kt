package com.superapp_driver

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.maps.model.LatLng
import com.superapp_driver.di.AppComponent
import com.superapp_driver.di.DaggerAppComponent
import com.superapp_driver.socketSetup.SocketSetup
import com.superapp_driver.util.AppLifecycleObserver
import com.superapp_driver.util.getFCMToken
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SaloneDriver : Application(), ActivityLifecycleCallbacks {
    companion object {
        lateinit var instance: SaloneDriver
        lateinit var appContext: Context
        lateinit var applicationComponent: AppComponent
        @JvmStatic
        var latLng: LatLng? = null
        var onChatScreen = false
        var googleMapKey = ""
    }

    override fun onCreate() {
        super.onCreate()
        SocketSetup.connectSocket()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        instance = this
        applicationComponent = DaggerAppComponent.builder().build()
        registerActivityLifecycleCallbacks(this)
        // Register the process lifecycle observer
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver)
        getFCMToken {
            Log.e("TokenIS", "Here:- $it")
        }
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Log.e("UncaughtException", "Thread: $thread, Exception: $exception", exception)
        }

    }


    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        try {
            appContext = p0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityStarted(p0: Activity) = Unit

    override fun onActivityResumed(p0: Activity) {
        try {
            appContext = p0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityPaused(p0: Activity) = Unit

    override fun onActivityStopped(p0: Activity) = Unit

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) = Unit

    override fun onActivityDestroyed(p0: Activity) = Unit

    override fun onLowMemory() {
        super.onLowMemory()
        System.gc()
    }
}