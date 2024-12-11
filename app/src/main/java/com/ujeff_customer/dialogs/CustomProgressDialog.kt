package com.ujeff_customer.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.Window
import com.ujeff_customer.R
import kotlin.jvm.Synchronized

class CustomProgressDialog {
    private var dialog: Dialog? = null
    private var mContext: Context? = null
    fun show(context: Context?) {
        this.mContext = context
        if (!(context as Activity).isFinishing) {
            if (dialog != null && dialog!!.isShowing) {
                dismiss()
            }
            dialog = Dialog(context, R.style.ProgressDialog)
            dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog!!.setContentView(R.layout.dialog_progress)
            dialog!!.setCancelable(false)
            if (!context.isFinishing)
                dialog!!.show()
        }
    }

    // to dismiss the dialog
    fun dismiss() {
        if (mContext!=null && !(mContext as Activity).isFinishing && dialog != null && dialog!!.isShowing) {
            dialog?.dismiss()
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var mInstance: CustomProgressDialog? = null

        @get:Synchronized
        val instance: CustomProgressDialog?
            get() {
                if (mInstance == null) {
                    mInstance = CustomProgressDialog()
                }
                return mInstance
            }
    }
}