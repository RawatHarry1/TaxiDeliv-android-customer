package com.salonedriver.util

import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.salonedriver.SaloneDriver.Companion.appContext
import java.util.Locale

object ResourceUtils {
    fun getColor(colorResId: Int): Int {
        return ContextCompat.getColor(appContext, colorResId)
    }

    fun getDrawable(drawableResId: Int): Drawable? {
        return ContextCompat.getDrawable(appContext, drawableResId)
    }

    fun getString(resId: Int): CharSequence {
        return appContext.getString(resId, Locale.getDefault().language)
    }
}