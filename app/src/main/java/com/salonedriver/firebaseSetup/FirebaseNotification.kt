package com.salonedriver.firebaseSetup

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.salonedriver.R
import com.salonedriver.SaloneDriver
import com.salonedriver.model.dataclassses.userData.UserDataDC
import com.salonedriver.util.SharedPreferencesManager
import com.salonedriver.view.fragment.wallet.WalletFragment
import com.salonedriver.view.ui.home_drawer.HomeActivity
import com.salonedriver.view.ui.home_drawer.ui.home.HomeFragment
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import kotlin.random.Random


@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FirebaseNotification : FirebaseMessagingService() {
    companion object {
        const val ACTION_STOP_MEDIA = "com.salonedriver.firebaseSetup.ACTION_STOP_MEDIA"
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            registerReceiver(
                stopMediaReceiver,
                IntentFilter(ACTION_STOP_MEDIA),
                RECEIVER_NOT_EXPORTED
            )
        else
            registerReceiver(stopMediaReceiver, IntentFilter(ACTION_STOP_MEDIA))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(stopMediaReceiver)
    }

    private val stopMediaReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_STOP_MEDIA) {
                Log.i("PUSHNOTI","IN stop media receiver")
                releaseMediaPlayer()
            }
        }
    }

    /**
     * Initialize Variables
     * */
    private val notificationData by lazy { NotificationData() }


    /**
     * On Message Received
     * */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.i("PUSHNOTI", Gson().toJson(remoteMessage.data))
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

        try {
            when {
                (notificationData.notificationType?.toIntOrNull()
                    ?: -1) == NotificationStatus.NEW_RIDE.type -> {
                    HomeFragment.notificationInterface?.newRide()
                        ?: kotlin.run {
//                            sendNotification()
                            playCustomSound()
                        }
                }

                (notificationData.notificationType?.toIntOrNull()
                    ?: -1) == NotificationStatus.TIME_OUT_RIDE.type -> {
                    HomeFragment.notificationInterface?.timeOutRide()
                }

                (notificationData.notificationType?.toIntOrNull()
                    ?: -1) == NotificationStatus.WALLET_UPDATE.type -> {
                    WalletFragment.notificationInterface?.walletUpdate()
                    HomeFragment.notificationInterface?.walletUpdate()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        //Check User Login
        SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
            ?.let {
                if (it.accessToken?.isNotEmpty() == true) {
                    if (notificationData.notificationType == "600" && SaloneDriver.onChatScreen) {
                        Log.i("PUSHNOTI", "on chat screen")
                    } else if ((notificationData.notificationType?.toIntOrNull()
                            ?: -1) == NotificationStatus.TIME_OUT_RIDE.type
                    ) {
                        Log.i("PUSHNOTI", "on type 2")
                    } else
                        sendNotification()
                    if ((notificationData.notificationType?.toIntOrNull()
                            ?: -1) == 0
                    )
                        playCustomSound()
                }
            }

    }


    /**
     * Fetch Notification Data
     * */
    private fun fetchNotificationData(data: MutableMap<String, String>) {
        try {
            val jsonData = JSONObject(data["notificationDetails"].orEmpty())
            Log.i("PUSHNOTI", "in notification detail")
            when (notificationData.notificationType?.toIntOrNull() ?: -1) {
                NotificationStatus.NEW_RIDE.type -> {
                    NewRideNotificationDC(
                        customerName = jsonData.optString("customer_name"),
                        customerImage = jsonData.optString("customer_image"),
                        customerId = jsonData.optString("customer_id"),
                        pickUpAddress = jsonData.optString("pickup_address"),
                        latitude = jsonData.optString("latitude"),
                        tripId = jsonData.optString("trip_id"),
                        estimatedDriverFare = jsonData.optString("estimated_driver_fare"),
                        longitude = jsonData.optString("longitude"),
                        currency = jsonData.optString("currency"),
                        estimatedDistance = jsonData.optString("estimated_distance"),
                        dropAddress = jsonData.optString("drop_address"),
                        dryEta = jsonData.optString("dry_eta"),
                        date = jsonData.optString("date"),
                        customerNote = jsonData.optString("customer_notes"),
                        distanceUnit = jsonData.optString("distanceUnit"),
                        userPhoneNo = jsonData.optString("user_phone_no")
                    ).apply {
                        SharedPreferencesManager.putModel(
                            SharedPreferencesManager.Keys.NEW_BOOKING, this
                        )
                    }
                }

                NotificationStatus.TIME_OUT_RIDE.type -> {
                    SharedPreferencesManager.clearKeyData(SharedPreferencesManager.Keys.NEW_BOOKING)
                }
            }
        } catch (e: Exception) {
            Log.i("PUSHNOTI", "in notification detail error :: ${e.message}")
            e.printStackTrace()
        }
    }


    /**
     * Send Notifications
     * */
    private fun sendNotification() {
        // Define vibration patterns

        val hardVibrationPattern = longArrayOf(0, 1000, 500, 1000) // Hard vibration pattern
        val normalVibrationPattern = longArrayOf(0, 500, 250, 500) // Normal vibration pattern
        val notificationType = notificationData.notificationType?.toIntOrNull() ?: -1
        val soundUri =
            Uri.parse("android.resource://" + packageName + "/" + R.raw.alert_for_new_ride)
        // Create the notification channel ID based on the notification type
//        val channelId = if (notificationType == 0) "new_ride" else packageName
        val channelId = packageName
        val notificationBuilder = NotificationCompat.Builder(this, packageName)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(notificationData.title ?: "").setContentText(notificationData.message)
            .setAutoCancel(true).setSilent(false).setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setSound(
//                if (notificationType == 0) {
//                    soundUri
//                } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//                }
            )
            .setVibrate(
                if (notificationType == 0
                ) hardVibrationPattern else normalVibrationPattern
            )
            .setContentIntent(
                when (notificationData.notificationType?.toIntOrNull() ?: -1) {
                    NotificationStatus.WALLET_UPDATE.type -> {
                        Log.i("PUSHNOTI", "in wallet")
                        getPendingIntent(destinationId = R.id.wallet)
                    }

                    NotificationStatus.CHAT.type -> {
                        sendBroadcast(Intent("newMsg"))
                        getPendingIntent(
                            destinationId = R.id.nav_home,
                            bundle = Bundle().apply { putString("notification_type", "600") })
                    }

                    else -> {
                        Log.i("PUSHNOTI", "in home")
                        getPendingIntent(destinationId = R.id.nav_home)
                    }
                }
            )


        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            if (notificationType == 0) {
//
//                val sirenChannel = NotificationChannel(
//                    channelId, "new_ride", NotificationManager.IMPORTANCE_HIGH
//                ).apply {
//                    description = notificationData.message
//                    setBypassDnd(true)
//                    enableVibration(true)
//                    vibrationPattern = hardVibrationPattern
//                    setShowBadge(true)
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                        setAllowBubbles(true)
//                    }
//                    setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
//
////                    setSound(
////                        soundUri,
////                        AudioAttributes.Builder()
////                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
////                            .setUsage(AudioAttributes.USAGE_ALARM)
////                            .build()
////                    )
//                }
//                notificationManager.createNotificationChannel(sirenChannel)
//            } else {
            val channel = NotificationChannel(
                channelId, packageName, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = notificationData.message
                setBypassDnd(true)
                enableVibration(true)
                setShowBadge(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setAllowBubbles(true)
                }
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
            }
            notificationManager.createNotificationChannel(channel)
//            }
        }
        val id = Random.nextInt(100000, 999999)
        notificationManager.notify(id, notificationBuilder.build())
    }


    /**
     * Pending Intent Handle
     * */
    private fun getPendingIntent(destinationId: Int, bundle: Bundle? = null): PendingIntent =
//        if (Build.VERSION.SDK_INT >= 31)
//            NavDeepLinkBuilder(this)
//                .setComponentName(HomeActivity::class.java)
//                .setGraph(R.navigation.mobile_navigation)
//                .setDestination(destinationId)
//                .setArguments(bundle)
//                .createTaskStackBuilder()
//                .getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)!!
//        else
        NavDeepLinkBuilder(this)
            .setComponentName(HomeActivity::class.java)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(destinationId)
            .setArguments(bundle).createPendingIntent()

    private fun createPendingIntent(destinationId: Int, bundle: Bundle? = null): PendingIntent? {
        val navDeepLinkBuilder = NavDeepLinkBuilder(this)
            .setComponentName(HomeActivity::class.java)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(destinationId)

        if (bundle != null) {
            Log.d("NotificationRedirection", "Bundle is not null, setting arguments: $bundle")
            navDeepLinkBuilder.setArguments(bundle)
        } else {
            Log.d("NotificationRedirection", "Bundle is null, not setting arguments.")
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            navDeepLinkBuilder.createTaskStackBuilder()
                .getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
                .also { pendingIntent ->
                    if (pendingIntent == null) {
                        Log.e(
                            "NotificationRedirection",
                            "Failed to create PendingIntent on Android 31+"
                        )
                    } else {
                        Log.d(
                            "NotificationRedirection",
                            "Successfully created PendingIntent on Android 31+"
                        )
                    }
                }
        } else {
            navDeepLinkBuilder.createPendingIntent()
                .also { pendingIntent ->
                    if (pendingIntent == null) {
                        Log.e(
                            "NotificationRedirection",
                            "Failed to create PendingIntent on Android < 31"
                        )
                    } else {
                        Log.d(
                            "NotificationRedirection",
                            "Successfully created PendingIntent on Android < 31"
                        )
                    }
                }
        }
    }

    /**
     * Notification Data
     * */
    @Parcelize
    data class NotificationData(
        var title: String? = "",
        var message: String? = "",
        var notificationType: String? = "",
    ) : Parcelable

    private var mediaPlayer: MediaPlayer? = null
    private fun playCustomSound() {
        try {
            // Release previous media player instance
            releaseMediaPlayer()
            // Create and configure the MediaPlayer instance with the custom sound file
            mediaPlayer = MediaPlayer.create(this, R.raw.ride_accept).apply {
                start()
                setOnCompletionListener {
                    releaseMediaPlayer()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playCustomSoundInLoop() {
        try {
            // Release previous media player instance
            releaseMediaPlayer()

            // Request audio focus and set the volume to a desired level
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
            val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setWillPauseWhenDucked(false)
                .setOnAudioFocusChangeListener { }
                .build()

            audioManager.requestAudioFocus(audioFocusRequest)


            // Create and configure the MediaPlayer instance with the custom sound file
            mediaPlayer = MediaPlayer.create(this, R.raw.ride_accept).apply {
                isLooping = true
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.run {
            if (isPlaying) stop()
            reset()
            release()
        }
        mediaPlayer = null
    }
}