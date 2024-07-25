package com.venus_customer.customClasses

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.venus_customer.view.activity.walk_though.Home

class BringAppToForegroundReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("FloatingIconService", "ON BROADCAST")
        val packageName = context.packageName
        val intent1 = context.packageManager.getLaunchIntentForPackage(packageName)
        intent1?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        Log.d("FloatingIconService", "Task app name $packageName")
        // Check if the app is already running
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        val runningProcesses = activityManager.runningAppProcesses
        var isAppRunning = false
        runningProcesses.forEach { processInfo ->
            if (processInfo.processName == packageName) {
                isAppRunning = true
            }
        }

        if (isAppRunning) {
            val tasks = activityManager.getRunningTasks(Int.MAX_VALUE)
            tasks.forEach { taskInfo ->
                Log.d("FloatingIconService", "Task getting from system ${taskInfo.topActivity?.packageName}")
                if (taskInfo.topActivity?.packageName == packageName) {
                    activityManager.moveTaskToFront(taskInfo.id, 0)
                    return
                }
            }
        } else {
            context.startActivity(intent1)
        }
    }
}