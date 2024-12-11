package com.ujeff_customer.amazonS3

import android.net.Uri
import android.os.Parcelable
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.io.File

@Parcelize
class ImageBean(
    var localUrl: String? = null,
    var file: File? = null,
    var name: String? = null,
    var progress: Int = 0,
    var position: Int = 0,
    var extension: String = "jpg",
    var mObserver: @RawValue TransferObserver? = null,
    var serverUrl: String = "",
    var isSuccess: String = "0",
    var imagePath: String = "",
    var placeHolderName: String = "",
    var id: Int? = null,
    var requestKey: String? = null,
    var isVideo: Boolean = false,
    var uri: Uri? = null,
    var tag: String? = null

) : Parcelable