package com.superapp_driver.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.superapp_driver.R
import com.superapp_driver.SaloneDriver
import com.superapp_driver.databinding.DialogNegativeTwoButtonBinding
import com.superapp_driver.databinding.DocumentUnderProcessAlertBinding
import com.superapp_driver.firebaseSetup.SoundService
import com.superapp_driver.view.adapter.VehicleModelAdapter
import com.superapp_driver.view.ui.SignIn
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun View.gone() {
    this.visibility = View.GONE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.inVisible() {
    this.visibility = View.INVISIBLE
}

fun Activity.transparentStatusAndNavigation(
    systemUiScrim: Int = Color.parseColor("#40000000") // 25% black
) {
    var systemUiVisibility = 0
    // Use a dark scrim by default since light status is API 23+
    var statusBarColor = systemUiScrim
    //  Use a dark scrim by default since light nav bar is API 27+
    var navigationBarColor = systemUiScrim
    val winParams = window.attributes


    systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    statusBarColor = Color.TRANSPARENT

    window.attributes = winParams
}


/**
 * Get Edit Text Value
 * */
fun EditText.getValue() = text?.trim().toString()


/**
 * Array Adapter
 * */
fun Context.arrayAdapter(
    autoCompleteTextView: AutoCompleteTextView,
    list: List<String>,
    onClick: (position: Int) -> Unit = {}
) {
    val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
    autoCompleteTextView.setAdapter(adapter)
    // Measure the height of a single item
    val itemHeight =
        autoCompleteTextView.context.resources.getDimensionPixelSize(android.R.dimen.app_icon_size)
    autoCompleteTextView.dropDownHeight = itemHeight * list.size
    autoCompleteTextView.showDropDown()
    autoCompleteTextView.onItemClickListener =
        AdapterView.OnItemClickListener { _, _, p2, _ -> onClick.invoke(p2) }
}

/**
 * Array Adapter
 * */
fun Context.vehicleModelAdapter(
    spinner: Spinner, list: List<Pair<String, String>>, onClick: (position: Int) -> Unit = {}
) {
    val adapter = VehicleModelAdapter(this, list)
    spinner.adapter = adapter
    spinner.performClick()
    spinner.onItemSelectedListener = object : OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            Log.e("sdfdsfsd", "dfsdfsd  yes")
            onClick(p2)
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
            Log.e("sdfdsfsd", "dfsdfsd  NO")
        }
    }
}


/**
 * Show Session Expire
 * */
fun showSessionExpire() {
    try {
        SaloneDriver.appContext.let { context ->
            context.stopService(Intent(context,SoundService::class.java))
            Toast.makeText(
                context,
                context.getString(R.string.session_expire_due_to_security_purpose_please_sign_in_again),
                Toast.LENGTH_SHORT
            ).show()
            SharedPreferencesManager.clearKeyData(SharedPreferencesManager.Keys.USER_DATA)
            (context as Activity).startActivity(Intent(context, SignIn::class.java))
            (context as Activity).finishAffinity()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


/**
 * Logout Alert
 * */
fun logoutAlert(callback: () -> Unit) = try {
    SaloneDriver.appContext.let { context ->
        AlertDialog.Builder(context).apply {
            val binding =
                DialogNegativeTwoButtonBinding.inflate(LayoutInflater.from(context), null, false)
            setView(binding.root)
            val dialog = create()

            binding.tvCancel.setOnClickListener {
                dialog.dismiss()
            }

            binding.tvConfirm.setOnClickListener {
                dialog.dismiss()
                callback()
            }

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }
    }
} catch (e: Exception) {
    e.printStackTrace()
}

fun deleteAlert(callback: () -> Unit) = try {
    SaloneDriver.appContext.let { context ->
        AlertDialog.Builder(context).apply {
            val binding =
                DialogNegativeTwoButtonBinding.inflate(LayoutInflater.from(context), null, false)
            setView(binding.root)
            val dialog = create()
            binding.tvTitle.text = "Are you sure you want\n to delete account?"

            binding.tvCancel.setOnClickListener {
                dialog.dismiss()
            }

            binding.tvConfirm.setOnClickListener {
                dialog.dismiss()
                callback()
            }

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }
    }
} catch (e: Exception) {
    e.printStackTrace()
}


/**
 * Get Time
 * */
@SuppressLint("SimpleDateFormat")
fun String?.getTime(
    input: String = "yyyy-MM-dd HH:mm:ss",
    output: String = "yyyy-MM-dd HH:mm:ss",
    applyTimeZone: Boolean = false,
    applyOutputTimeZone: Boolean = false
): String {
    if (this.isNullOrEmpty()) return "N/A"
    return if (this.isNotEmpty()) {
        val inputFormat = SimpleDateFormat(input, Locale.getDefault())
        if (applyTimeZone) inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val outputFormat = SimpleDateFormat(output, Locale.getDefault())
        if (applyOutputTimeZone) outputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(this)
        outputFormat.format(date ?: "")
    } else {
        "N/A"
    }
}


/**
 * Document Bottom Sheet
 * */
var documentBottomSheet: WeakReference<BottomSheetDialog>? = null
fun documentNotVerifiedBottomSheet() {
    SaloneDriver.appContext.let { context ->
        documentBottomSheet?.get()?.dismiss()
        BottomSheetDialog(context).apply {
            documentBottomSheet = WeakReference(this)
            val binding =
                DocumentUnderProcessAlertBinding.inflate(LayoutInflater.from(context), null, false)
            setContentView(binding.root)
            documentBottomSheet?.get()?.show()
        }
    }
}


fun commonAlert(
    title: String? = null,
    message: String? = null,
    positiveButtonText: String? = null,
    negativeButtonText: String? = null,
    callback: () -> Unit = {}
) {
    SaloneDriver.appContext.let { context ->
        AlertDialog.Builder(context).apply {
            setTitle(title ?: context.getString(R.string.app_name))
            setMessage(message ?: context.getString(R.string.app_name))

            setPositiveButton(positiveButtonText) { dialog, _ ->
                dialog.dismiss()
                callback()
            }

            negativeButtonText?.let {
                setNegativeButton(negativeButtonText) { dialog, _ ->
                    dialog.dismiss()
                }
            }

            show()
        }
    }
}


fun commonToast(
    message: String? = null
) {
    SaloneDriver.appContext.let {
        Toast.makeText(it, message.orEmpty(), Toast.LENGTH_SHORT).show()
    }
}


/**
 * Logout Alert
 * */
fun cancelTrip(callback: () -> Unit) = try {
    SaloneDriver.appContext.let { context ->
        AlertDialog.Builder(context).apply {
            val binding =
                DialogNegativeTwoButtonBinding.inflate(LayoutInflater.from(context), null, false)
            setView(binding.root)

            binding.tvTitle.text = context.getString(R.string.cancel_thr_ride_request)
            binding.tvConfirm.text = context.getString(R.string.cancel_ride)
            val dialog = create()

            binding.tvCancel.setOnClickListener {
                dialog.dismiss()
            }

            binding.tvConfirm.setOnClickListener {
                dialog.dismiss()
                callback()
            }

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }
    }
} catch (e: Exception) {
    e.printStackTrace()
}