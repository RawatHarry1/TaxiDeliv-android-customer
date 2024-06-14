package com.mukesh.photopicker.utils

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class ItemModel(
    val type: ItemType,
    val itemLabel: String = "",
    @DrawableRes
    val itemIcon: Int = 0,
    @ColorInt val itemBackgroundColor: Int = 0,
) : Parcelable
