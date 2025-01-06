package com.mcabs_driver.util

import android.os.Handler
import android.os.Looper

class DelayHandler {
    var handler: Handler? = null
    var runable: Runnable? = null

    init {
        handler = Handler(Looper.getMainLooper())
    }

    fun startDelay(sec: Long, listner: () -> Unit) {

        runable = Runnable { listner.invoke() }
        handler?.postDelayed(runable!!, sec)
    }

    fun cancelHandler() {
        if (handler != null) {
            runable?.let { handler?.removeCallbacks(it) }
            handler?.removeCallbacksAndMessages(null)
        }
    }
}