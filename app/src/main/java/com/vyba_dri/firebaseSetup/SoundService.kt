package com.vyba_dri.firebaseSetup

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.vyba_dri.R
import com.vyba_dri.view.ui.home_drawer.HomeActivity


class SoundService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var notification: Notification
    private lateinit var notificationManager: NotificationManager
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Create a notification channel
        val channel = NotificationChannel(
            "sound_channel",
            "Sound Service",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        Log.i("PUSHNOTI", "Sound Service onStartCommand")
        notification = NotificationCompat.Builder(this, "sound_channel")
            .setContentTitle(intent.getStringExtra("title") ?: "You have 1 new ride request")
            .setContentText(intent.getStringExtra("message") ?: "You have 1 new ride request")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Set notification priority to high
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Ensure visibility
            .setContentIntent(getPendingIntent(destinationId = R.id.nav_home))
            .build()
        // Start the service in the foreground
        startForeground(1, notification)
        playCustomSoundInLoop()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.i("PUSHNOTI", "Sound Service onDestroy")
        releaseMediaPlayer()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun playCustomSoundInLoop() {
        try {
            Log.i("PLAYSOUND", "in try")
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
        }

        catch (e: Exception) {
            Log.i("PLAYSOUND", "${e.message}")
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

    private fun getPendingIntent(destinationId: Int, bundle: Bundle? = null): PendingIntent =
        NavDeepLinkBuilder(this)
            .setComponentName(HomeActivity::class.java)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(destinationId)
            .setArguments(bundle).createPendingIntent()
}