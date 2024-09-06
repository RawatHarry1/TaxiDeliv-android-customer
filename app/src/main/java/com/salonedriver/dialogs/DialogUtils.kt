package com.salonedriver.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.mukesh.mukeshotpview.mukeshOtpView.MukeshOtpView
import com.salonedriver.R
import com.salonedriver.customClasses.singleClick.setOnSingleClickListener
import com.salonedriver.databinding.DialogEditProfilePictureBinding
import com.salonedriver.util.showSnackBar


object DialogUtils {

    fun getNegativeDialog(
        mContext: Activity,
        positiveButton: String,
        title: String,
        onClickNegativeResult: (Int) -> Unit?
    ): Dialog {
        val dialogView = Dialog(mContext)
        with(dialogView) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)


            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(R.layout.dialog_negative_two_button)
            setCancelable(false)


            val tvCancel = findViewById<AppCompatTextView>(R.id.tvCancel)
            val tvTitle = findViewById<AppCompatTextView>(R.id.tvTitle)
            val tvConfirm = findViewById<AppCompatTextView>(R.id.tvConfirm)

            tvTitle.text = title
            tvConfirm.text = positiveButton

            tvCancel.setOnClickListener {
                dismiss()
            }

            tvConfirm.setOnClickListener {
                onClickNegativeResult(0)
                dismiss()
            }
            show()
        }
        return dialogView
    }

    lateinit var dialogView: Dialog

    fun getOtpDialog(
        mContext: Activity,
        phoneNumber: String,
        onClickDialogLambda: (otp: String?, isResend: Boolean, phoneNumber: String) -> Unit
    ): Dialog {
        dialogView = Dialog(mContext)
        with(dialogView) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)


            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(R.layout.dialog_otp)
            setCancelable(false)


            val tvVerify = findViewById<AppCompatTextView>(R.id.tvVerify)
            val tvResend = findViewById<AppCompatTextView>(R.id.tvResend)
            val customOtpView = findViewById<MukeshOtpView>(R.id.customOtpView)
            val tvEmailAddress = findViewById<AppCompatTextView>(R.id.tvEmailAddress)
            val ivClose = findViewById<AppCompatImageView>(R.id.ivClose)
            tvEmailAddress.text = phoneNumber


            tvResend.setOnClickListener {
                onClickDialogLambda(null, true, phoneNumber)
            }

            ivClose.setOnClickListener {
                dismiss()
            }

            tvVerify.setOnClickListener {
                if (customOtpView.text?.trim().toString().length == 4) {
                    dismiss()
                    onClickDialogLambda(customOtpView.text?.trim().toString(), false, phoneNumber)
                }
            }
            show()
        }
        return dialogView

    }


    fun dismissView() {
        if (this::dialogView.isInitialized) dialogView.dismiss()
    }


    private fun changeFocus(editTextArray: Array<AppCompatEditText>) {
        for (i in 1 until editTextArray.size) otpEnter(editTextArray[i - 1], editTextArray[i])
    }

    private fun otpEnter(currentEditText: EditText, nextEditText: EditText) {
        currentEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (currentEditText.length() == 1) {
                    nextEditText.requestFocus()
                }
            }
        })
    }

    fun getPermissionDeniedDialog(
        mContext: Activity,
        denied: Int,
        title: String,
        onClickNegativeResult: (Int) -> Unit?
    ): Dialog {
        val dialogView = Dialog(mContext)
        with(dialogView) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(R.layout.dialog_permission_denied)
            setCancelable(false)
            val tvTitle = findViewById<AppCompatTextView>(R.id.tvPermissionTitle)
            val tvConfirm = findViewById<AppCompatTextView>(R.id.tvAllowPermission)
            tvTitle.text = title
            tvConfirm.setOnClickListener {
                onClickNegativeResult(denied)
                dismiss()
            }
            show()
        }
        return dialogView
    }

    fun editProfilePictureDialog(
        context: Context,
        clickListener: EditProfileListener
    ) {
        val mDialog = Dialog(context, android.R.style.Theme_Translucent_NoTitleBar)
        mDialog.setCanceledOnTouchOutside(true)
        mDialog.setCancelable(true)
        if (mDialog.window != null) {
            mDialog.window!!.setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            mDialog.window!!.setGravity(Gravity.BOTTOM)
            val lp = mDialog.window!!.attributes
            lp.dimAmount = 0.75f
            mDialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            mDialog.window
            mDialog.setCancelable(false)
            mDialog.window!!.attributes = lp
            val dialogBinding: DialogEditProfilePictureBinding = DataBindingUtil.inflate(
                LayoutInflater.from(
                    context
                ),
                R.layout.dialog_edit_profile_picture, null, false
            )





            dialogBinding.tvTakePicture.setOnClickListener(View.OnClickListener {
                clickListener.onTakePictureListener()
                mDialog.dismiss()
            })

            dialogBinding.tvChooseFromGallery.setOnClickListener(View.OnClickListener {
                clickListener.onGalleryPictureListener()
                mDialog.dismiss()
            })

            dialogBinding.btnCancel.setOnClickListener(View.OnClickListener { mDialog.dismiss() })
            mDialog.setContentView(dialogBinding.root)
            mDialog.show()
        }
    }


    fun getAddTopUpDialog(mContext: Activity, onClickDialogLambda: (String) -> Unit): Dialog {
        val dialogView = Dialog(mContext)
        with(dialogView) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(R.layout.dialog_add_topup)
            setCancelable(false)

            val etEnterAmount = findViewById<AppCompatEditText>(R.id.etEnterAmount)
            val btnAddAmount = findViewById<AppCompatTextView>(R.id.btnAddAmount1)
            val ivClose = findViewById<AppCompatImageView>(R.id.ivClose)

            ivClose.setOnClickListener {
                dismiss()
            }

            btnAddAmount.setOnClickListener {
                if (etEnterAmount.text.toString().trim().isEmpty())
                    showSnackBar("Please enter amount", btnAddAmount)
                else if (etEnterAmount.text.toString().startsWith("0"))
                    showSnackBar("Please enter valid amount", btnAddAmount)
                else {
                    onClickDialogLambda(etEnterAmount.text.toString())
                    dismiss()
                }
            }
            show()
        }
        return dialogView

    }

    fun getVersionUpdateDialog(
        mContext: Activity,
        forceUpdate: Int,
        popUpDesc: String,
        downloadLink: String,
        onClickNegativeResult: (String) -> Unit?
    ): Dialog {
        val dialogView = Dialog(mContext)
        with(dialogView) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(R.layout.dialog_version_update)
            setCancelable(false)
            val tvUpdateNow = findViewById<AppCompatTextView>(R.id.tvUpdateNow)
            val tvDesc = findViewById<AppCompatTextView>(R.id.tvDescription)
            val tvLater = findViewById<AppCompatTextView>(R.id.tvLater)
            val viewMiddle = findViewById<View>(R.id.viewMiddle)
            tvDesc.text = popUpDesc
            if (forceUpdate == 1) {
                tvLater.isVisible = false
                viewMiddle.isVisible = false
            }
            tvUpdateNow.setOnSingleClickListener {
                mContext.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(downloadLink)
                    )
                )
            }
            tvLater.setOnSingleClickListener {
                dismiss()
            }
            show()
        }
        return dialogView
    }


    interface EditProfileListener {
        fun onTakePictureListener()
        fun onGalleryPictureListener()
    }
}