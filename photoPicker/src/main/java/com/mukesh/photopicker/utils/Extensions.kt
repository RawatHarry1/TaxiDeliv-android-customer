package com.mukesh.photopicker.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.mukesh.photopicker.picker.PickerDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

internal val ViewGroup.layoutInflater
    get() = LayoutInflater.from(context)


fun FragmentActivity.pickerDialog(): PickerDialog =
    PickerDialog.Builder(this).run {
        create()
    }

fun Fragment.pickerDialog(): PickerDialog =
    PickerDialog.Builder(this).run {
        create()
    }


/**
 * After Getting Image
 * */
//fun Context.getMediaFilePathFor(uri: Uri): String {
//    contentResolver.query(uri, null, null, null, null).use { cursor ->
//        val nameIndex = cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//        cursor.moveToFirst()
//        val name = cursor.getString(nameIndex)
//
//        File(filesDir, name).run {
//            try {
//                contentResolver.openInputStream(uri).use { inputStream ->
//                    val outputStream = FileOutputStream(this)
//                    var read: Int
//                    val buffers = ByteArray(inputStream!!.available())
//                    while (inputStream.read(buffers).also { read = it } != -1) {
//                        outputStream.use {
//                            it.write(buffers, 0, read)
//                        }
//                    }
//                }
//            } catch (e: java.lang.Exception) {
//                e.printStackTrace()
//            }
//            return getCompressed(this@getMediaFilePathFor, path).absolutePath
//        }
//    }
//}

fun Context.getMediaFilePathFor(uri: Uri): String {
    contentResolver.query(uri, null, null, null, null).use { cursor ->
        if (cursor != null && cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val name = cursor.getString(nameIndex)

            val file = File(filesDir, name)
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        val buffer = ByteArray(4096)
                        var read: Int
                        while (inputStream.read(buffer).also { read = it } != -1) {
                            outputStream.write(buffer, 0, read)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return getCompressed(this, file.path).absolutePath
        } else {
            throw IllegalArgumentException("Failed to retrieve media file path for URI: $uri")
        }
    }
}


fun Context.getMediaFilePathForm(uri: Uri): String? {
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    val cursor = contentResolver.query(uri, projection, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            return it.getString(columnIndex)
        }
    }
    return null
}


/**
 * Check Permissions
 * */
var isAlertDialogShown = false // Add a boolean flag
fun Context.checkPermissions(vararg permission: String, returnData: (Boolean) -> Unit) = try {
    Dexter.withContext(this).withPermissions(*permission)
        .withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                report?.let {
                    if (report.areAllPermissionsGranted()) {
                        returnData(true)
                    } else if (report.isAnyPermissionPermanentlyDenied) {
//                        CoroutineScope(Dispatchers.Main).launch {
//                            if (!isAlertDialogShown){
//                                isAlertDialogShown = true
//                                AlertDialog.Builder(this@checkPermissions).apply {
//                                    setTitle("Salone Driver")
//                                    setCancelable(false)
//                                    setMessage("App need's permission. Please grant permission's.")
//                                    setPositiveButton("Grant") { dialog, _ ->
//                                        dialog.dismiss()
//                                        isAlertDialogShown = false
//                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
//                                            data = Uri.fromParts("package", "com.salonedriver", null)
//                                            startActivity(this)
//                                        }
//                                    }
//
//                                    show()
//                                    isAlertDialogShown = true // Set the flag
//                                }
//                            }
//                        }
                    }
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?, p1: PermissionToken?
            ) {
                p1?.continuePermissionRequest()
            }
        }).check()
} catch (e: Exception) {
    e.printStackTrace()
}