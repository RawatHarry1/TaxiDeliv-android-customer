package com.mb_driver.util

import android.text.InputFilter
import android.text.Spanned

class NoSpaceInputFilter : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        // If source contains spaces, remove them
        if (source != null && source.contains(" ")) {
            return source.toString().replace(" ", "")
        }
        return null
    }
}
