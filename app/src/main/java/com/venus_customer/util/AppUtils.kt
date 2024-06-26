package com.venus_customer.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Insets
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
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
import android.util.TypedValue
import android.view.View
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.venus_customer.VenusApp
import com.venus_customer.di.ErrorModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DateFormat
import java.text.ParseException
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AppUtils {

    const val CurrencyCode = "$"
    const val DATE_yyyy_MM_dd_T_HH_mm_ss = "yyyy-MM-dd'T'HH:mm:ss"

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
            VenusApp.instance.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun isInternetAvailable(context: Context? = VenusApp.instance.applicationContext): Boolean {
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
            if (cursor == null)
                return contentUri.path
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            if (cursor.moveToFirst())
                filePath = cursor.getString(columnIndex)
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
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
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
        df.setTimeZone(timeZone)

        return df.format(Date(minutes!! * 60 * 1000L))
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getOnlyHourFromMinutes(minutes: Int): String {
        val timeZone: TimeZone = TimeZone.getTimeZone("UTC")
        val df = SimpleDateFormat("HH")
        df.setTimeZone(timeZone)

        return df.format(Date(minutes * 60 * 1000L))
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getOnlyMinuteFromMinutes(minutes: Int): String {
        val timeZone: TimeZone = TimeZone.getTimeZone("UTC")
        val df = SimpleDateFormat("mm")
        df.setTimeZone(timeZone)

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

    fun loadBitmapFromView(v: View): Bitmap? {
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

    fun prepareBody(mainObject: JSONObject): RequestBody {
        Log.e("request data", "-request--$mainObject---")
        return mainObject.toString().toRequestBody("application/json".toMediaType())
    }

    fun errorFormatter(catch: Throwable?): ErrorModel {
        return try {
            val errorResponse = (catch as? HttpException)?.response()?.errorBody()?.string()
            if (errorResponse == null) {
                println("errorFormatter================" + catch?.message)
//                println("errorFormatter================" + catch?.localizedMessage)
//                println("cause================" +  catch?.cause)
                val exceptionToCheck = (catch as? Exception)
//                println("SocketTimeoutException================" + (exceptionToCheck is SocketTimeoutException))
//                println("TimeoutException================" + (exceptionToCheck is TimeoutException))
                /*            if (exceptionToCheck != null && (exceptionToCheck is SocketTimeoutException)) {
                                EventBus.getDefault().post(
                                    EventBusData(
                                        AppConstants.EventBusConstants.TIME_OUT,
                                        AppConstants.EventBusConstants.TIME_OUT
                                    )
                                )
                            }*/
                val errorToSend = ErrorModel()
                errorToSend.message = catch?.message
                if (errorToSend.message == "closed")
                    errorToSend.message = "Internal Server Error"
                return errorToSend

            }
            Gson().fromJson(errorResponse, ErrorModel::class.java)
        } catch (e: Exception) {
            ErrorModel()
        }
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
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
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
        val imm = VenusApp.instance
            .getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
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
            val encodeByte =
                Base64.decode(encodedString, Base64.DEFAULT)
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
        if (!dir.exists())
            dir.mkdir();
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
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )
            context.contentResolver?.openOutputStream(uri!!).use { output ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, output)
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


fun View.getBottomSheetBehaviour(
    isDraggableAlert: Boolean = false,
    showFull: Boolean = false
): BottomSheetBehavior<View> {
    return BottomSheetBehavior.from(this).apply {
        isDraggable = isDraggableAlert
        peekHeight = context.toPx(200).toInt()
        maxHeight = if (showFull) calculateDynamicHeight().toInt() else context.toPx(400).toInt()
        state = BottomSheetBehavior.STATE_HIDDEN
        Log.d("BottomSheet", "Initial state set to HIDDEN")
    }
}

fun View.calculateDynamicHeight(): Int {
    measure(
        View.MeasureSpec.makeMeasureSpec((parent as View).width, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    return measuredHeight
}

fun Context.toPx(dp: Int): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    dp.toFloat(),
    resources.displayMetrics
)


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

