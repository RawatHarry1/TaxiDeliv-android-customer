package com.mukesh.photopicker.picker

import com.mukesh.photopicker.utils.ItemType

fun interface OnPickerCloseListener {
    fun onPickerClosed(type: ItemType, uris: String)
}
