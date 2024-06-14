package com.venus_customer.amazonS3

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.amazonaws.mobileconnectors.s3.transferutility.*
//import androidx.camera.video.internal.utils.OutputUtil
import com.amazonaws.services.s3.model.CannedAccessControlList
import java.io.File
import java.util.*


class AmazonS3 {
    private var mActivity: Context? = null
    private var amazonCallback: AmazonS3Callbacks? = null
    private var mTransferUtility: TransferUtility? = null

    /**
     * Initializes Amazon S3
     * @param activity          Activity where callbacks are to sent
     * @param amazonCallback    Interface through which actions are implemented on the required activity
     */
    fun setCallback(activity: Activity, amazonCallback: AmazonS3Callbacks) {
        this.mActivity = activity
        this.amazonCallback = amazonCallback
    }

    fun setCallbacks(context : Context, amazonCallback: AmazonS3Callbacks){
        this.mActivity = context
        this.amazonCallback = amazonCallback
    }


    fun uploadPDF(imageBean: ImageBean) {
        val file: File = File(imageBean.imagePath)
        if (file.exists()) {
            mTransferUtility = AmazonS3Utils.getTransferUtility(mActivity!!)
            if (file.absolutePath.substring(file.absolutePath.lastIndexOf(".")) == ".pdf") {
                uploadFileOnAmazon(imageBean, file)
            }
        }
    }

    private fun uploadFileOnAmazon(imageBean: ImageBean, file: File) {
        val observer: TransferObserver
        val extension = file.absolutePath.substring(file.absolutePath.lastIndexOf("."))
        observer = mTransferUtility!!.upload(
            "BuildConfig.BUCKET",
            "android" + Calendar.getInstance().timeInMillis + extension,
            file,
            CannedAccessControlList.Private
        )
        observer.setTransferListener(UploadListener(imageBean))
        imageBean.mObserver = (observer)
    }

    /**
     * Uploads image to S3
     */
    fun uploadFile(imageBean: ImageBean) {
        if (imageBean.file?.exists() == true) {
            Log.e("VIDEO", "file exists")
            mTransferUtility = AmazonS3Utils.getTransferUtility(mActivity!!)
            val observer = mTransferUtility?.upload(
                "BuildConfig.BUCKET",
                /*imageBean.name + */"_" +Calendar.getInstance().timeInMillis.toString() + "." + imageBean.extension,
                imageBean.file,
                CannedAccessControlList.Private
            )
            observer?.setTransferListener(UploadListener(imageBean))
            imageBean.mObserver = (observer)

        } else {
            Log.e("AMZON_File", "file not exists")
        }
    }

    fun cancelAllUploads(){
        AmazonS3Utils.getTransferUtility(mActivity!!).cancelAllWithType(TransferType.UPLOAD)
    }

    private fun getFileExtension(uri: Uri): String? {
        val contentResolver: ContentResolver = mActivity?.contentResolver!!
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    private inner class UploadListener(private val imageBean: ImageBean) : TransferListener {

        // Simply updates the UI list when notified.
        override fun onError(id: Int, e: Exception) {
            imageBean.isSuccess = "0"
            amazonCallback?.uploadError(e, imageBean)
        }

        override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
            val progress = (bytesCurrent.toDouble() * 100 / bytesTotal).toInt()
            imageBean.progress = (progress)
            amazonCallback?.uploadProgress(imageBean)
        }

        override fun onStateChanged(id: Int, newState: TransferState) {
            if (newState == TransferState.COMPLETED) {
                imageBean.isSuccess = ("1")
                val url = "BuildConfig.AWS_DOWNLOAD_URL" + imageBean.mObserver?.key
                imageBean.serverUrl = (url)
                amazonCallback?.uploadSuccess(imageBean)
                Log.e("AWS URL","$url")
            } else if (newState == TransferState.FAILED) {
                imageBean.isSuccess = ("0")
                amazonCallback?.uploadFailed(imageBean)
            }
        }
    }

    fun removeCallBack() {
        amazonCallback = null
    }
}