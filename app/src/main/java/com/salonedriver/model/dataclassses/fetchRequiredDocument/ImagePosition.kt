package com.salonedriver.model.dataclassses.fetchRequiredDocument


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImagePosition(
    @SerializedName("img_position")
    val imgPosition: Int? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("imageUrl")
    var imageUrl: String? = null
) : Parcelable