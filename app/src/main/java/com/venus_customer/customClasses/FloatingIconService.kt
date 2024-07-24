package com.venus_customer.customClasses

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import com.venus_customer.R

class FloatingIconService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private var xOffset = 0
    private var yOffset = 0
    private var initialX = 0
    private var initialY = 0
    private var isDragging = false

    override fun onCreate() {
        super.onCreate()
        Log.d("FloatingIconService", "Service Created")

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.layout_floating_icon, null)
        val button = overlayView.findViewById<ImageView>(R.id.floating_icon)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = xOffset
        params.y = yOffset

        button.setOnClickListener {
            // Handle button click here
            Toast.makeText(this, "Overlay button clicked!", Toast.LENGTH_SHORT).show()
            bringAppToForeground()
        }

        button.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.rawX.toInt()
                    initialY = event.rawY.toInt()
                    isDragging = false
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX.toInt() - initialX
                    val deltaY = event.rawY.toInt() - initialY
                    params.x = params.x + deltaX
                    params.y = params.y + deltaY

                    // Prevent the overlay view from moving off the screen
                    if (params.x < 0) params.x = 0
                    if (params.y < 0) params.y = 0
                    if (params.x > windowManager.defaultDisplay.width - overlayView.width) params.x =
                        windowManager.defaultDisplay.width - overlayView.width
                    if (params.y > windowManager.defaultDisplay.height - overlayView.height) params.y =
                        windowManager.defaultDisplay.height - overlayView.height

                    windowManager.updateViewLayout(overlayView, params)
                    initialX = event.rawX.toInt()
                    initialY = event.rawY.toInt()
                    isDragging = true
                    true
                }

                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        // Handle button click here
                        Toast.makeText(this, "Overlay button clicked!", Toast.LENGTH_SHORT).show()
                        bringAppToForeground()
                    }
                    false
                }

                else -> false
            }
        }

        windowManager.addView(overlayView, params)
        Log.d("FloatingIconService", "Overlay view added")
    }

    private fun bringAppToForeground() {
        sendBroadcast(Intent("bring_app_to_foreground"))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) windowManager.removeView(overlayView)
        Log.d("FloatingIconService", "Service Destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
