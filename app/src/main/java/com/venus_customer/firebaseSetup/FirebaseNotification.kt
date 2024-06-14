package com.venus_customer.firebaseSetup

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.venus_customer.R
import com.venus_customer.util.SharedPreferencesManager
import com.venus_customer.view.activity.walk_though.Home
import com.venus_customer.view.activity.walk_though.ui.home.HomeFragment
import kotlinx.parcelize.Parcelize
import org.json.JSONObject


@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FirebaseNotification : FirebaseMessagingService() {

    /**
     * Initialize Variables
     * */
    private val notificationData by lazy { NotificationData() }


    /**
     * On Message Received
     * */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification.let {
            notificationData.title = it?.title ?: getString(R.string.app_name)
            notificationData.message = it?.body ?: getString(R.string.app_name)
        }

        if (remoteMessage.data.isNotEmpty()) {
            remoteMessage.data.let { data ->
                if (data["title"].orEmpty().isNotEmpty()) notificationData.title =
                    data["title"].orEmpty()

                if (data["body"].orEmpty().isNotEmpty()) notificationData.message =
                    data["body"].orEmpty()

                notificationData.notificationType = data["notification_type"].orEmpty()
                fetchNotificationData(data)
            }
        }
        sendNotification()
    }


    /**
     * Fetch Notification Data
     * */
    private fun fetchNotificationData(data: MutableMap<String, String>) {
        try {
            notificationData.notificationType = data["notification_type"].toString()

            if (data.contains("notificationDetails")) {
                JSONObject(data["notificationDetails"].toString()).let { detail ->
                    notificationData.notificationModel.tripId = detail.optString("trip_id").toString()
                    notificationData.notificationModel.driverId = detail.optString("driver_id").toString()
                }
            }

            if ((data["notification_type"]?.toIntOrNull() ?: 0) == NotificationStatus.RIDE_ENDED.type){
                HomeFragment.notificationInterface?.rideEnd(notificationData.notificationModel.tripId.orEmpty(), notificationData.notificationModel.driverId.orEmpty())
            } else if ((notificationData.notificationType?.toIntOrNull() ?: 0) == NotificationStatus.RIDE_ACCEPTED.type) {
                HomeFragment.notificationInterface?.acceptRide()
            } else {
                HomeFragment.notificationInterface?.callFetchRideApi()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * Send Notifications
     * */
    private fun sendNotification() {

        val notificationBuilder = NotificationCompat.Builder(this, packageName)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(notificationData.title ?: "").setContentText(notificationData.message)
            .setAutoCancel(true).setSilent(false).setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(
                when (notificationData.notificationType?.toIntOrNull() ?: -1) {
                    NotificationStatus.WALLET_UPDATE.type -> getPendingIntent(destinationId = R.id.navigation_wallet)
                    NotificationStatus.RIDE_ENDED.type -> getPendingIntent(destinationId = R.id.navigation_ride_details, bundleOf(
                        "tripId" to notificationData.notificationModel.tripId.orEmpty(),
                        "driverId" to notificationData.notificationModel.driverId.orEmpty()
                    ))
                    else -> getPendingIntent(destinationId = R.id.mobile_navigation)
                }
            )


        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                packageName, notificationData.title, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = notificationData.message
                setBypassDnd(true)
                enableVibration(true)
                setShowBadge(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setAllowBubbles(true)
                }
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }


    /**
     * Pending Intent Handle
     * */
    private fun getPendingIntent(destinationId: Int, bundle: Bundle? = null): PendingIntent =
        if (Build.VERSION.SDK_INT >= 31) NavDeepLinkBuilder(this).setComponentName(Home::class.java)
            .setGraph(R.navigation.mobile_navigation).setDestination(destinationId)
            .setArguments(bundle).createTaskStackBuilder()
            .getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)!!
        else NavDeepLinkBuilder(this).setComponentName(Home::class.java)
            .setGraph(R.navigation.mobile_navigation).setDestination(destinationId)
            .setArguments(bundle).createPendingIntent()


    /**
     * Notification Data
     * */
    @Parcelize
    data class NotificationData(
        var title: String? = "",
        var message: String? = "",
        var notificationType: String? = "",
        var notificationModel: NotificationModel = NotificationModel()
    ) : Parcelable

}