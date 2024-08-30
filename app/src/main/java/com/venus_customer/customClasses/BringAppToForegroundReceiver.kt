package com.venus_customer.customClasses

import android.app.ActivityManager
import android.app.TaskInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.venus_customer.view.activity.walk_though.Home

class BringAppToForegroundReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("FloatingIconService", "ON BROADCAST")

//        val packageName = context.packageName
//        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)?.apply {
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        }
//        Log.d("FloatingIconService", "Task app name $packageName")
//
//        // Check if the app is already running
//        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        val runningTasks = activityManager.getRunningTasks(Int.MAX_VALUE)
//        var isTaskMoved = false
//
//        runningTasks.forEach { taskInfo ->
//            Log.d("FloatingIconService", "Task getting from system ${taskInfo.topActivity?.packageName}")
//            if (taskInfo.topActivity?.packageName == packageName) {
//                Log.d("FloatingIconService", "inside if taskInfo ${taskInfo.topActivity?.packageName}")
//                activityManager.moveTaskToFront(taskInfo.id, ActivityManager.MOVE_TASK_WITH_HOME)
//                isTaskMoved = true
//                return
//            }
//        }
//
//        if (!isTaskMoved) {
//            Log.d("FloatingIconService", "Launching app as it is not running")
//            context.startActivity(launchIntent)
//        }

        val packageName = context.packageName
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        Log.d("FloatingIconService", "Task app name $packageName")

     // Check if the app is already running
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        var isTaskMoved = false
        val tasks = activityManager.appTasks
        tasks.forEach { task ->
            if (task.taskInfo.topActivity?.packageName == packageName) {
                Log.d("FloatingIconService", "inside if taskInfo ${task.taskInfo.topActivity?.packageName}")
                task.moveToFront()
                isTaskMoved = true
                return
            }
        }

        if (!isTaskMoved) {
            Log.d("FloatingIconService", "Launching app as it is not running")
            context.startActivity(launchIntent)
        }
    }
}