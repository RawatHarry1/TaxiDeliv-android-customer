package com.mukesh.photopicker.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


//fun getCompressed(context: Context?, path: String?): File {
//    if (context == null) throw NullPointerException("Context must not be null.")
//    if (path == null) throw NullPointerException("Path must not be null.")
//    var cacheDir = context.externalCacheDir
//    if (cacheDir == null) //fall back
//        cacheDir = context.cacheDir
//    val rootDir = cacheDir!!.absolutePath + "/ImageCompressor"
//    val root = File(rootDir)
//    if (!root.exists()) root.mkdirs()
//    var bitmap = decodeImageFromFiles(path, 512, 512)
//    val compressed = File(root, System.currentTimeMillis().toString().plus(".png"))
//    val byteArrayOutputStream = ByteArrayOutputStream()
//    bitmap = rotateImageIfRequired(Uri.parse(path), bitmap)
//    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
//    val fileOutputStream = FileOutputStream(compressed)
//    fileOutputStream.write(byteArrayOutputStream.toByteArray())
//    fileOutputStream.flush()
//    fileOutputStream.close()
//    return compressed
//}


fun getCompressed(context: Context?, path: String?): File {
    if (context == null) throw NullPointerException("Context must not be null.")
    if (path == null) throw NullPointerException("Path must not be null.")
//    val pathDirectory = Environment.getExternalStoragePublicDirectory(
//            Environment.DIRECTORY_PICTURES)
    var cacheDir = context.externalCacheDir ?: context.cacheDir
    val rootDir = "${cacheDir.absolutePath}/ImageCompressor"
    val root = File(rootDir)
    if (!root.exists()) root.mkdirs()

    val bitmap = decodeImageFromFiles(path, 512, 512)
    val compressed = File(root, "${System.currentTimeMillis()}.png")

    val byteArrayOutputStream = ByteArrayOutputStream()
    val rotatedBitmap = rotateImageIfRequired(Uri.parse(path), bitmap)
    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)

    FileOutputStream(compressed).use { fileOutputStream ->
        fileOutputStream.write(byteArrayOutputStream.toByteArray())
        fileOutputStream.flush()
    }

    return compressed
}


/**
 * Decode Image From Files
 * */
private fun decodeImageFromFiles(path: String?, width: Int, height: Int): Bitmap {
    val scaleOptions = BitmapFactory.Options()
    scaleOptions.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, scaleOptions)
    var scale = 1
    while (scaleOptions.outWidth / scale / 2 >= width
        && scaleOptions.outHeight / scale / 2 >= height
    ) {
        scale *= 2
    }
    val outOptions = BitmapFactory.Options()
    outOptions.inSampleSize = scale
    return BitmapFactory.decodeFile(path, outOptions)
}


/**
 * Rotate Image If Requires
 * */
private fun rotateImageIfRequired(
    selectedImage: Uri,
    img: Bitmap,
): Bitmap {
    val ei = ExifInterface(selectedImage.path ?: "")
    val orientation: Int = ei.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_UNDEFINED
    )
    var rotatedBitmap: Bitmap = img
    when (orientation) {
        ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = rotateImage(img, 0)
        ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = rotateImage(img, 90)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = rotateImage(img, 180)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = rotateImage(img, 270)
    }
    return rotatedBitmap
}


/**
 * Rotate Image
 * */
private fun rotateImage(bitmap: Bitmap, degrees: Int): Bitmap =
    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply {
        postRotate(degrees.toFloat())
    }, true)
