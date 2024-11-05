package com.superapp_customer.dialogs

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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.mukesh.mukeshotpview.mukeshOtpView.MukeshOtpView
import com.superapp_customer.R
import com.superapp_customer.customClasses.singleClick.setOnSingleClickListener
import com.superapp_customer.databinding.DialogEditProfilePictureBinding
import com.superapp_customer.util.NoSpaceInputFilter
import com.superapp_customer.util.safeCall
import com.superapp_customer.util.showSnackBar
import com.superapp_customer.view.activity.walk_though.ui.home.SelectGoodsAdapter

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

            tvCancel.setOnSingleClickListener {
                dismiss()
            }

            tvConfirm.setOnSingleClickListener {
                onClickNegativeResult(0)
                dismiss()
            }
            show()
        }
        return dialogView
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




    fun getPromoDialog(
        mContext: Activity,
        onClickNegativeResult: (String) -> Unit?
    ): Dialog {
        val dialogView = Dialog(mContext)
        with(dialogView) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(R.layout.dialog_enter_promo_code)
            setCancelable(true)
            val tvConfirm = findViewById<AppCompatTextView>(R.id.tvSubmit)
            val etPromoCode = findViewById<AppCompatEditText>(R.id.etEnterPromoCode)
            etPromoCode.filters = arrayOf(NoSpaceInputFilter())
            tvConfirm.setOnSingleClickListener {
                if (etPromoCode.text.toString().trim().isEmpty())
                    showSnackBar("Please Enter Promo Code", this)
                else {
                    onClickNegativeResult(etPromoCode.text.toString().trim())
                    dismiss()
                }
            }
            show()
        }
        return dialogView
    }
    fun getGoodsTypeDialog(
        mContext: Activity,
        onClickNegativeResult: (String) -> Unit?
    ): Dialog {
        val dialogView = Dialog(mContext)
        with(dialogView) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(R.layout.dialog_select_goods)
            setCancelable(true)
            var selectedGood = ""
            val tvConfirm = findViewById<AppCompatTextView>(R.id.tvSubmit)
            val recyclerViewGoodsTypes = findViewById<RecyclerView>(R.id.rvSelectGoods)
            val goodsTypes = listOf(
                "Timber / Plywood / Laminate",
                "Electrical / Electronics / Home Appliances",
                "Building / Construction",
                "Catering / Restaurant / Event Management",
                "Machines / Equipments / Spare Parts",
                "General",
                "House Shifting",
                "Perishable food items",
                "Plastic / Rubber",
                "Books / Stationery / Toys",
                "House Shifting",
                "Chemicals / Paints"
            )
            // Set up RecyclerView
            recyclerViewGoodsTypes.layoutManager = LinearLayoutManager(mContext)
            recyclerViewGoodsTypes.adapter = SelectGoodsAdapter(goodsTypes) { selectedGoodsType ->
                selectedGood = selectedGoodsType
            }
            tvConfirm.setOnSingleClickListener {
                if (selectedGood.isEmpty())
                    showSnackBar("Please select goods type", this)
                else
                    onClickNegativeResult(selectedGood)
                dismiss()

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
                safeCall {
                    mContext.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(downloadLink)
                        )
                    )
                }
            }
            tvLater.setOnSingleClickListener {
                dismiss()
            }
            show()
        }
        return dialogView
    }


    fun verifyOtpDialog(
        mContext: Activity,
        countryCode: String,
        phoneNumber: String,
        dismissDialog: (dialog: Dialog) -> Unit,
        verify: (otp: String, dialog: Dialog) -> Unit?,
        resend: (dialog: Dialog) -> Unit?,
    ): Dialog {
        val dialogView = Dialog(mContext)
        with(dialogView) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)


            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(R.layout.dialog_otp)
            setCancelable(false)
            val rootView = findViewById<MaterialCardView>(R.id.cardMainOtp)
            val tvEmailAddress = findViewById<TextView>(R.id.tvEmailAddress)
            val ivClose = findViewById<ImageView>(R.id.ivClose)
            val tvConfirm = findViewById<AppCompatTextView>(R.id.tvVerify)
            val tvResend = findViewById<AppCompatTextView>(R.id.tvResend)
            val simpleOtpView = findViewById<MukeshOtpView>(R.id.simpleOtpView)

            tvEmailAddress.text = "$countryCode $phoneNumber"


            ivClose.setOnSingleClickListener {
                dismissDialog(dialogView)
            }

            tvConfirm.setOnSingleClickListener {
                if ((simpleOtpView.text?.length ?: 0) == 4) {
                    verify(simpleOtpView.text.toString(), dialogView)
                } else if ((simpleOtpView.text?.length ?: 0) == 0) {
                    showSnackBar(mContext.getString(R.string.please_enter_otp), this)
                } else {
                    showSnackBar(mContext.getString(R.string.please_enter_valid_otp), this)
                }
            }


            tvResend.setOnSingleClickListener {
                showSnackBar(mContext.getString(R.string.otp_has_been_resent), this)
                resend(dialogView)
            }
            show()
        }
        return dialogView
    }

    lateinit var dialogView: Dialog

    fun dismissView() {
        if (this::dialogView.isInitialized) dialogView.dismiss()
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

            ivClose.setOnSingleClickListener {
                dismiss()
            }

            btnAddAmount.setOnSingleClickListener {
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

            dialogBinding.tvTakePicture.setOnSingleClickListener {
                clickListener.onTakePictureListener()
                mDialog.dismiss()
            }

            dialogBinding.tvChooseFromGallery.setOnSingleClickListener {
                clickListener.onGalleryPictureListener()
                mDialog.dismiss()
            }

            dialogBinding.btnCancel.setOnSingleClickListener { mDialog.dismiss() }
            mDialog.setContentView(dialogBinding.getRoot())
            mDialog.show()
        }
    }


    interface EditProfileListener {
        fun onTakePictureListener()
        fun onGalleryPictureListener()
    }
}