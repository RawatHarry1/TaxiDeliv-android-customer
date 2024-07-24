package com.venus_customer.customClasses

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BringAppToForegroundReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("FloatingIconService", "ON BROADCAST")
        val packageName = context.packageName
        val intent1 = context.packageManager.getLaunchIntentForPackage(packageName)
        intent1?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // Check if the app is already running
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val tasks = activityManager.getRunningTasks(1)
        if (tasks.isNotEmpty() && tasks[0].topActivity?.packageName == packageName) {
            // App is already running, bring it to the foreground
            activityManager.moveTaskToFront(tasks[0].id, 0)
        } else {
            // App is not running, start the activity
            context.startActivity(intent1)
        }
    }
}