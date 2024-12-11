package com.ujeff_customer.customClasses

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.ujeff_customer.R
import com.ujeff_customer.Splash

class FloatingOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var removeIconView: View
    private var isRemoveIconVisible = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Inflate the overlay layout
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100

        // Inflate the remove icon layout
        removeIconView = LayoutInflater.from(this).inflate(R.layout.remove_icon_layout, null)
        val removeParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        removeParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        removeParams.y = 100

        // Add the overlay and remove icon to the window
        windowManager.addView(overlayView, params)
        windowManager.addView(removeIconView, removeParams)

        // Initially hide the remove icon
        removeIconView.visibility = View.GONE

        // Handle touch events
        overlayView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        // Get the position of the remove icon on screen
                        val location = IntArray(2)
                        removeIconView.getLocationOnScreen(location)
                        val removeIconX = location[0]
                        val removeIconY = location[1]

                        // Get the bounds of the remove icon
                        val removeIconWidth = removeIconView.width
                        val removeIconHeight = removeIconView.height

                        // Check if the overlay is within the bounds of the remove icon
                        val isInRemoveArea = (params.x >= removeIconX && params.x <= removeIconX + removeIconWidth
                                && params.y >= removeIconY && params.y <= removeIconY + removeIconHeight)

                        if (isRemoveIconVisible && isInRemoveArea) {
                            stopSelf()
                        }

                        removeIconView.visibility = View.GONE
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()

                        windowManager.updateViewLayout(overlayView, params)

                        // Show the remove icon if the overlay is dragged downwards
                        if (params.y > removeParams.y - overlayView.height) {
                            removeIconView.visibility = View.VISIBLE
                            isRemoveIconVisible = true
                        } else {
                            removeIconView.visibility = View.GONE
                            isRemoveIconVisible = false
                        }
                        return true
                    }
                }
                return false
            }
        })

        // Handle overlay click to bring the app to the foreground
        overlayView.setOnClickListener {
            val intent = Intent(this, Splash::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) windowManager.removeView(overlayView)
        if (::removeIconView.isInitialized) windowManager.removeView(removeIconView)
    }
}
