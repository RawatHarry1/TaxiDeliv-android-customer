package com.mb_driver.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Insets
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.SettingsClient
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.messaging.FirebaseMessaging
import com.mb_driver.SaloneDriver
import com.mb_driver.databinding.HomePageAlertBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DateFormat
import java.text.ParseException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale


object AppUtils {
    const val DATE_yyyy_MM_dd_T_HH_mm_ss = "yyyy-MM-dd'T'HH:mm:ss"
    var tripId = ""






    fun isAppRunning(context: Context, packageName: String): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = activityManager.runningAppProcesses
        for (processInfo in runningProcesses) {
            if (processInfo.processName == packageName) {
                return true
            }
        }
        return false
    }

    fun isGPSEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    //    fun promptEnableGPS(context: Context) {
//        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//        context.startActivity(intent)
//    }
    fun checkAndEnableGPS(
        activity: Activity,
        onGPSEnabled: () -> Unit,
        onGPSDenied: () -> Unit,
        gpsEnableLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPS is not enabled, prompt the user to enable it
            promptEnableGPS(activity, onGPSEnabled, onGPSDenied, gpsEnableLauncher)
        } else {
            // GPS is already enabled
//            Toast.makeText(activity, "GPS is already enabled", Toast.LENGTH_SHORT).show()
            onGPSEnabled()
        }
    }

    fun formatStringTwoDecimal(double: Double): String {
        var s = ""
        try {
            s = String.format("%.2f", double)
        } catch (e: Exception) {
            s = double.toString()
        }
        return s
    }

    fun formatStringOneDecimal(double: Double): String {
        var s = ""
        try {
            s = String.format("%.1f", double)
        } catch (e: Exception) {
            s = double.toString()
        }
        return s
    }


    private fun promptEnableGPS(
        activity: Activity,
        onGPSEnabled: () -> Unit,
        onGPSDenied: () -> Unit,
        gpsEnableLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val settingsClient: SettingsClient = LocationServices.getSettingsClient(activity)
        val task = settingsClient.checkLocationSettings(builder.build())

        task.addOnSuccessListener { response ->
            val states = response.locationSettingsStates
            if (states?.isLocationPresent == true) {
//                Toast.makeText(activity, "GPS is enabled", Toast.LENGTH_SHORT).show()
                onGPSEnabled()
            }
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(exception.resolution).build()
                    gpsEnableLauncher.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    onGPSDenied()
                }
            } else {
                onGPSDenied()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun currentUtcTimeAsString(): String {
        val currentUtc = LocalDateTime.now(ZoneOffset.UTC)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        return currentUtc.format(formatter)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun convertUtcToLocal(utcTimeString: String): String {
        val formatterUtc = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val utcDateTime = LocalDateTime.parse(utcTimeString, formatterUtc)

        val localZoneId = ZoneId.systemDefault() // Or specify a specific zone ID if needed
        val localDateTime = utcDateTime.atZone(ZoneOffset.UTC).withZoneSameInstant(localZoneId).toLocalDateTime()

        val formatterLocal = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
        return localDateTime.format(formatterLocal)
    }

    fun convertUtcToLocalDate(utcTimeString: String): String {
        val formatterUtc = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val utcDateTime = LocalDateTime.parse(utcTimeString, formatterUtc)

        val localZoneId = ZoneId.systemDefault() // Or specify a specific zone ID if needed
        val localDateTime =
            utcDateTime.atZone(ZoneOffset.UTC).withZoneSameInstant(localZoneId).toLocalDateTime()

        val formatterLocal = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
        return localDateTime.format(formatterLocal)
    }

    fun hasScreenNavigation(activity: Activity): Boolean {
        val hasSoftwareKeys: Boolean
        val d = activity.windowManager.defaultDisplay
        val realDisplayMetrics = DisplayMetrics()
        d.getRealMetrics(realDisplayMetrics)
        val realHeight = realDisplayMetrics.heightPixels
        val realWidth = realDisplayMetrics.widthPixels
        val displayMetrics = DisplayMetrics()
        d.getMetrics(displayMetrics)
        val displayHeight = displayMetrics.heightPixels
        val displayWidth = displayMetrics.widthPixels
        hasSoftwareKeys = (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0

        return hasSoftwareKeys
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(): String {
        return Settings.Secure.getString(
            SaloneDriver.instance.contentResolver, Settings.Secure.ANDROID_ID
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun isInternetAvailable(context: Context? = SaloneDriver.instance.applicationContext): Boolean {
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false

        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            //for other device how are able to connect with Ethernet
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        val cursor: Cursor?
        var filePath: String? = ""
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            if (cursor == null) return contentUri.path
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            if (cursor.moveToFirst()) filePath = cursor.getString(columnIndex)
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
            filePath = contentUri.path
        }

        return filePath
    }

    fun imageCameraIntent(mImageURI: Uri?, mContext: Context): Intent? {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(mContext.packageManager) != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageURI)
            takePictureIntent.putExtra("return-data", true)
            takePictureIntent.putExtra("android.intent.extra.quickCapture", true)
            return takePictureIntent
        }
        return null
    }

    fun getScreenWidth(activity: Activity): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets: Insets =
                windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }

    fun getScreenHight(activity: Activity): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = activity.windowManager.currentWindowMetrics
            val insets = metrics.windowInsets.getInsets(WindowInsets.Type.systemBars())
            metrics.bounds.height() - insets.bottom - insets.top
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getHoursFromMinutes(minutes: Int?): String {
        val timeZone: TimeZone = TimeZone.getTimeZone("UTC")
        val df = SimpleDateFormat("HH'h' mm'min' ss's'")
        df.timeZone = timeZone

        return df.format(Date(minutes!! * 60 * 1000L))
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getOnlyHourFromMinutes(minutes: Int): String {
        val timeZone: TimeZone = TimeZone.getTimeZone("UTC")
        val df = SimpleDateFormat("HH")
        df.timeZone = timeZone

        return df.format(Date(minutes * 60 * 1000L))
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getOnlyMinuteFromMinutes(minutes: Int): String {
        val timeZone: TimeZone = TimeZone.getTimeZone("UTC")
        val df = SimpleDateFormat("mm")
        df.timeZone = timeZone

        return df.format(Date(minutes * 60 * 1000L))
    }

    fun getKmfromMeter(meter: Float): String {
        val km = meter / 1000
        return String.format("%.1f", km)
    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun getDateFromDateISO(selectDate: String?, format: String): String? {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var date: Date? = null
        try {
            date = sdf.parse(selectDate)
            val formatter = SimpleDateFormat(format)
            val newFormat = formatter.format(date)
            return newFormat
        } catch (ex: Exception) {
            Log.e("Exception", "" + ex.message)
        }
        return "-"
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getDateFromServerDate(selectDate: String?, format: String): String? {
        val sdf = SimpleDateFormat("YYYY-MM-dd HH:mm:ss")
        var date: Date? = null
        try {
            date = sdf.parse(selectDate)
            val formatter = SimpleDateFormat(format)
            val newFormat = formatter.format(date)
            return newFormat
        } catch (ex: Exception) {
            Log.e("Exception", "" + ex.message)
        }
        return "-"
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getFilterDate(selectDate: Date?, format: String): String? {
        try {
            val formatter = SimpleDateFormat(format)
            val newFormat = formatter.format(selectDate)
            return newFormat
        } catch (ex: Exception) {
            Log.e("Exception", "" + ex.message)
        }
        return "-"
    }

    fun getLeanAngle(angle: String): String {
        val angleText = SpannableStringBuilder()

        val arrayStr = angle.split(",")
        val first = arrayStr.first()
        val last = arrayStr.last()

        angleText.append(first)
        angleText.append("°L ")
        angleText.append(last)
        angleText.append("°R")

        return angleText.toString()
    }

    /*fun setGForceTypeface(
        context: Context,
        stringValue: String,
        startPoint: Int
    ): Spannable {
        val spannable = SpannableString(stringValue)
        if (stringValue.isNotEmpty()) {
            val typefaceLight = ResourcesCompat.getFont(context, R.font.bevietnampro_regular)
            val foregroundColorSpan = ForegroundColorSpan(ContextCompat.getColor(context,R.color.black_B1B1B1))
            spannable.setSpan(
                TypefaceSpan(typefaceLight!!),
                (stringValue.length - startPoint),
                stringValue.length,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
            spannable.setSpan(
                foregroundColorSpan,
                (stringValue.length - startPoint),
                stringValue.length,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
            spannable.setSpan(
                RelativeSizeSpan(0.8f),
                (stringValue.length - startPoint),
                stringValue.length,
                0
            )
        }
        return spannable
    }*/

//    fun setNumberSpannable(
//        context: Context,
//        stringValue: String,
//        startPoint: Int,
//        endPoint: Int
//    ): Spannable {
//        val spannable = SpannableString(stringValue)
//        if (stringValue.isNotEmpty()) {
//            val typefaceLight = ResourcesCompat.getFont(context, R.font.play_regular)
//            val foregroundColorSpan = ForegroundColorSpan(ContextCompat.getColor(context,R.color.white_50))
//            spannable.setSpan(
//                TypefaceSpan(typefaceLight!!),
//                startPoint,
//                endPoint,
//                Spanned.SPAN_INCLUSIVE_INCLUSIVE
//            )
//            spannable.setSpan(
//                foregroundColorSpan,
//                startPoint,
//                endPoint,
//                Spanned.SPAN_INCLUSIVE_INCLUSIVE
//            )
//            spannable.setSpan(
//                RelativeSizeSpan(0.8f),
//                startPoint,
//                endPoint,
//                0
//            )
//        }
//        return spannable
//    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun getFormattedDate(selectDate: String?): String? {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var date: Date? = null
        try {
            date = sdf.parse(selectDate)
        } catch (ex: Exception) {
            Log.e("Exception", "" + ex.message)
        }
        val smsTime = Calendar.getInstance()
        if (date != null) {
            smsTime.timeInMillis = date.time
        }
        val now = Calendar.getInstance()
        return getDateFromDateISO(selectDate, "dd MMM yy")

        /* return if (now[Calendar.YEAR] == smsTime[Calendar.YEAR] && now[Calendar.MONTH] == smsTime[Calendar.MONTH] && now[Calendar.DATE] == smsTime[Calendar.DATE]) {
             "Today "
         } else if (now[Calendar.YEAR] == smsTime[Calendar.YEAR] && now[Calendar.MONTH] == smsTime[Calendar.MONTH] && now[Calendar.DATE] - smsTime[Calendar.DATE] === 1) {
             "Yesterday "
         } else {
             getDateFromDateISO(selectDate, "dd MMM yy")
         }*/
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getFormattedDateForDetail(selectDate: String?): String? {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var date: Date? = null
        try {
            date = sdf.parse(selectDate)
        } catch (ex: Exception) {
            Log.e("Exception", "" + ex.message)
        }
        val smsTime = Calendar.getInstance()
        if (date != null) {
            smsTime.timeInMillis = date.time
        }
        val now = Calendar.getInstance()
        return if (now[Calendar.YEAR] == smsTime[Calendar.YEAR] && now[Calendar.MONTH] == smsTime[Calendar.MONTH] && now[Calendar.DATE] == smsTime[Calendar.DATE]) {
            "Today "
        } else if (now[Calendar.YEAR] == smsTime[Calendar.YEAR] && now[Calendar.MONTH] == smsTime[Calendar.MONTH] && now[Calendar.DATE] - smsTime[Calendar.DATE] === 1) {
            "Yesterday "
        } else {
            getDateFromDateISO(selectDate, "dd MMM")
        }
    }

    fun formatDate(millis: Long, dateFormat: String, returnFormat: String): String? {
        var date: String = java.text.SimpleDateFormat(dateFormat).format(millis)
        var format = "yyyy-MM-dd'T'HH:mm:ss"
        if (!dateFormat.isNullOrBlank()) {
            format = dateFormat
        }
        var df1: DateFormat = java.text.SimpleDateFormat(format, Locale.getDefault())
        val date1: Date?
        var date2: String? = ""
        try {
            date1 = df1.parse(date)
            df1 = java.text.SimpleDateFormat(returnFormat, Locale.getDefault())
            date2 = df1.format(date1 ?: "")
            return date2
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return date2
    }

    fun loadBitmapFromView(v: View): Bitmap {
        val b: Bitmap = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        v.draw(c)
        return b
    }

    fun shareScreenShot(context: Context, view: View) {
        var bitmap: Bitmap? = null
        Handler(Looper.getMainLooper()).postDelayed({
            bitmap = loadBitmapFromView(view)
            bitmap?.let {
                shareBitmap(context, it)
            }
        }, 1)
    }

    fun shareBitmap(activity: Context, bitmap: Bitmap?) {
        try {
            val cachePath = File(activity.cacheDir, "images")
            cachePath.mkdirs() // don't forget to make the directory
            val stream =
                FileOutputStream("$cachePath/image.png") // overwrites this image every time
            bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val imagePath = File(activity.cacheDir, "images")
        val newFile = File(imagePath, "image.png")
        val contentUri: Uri =
            FileProvider.getUriForFile(activity, "com.matter.companion.fileprovider", newFile)

        if (contentUri != null) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
            shareIntent.setDataAndType(contentUri, activity.contentResolver.getType(contentUri))
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            activity.startActivity(Intent.createChooser(shareIntent, "Choose an app"))
        }
    }

    fun pxToDp(px: Int, context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        return (px / (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    fun dpToPx(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    @JvmStatic
    fun hideKeyBoard(view: View) {
        val imm =
            SaloneDriver.instance.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

//     fun setUnitsTypeface(
//        context: Context,
//        stringValue: String,
//        startPoint: Int
//    ): Spannable {
//        val spannable = SpannableString(stringValue)
//        if (stringValue.isNotEmpty()) {
//            val typefaceLight = ResourcesCompat.getFont(context, R.font.bevietnam_regular)
//            if(stringValue.length - startPoint > 0) {
//                spannable.setSpan(
//                    TypefaceSpan(typefaceLight!!),
//                    (stringValue.length - startPoint),
//                    stringValue.length,
//                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
//                )
//                spannable.setSpan(
//                    RelativeSizeSpan(0.8f),
//                    (stringValue.length - startPoint),
//                    stringValue.length,
//                    0
//                )
//            }
//
//        }
//        return spannable
//    }

    /*fun setRegularTypeface(
        context: Context,
        stringValue: String,
        startPoint: Int
    ): Spannable {
        val spannable = SpannableString(stringValue)
        if (stringValue.isNotEmpty()) {
            val typefaceLight = ResourcesCompat.getFont(context, R.font.bevietnam_regular)
            spannable.setSpan(
                TypefaceSpan(typefaceLight!!),
                startPoint,
                stringValue.length,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
        }
        return spannable
    }*/

    /*fun setTextSpan(
        context: Context,
        stringValue: String,
        startPoint: Int,
        endPoint: Int,
        color: Int,
        fontStyle: Int?,
   ): Spannable {
        val spannableString = SpannableString(stringValue)
        val foregroundColorSpanGreen = ForegroundColorSpan(ContextCompat.getColor(context,color))

        spannableString.setSpan(foregroundColorSpanGreen, startPoint, endPoint, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)


        if(fontStyle!=null){
            val typefaceLight = ResourcesCompat.getFont(context, fontStyle)
            spannableString.setSpan(
                TypefaceSpan(typefaceLight!!),
                startPoint,
                endPoint,
                0
            )
        }

        return spannableString
   }*/

    /*fun setTimeTypeFaceForEmpty(
        context: Context,
        stringValue: String,
        startPoint: Int,
        endPoint: Int,
    ) : Spannable {
        val spannableString = SpannableString(stringValue)
        val foregroundColorSpanGreen = ForegroundColorSpan(ContextCompat.getColor(context,R.color.white))

        spannableString.setSpan(foregroundColorSpanGreen, startPoint, endPoint, 0)

        val typefaceLight = ResourcesCompat.getFont(context, R.font.bevietnam_regular)
        spannableString.setSpan(
            TypefaceSpan(typefaceLight!!),
            startPoint,
            endPoint,
            0
        )
        spannableString.setSpan(
            RelativeSizeSpan(0.7f),
            startPoint,
            endPoint,
            0
        )
        return spannableString
    }


    fun setTimeTypeface(context: Context, stringValue: String): Spannable {
        val spannable = SpannableString(stringValue)
        if (stringValue.isNotEmpty()) {
            val typefaceLight = ResourcesCompat.getFont(context, R.font.bevietnam_regular)
            spannable.setSpan(
                TypefaceSpan(typefaceLight!!),
                2,
                3,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
            spannable.setSpan(
                RelativeSizeSpan(0.7f),
                2,
                3,
                0
            )
            spannable.setSpan(
                TypefaceSpan(typefaceLight!!),
                6,
                9,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
            spannable.setSpan(
                RelativeSizeSpan(0.7f),
                6,
                9,
                0
            )
            spannable.setSpan(
                TypefaceSpan(typefaceLight!!),
                12,
                stringValue.length,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
            spannable.setSpan(
                RelativeSizeSpan(0.7f),
                12,
                stringValue.length,
                0
            )
        } else if(stringValue.contains("-")) {
            val typefaceLight = ResourcesCompat.getFont(context, R.font.bevietnam_regular)
            spannable.setSpan(
                TypefaceSpan(typefaceLight!!),
                3,
                4,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
            spannable.setSpan(
                RelativeSizeSpan(0.7f),
                3,
                4,
                0
            )
            spannable.setSpan(
                TypefaceSpan(typefaceLight!!),
                6,
                9,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
            spannable.setSpan(
                RelativeSizeSpan(0.7f),
                6,
                9,
                0
            )
            spannable.setSpan(
                TypefaceSpan(typefaceLight!!),
                12,
                stringValue.length,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
            spannable.setSpan(
                RelativeSizeSpan(0.7f),
                12,
                stringValue.length,
                0
            )
        }
        return spannable
    }

    fun setLeanTypeface(context: Context, stringValue: String): Spannable {
        val spannable = SpannableString(stringValue)
        if (stringValue.isNotEmpty()) {
            val typefaceLight = ResourcesCompat.getFont(context, R.font.bevietnam_regular)
            spannable.setSpan(
                TypefaceSpan(typefaceLight!!),
                3,
                4,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
            spannable.setSpan(
                RelativeSizeSpan(0.7f),
                3,
                4,
                0
            )
            spannable.setSpan(
                TypefaceSpan(typefaceLight!!),
                8,
                stringValue.length,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
            spannable.setSpan(
                RelativeSizeSpan(0.7f),
                8,
                stringValue.length,
                0
            )
        }
        return spannable
    }*/


    fun convertStringToBitmap(encodedString: String?): Bitmap? {
        return try {
            val encodeByte = Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        } catch (e: java.lang.Exception) {
            e.message
            null
        }
    }

    fun convertOneFormatToAnother(eventDay: String, changeFrom: String, changeTo: String): String {
        val clickedDayCalendar = eventDay
        val dateParser = java.text.SimpleDateFormat(changeFrom)
        val date = dateParser.parse(clickedDayCalendar.toString())
        val dateFormatter = java.text.SimpleDateFormat(changeTo)
        return dateFormatter.format(date)
    }


    fun convertStringToBase64(text: String): String {
        return Base64.encodeToString(text.toByteArray(), Base64.DEFAULT)
    }

    fun bitMapToString(bitmap: Bitmap): String? {
        return try {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos)
            val b = baos.toByteArray()
            Log.e("IMAGE", "image size after compression : ${(b.size.toFloat() / 1024) / 1024}")
            Base64.encodeToString(b, Base64.DEFAULT)
        } catch (e: Exception) {
            ""

        }
    }

    /////////////animation
    /*fun setAnimation(requireActivity: Context, onClickLamdaAnimation: (Animation, Animation, Animation) -> Unit){
        val animBottomLogo = AnimationUtils.loadAnimation(requireActivity, R.anim.bottom_up)
        val animBottomTvHeadTitle = AnimationUtils.loadAnimation(requireActivity, R.anim.bottom_up)
        val animBottom_up2 = AnimationUtils.loadAnimation(requireActivity, R.anim.bottom_up)
        animBottomLogo.setStartOffset(100)
        onClickLamdaAnimation(animBottomLogo,animBottomTvHeadTitle,animBottom_up2)
    }*/

    // replace string
    fun replaceDashWithEmpty(textString: String): String {
        return textString.replace("-", "")
    }

    // replace string
    /*  fun getEndDrawableIconVerified(textString: Boolean):Int{
          return if(textString){
              R.drawable.ic_info_verified
          }else{
              R.drawable.ic_info_unverified
          }
  }*/

    /*fun showCustomToast(context: Context, type : Int, message: String?, isBootomGravity: Boolean?) {
        val inflater = (context as Activity).layoutInflater
        val layout: View = inflater.inflate(R.layout.layout_custom_toast, null)
        val tvText = layout.findViewById<AppCompatTextView>(R.id.tvMessage)
        val clMain : View = layout.findViewById(R.id.clMain)
        clMain.setLayoutParams(
            ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,  ConstraintLayout.LayoutParams.MATCH_PARENT
            )
        )
        when(type) {
            ToastType.WHITE -> {
                clMain.background = ContextCompat.getDrawable(context, R.drawable.bg_white_stroke_1dp)
            }
            ToastType.RED -> {
                clMain.background = ContextCompat.getDrawable(context, R.drawable.bg_red_stroke_1dp)
            }
            ToastType.GREEN -> {
                clMain.background = ContextCompat.getDrawable(context, R.drawable.bg_green_stroke_1dp)
            }
        }
        tvText.text = message
        val toast = Toast(context)
        //toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        if(isBootomGravity!!){
            toast.setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 230)
        }else{
            toast.setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 20)
        }
        toast.show()
    }*/

    /*  fun setPlayTypeface(
          context: Context,
          stringValue: String,
          startPoint: Int,
          endPoint: Int
      ): Spannable {
          val spannable = SpannableString(stringValue)
          if (stringValue.isNotEmpty()) {
              val typefaceLight = ResourcesCompat.getFont(context, R.font.play_regular)
              spannable.setSpan(
                  TypefaceSpan(typefaceLight!!),
                  startPoint,
                  endPoint,
                  Spanned.SPAN_EXCLUSIVE_INCLUSIVE
              )
          }
          return spannable
      }
  */
    //creates a folder inside internal storage
    fun getOutputDirectory(fileName: String): File {
        val dir: File =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "//Matter/$fileName")
        if (!dir.exists()) dir.mkdir()
        return dir
    }

    fun saveBitmap(bmp: Bitmap, context: Context): Uri? {
        try {
            val fileName: String = System.currentTimeMillis().toString() + ".jpg"
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/")
                values.put(MediaStore.MediaColumns.IS_PENDING, 1)
            } else {
                val directory =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                val file = File(directory, fileName)
                values.put(MediaStore.MediaColumns.DATA, file.absolutePath)
            }
            val uri: Uri? = context.contentResolver?.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            )
            context.contentResolver?.openOutputStream(uri!!).use { output ->
                if (output != null) {
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, output)
                }
            }
            return uri
        } catch (e: java.lang.Exception) {
            Log.d("onBtnSavePng", e.toString()) // java.io.IOException: Operation not permitted
            return null
        }
    }

    /*fun setTimeUnitsTypefaceForNotifications(
        context: Context,
        stringValue: String,
        startPoint: Int
    ): Spannable {
        val spannable = SpannableString(stringValue)
        if (stringValue.isNotEmpty()) {
            val typefaceLight = ResourcesCompat.getFont(context, R.font.bevietnam_regular)
            if(stringValue.length - startPoint > 0) {
                spannable.setSpan(
                    TypefaceSpan(typefaceLight!!),
                    (stringValue.length - startPoint),
                    stringValue.length,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                )
                spannable.setSpan(
                    RelativeSizeSpan(0.8f),
                    (stringValue.length - startPoint),
                    stringValue.length,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                )
            }

        }
        return spannable
    }*/

    /* fun spanOnlyNumbersInString(str: String, context: Context) : Spannable{
         var startIndex = 0
         val spannable = SpannableString(str)
         val typefaceLight = ResourcesCompat.getFont(context, R.font.bevietnam_regular)

         str.forEachIndexed { index, c ->
             if (c.isDigit()) {
                 startIndex = index

                 spannable.setSpan(
                     TypefaceSpan(typefaceLight!!),
                     startIndex,
                     startIndex,
                     Spanned.SPAN_INCLUSIVE_INCLUSIVE
                 )
                 spannable.setSpan(
                     RelativeSizeSpan(0.8f),
                     startIndex,
                     startIndex,
                     Spanned.SPAN_INCLUSIVE_INCLUSIVE
                 )
             }
         }

         return spannable
     }

     fun getImageUri(inContext:Context, inImage:Bitmap):Uri? {
         val bytes = ByteArrayOutputStream()
         inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
         val path =
             MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "matter", null)
         return Uri.parse(path)
     }*/
}


/**
 * Get Auth Token
 * */
fun getFCMToken(fcmToken: (String) -> Unit = {}) = try {
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (!task.isSuccessful) {
            return@addOnCompleteListener
        }
        fcmToken(task.result)
    }
} catch (e: Exception) {
    e.printStackTrace()
}


/**
 * Fetch last 30 years list
 * */
fun fetchYearsList(): List<String> {
    val list = mutableListOf<String>()
    val calendar = Calendar.getInstance()
    for (i in 0..40) {
        list.add(calendar.get(Calendar.YEAR).toString())
        calendar.add(Calendar.YEAR, -1)
    }
    return list
}


fun Context.composeEmail(email: String?, subject: String? = null) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.setData(Uri.parse("mailto:$email")) // only email apps should handle this
        subject?.let {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun String?.formatAmount(): String {
    if (this.isNullOrEmpty()) return this.orEmpty().ifEmpty { "0.0" }
    try {
        this.toDoubleOrNull()?.let {
            return String.format("%.2f", it)
        } ?: run {
            throw Exception()
        }
    } catch (e: Exception) {
        return this
    }
}


/**
 * Show Home Page Dialog
 * */
@SuppressLint("StaticFieldLeak")
var bottomSheetDialog: BottomSheetDialog? = null
fun showHomePageDialog(
    @DrawableRes image: Int,
    message: String? = null,
    btnText: String? = null,
    cancelable: Boolean = false,
    callback: () -> Unit = {}
) {
    if (bottomSheetDialog?.isShowing != true) {
        SaloneDriver.appContext.let { context ->
            bottomSheetDialog = BottomSheetDialog(context).apply {
                val binding =
                    HomePageAlertBinding.inflate(LayoutInflater.from(context), null, false)
                setContentView(binding.root)

                setCancelable(cancelable)
                setCanceledOnTouchOutside(cancelable)

                binding.ivImage.setImageResource(image)
                binding.tvText.text = message

                binding.tvButton.isVisible = btnText.orEmpty().isNotEmpty()
                binding.tvButton.text = btnText.orEmpty()
                binding.tvButton.setOnClickListener {
                    dismiss()
                    callback()
                }

                show()
            }
        }
    }


}