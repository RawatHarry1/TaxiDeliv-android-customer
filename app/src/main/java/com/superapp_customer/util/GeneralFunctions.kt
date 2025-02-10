package com.superapp_customer.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.annotation.CheckResult
import com.superapp_customer.R
import com.superapp_customer.VenusApp
import com.superapp_customer.customClasses.singleClick.setOnSingleClickListener
import com.superapp_customer.databinding.DialogNegativeTwoButtonBinding
import com.superapp_customer.view.activity.sign_in.SignIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import java.text.SimpleDateFormat
import java.util.Date
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
 * Show Session Expire
 * */
fun showSessionExpire() {
    try {
        VenusApp.appContext.let { context ->
            VenusApp.offerApplied = 0
            VenusApp.offerTitle = ""
            showSnackBar(context.getString(R.string.session_expire_due_to_security_purpose_please_sign_in_again))
            SharedPreferencesManager.clearKeyData(SharedPreferencesManager.Keys.USER_DATA)
            (context as Activity).startActivity(
                Intent(
                    context,
                    SignIn::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
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
    VenusApp.appContext.let { context ->
        AlertDialog.Builder(context).apply {
            val binding =
                DialogNegativeTwoButtonBinding.inflate(LayoutInflater.from(context), null, false)
            setView(binding.root)
            val dialog = create()

            binding.tvCancel.setOnSingleClickListener {
                dialog.dismiss()
            }

            binding.tvConfirm.setOnSingleClickListener {
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
    input: String = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
    output: String = "yyyy-MM-dd HH:mm:ss",
    applyTimeZone: Boolean = false,
    applyOutputTimeZone: Boolean = false
): String {
    try {
        if (this.isNullOrEmpty()) return ""
        return if (this.isNotEmpty()) {
            val inputFormat = SimpleDateFormat(input, Locale.getDefault())
            if (applyTimeZone) inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val outputFormat = SimpleDateFormat(output, Locale.getDefault())
            if (applyOutputTimeZone) outputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(this)
            outputFormat.format(date ?: "")
        } else {
            ""
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
}


/**
 * Get Edit Text Value
 * */
fun EditText.getValue() = text?.trim().toString()

fun safeCall(block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        e.printStackTrace()
    }
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


@ExperimentalCoroutinesApi
@CheckResult
fun EditText.textChanges(): Flow<CharSequence?> {
    return callbackFlow<CharSequence?> {
        val listener = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.trim()?.isNotEmpty() == true) {
                    trySend(s.trim())
                }
            }
        }
        addTextChangedListener(listener)
        awaitClose { removeTextChangedListener(listener) }
    }.onStart { emit(text) }
}
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

fun getCurrentUTCDateTime(): String {
    // Define the output format as UTC
    val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
    outputFormat.timeZone = TimeZone.getTimeZone("UTC")
    // Get the current date and time
    val currentDate = Date()
    // Format the current date and time in UTC
    return outputFormat.format(currentDate)
}