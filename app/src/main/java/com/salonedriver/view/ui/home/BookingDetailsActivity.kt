package com.salonedriver.view.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.salonedriver.R
import com.salonedriver.databinding.ActivityBookingDetailsBinding
import com.salonedriver.model.api.observeData
import com.salonedriver.model.dataclassses.bookingHistory.RideSummaryDC
import com.salonedriver.util.SharedPreferencesManager
import com.salonedriver.util.formatAmount
import com.salonedriver.util.getTime
import com.salonedriver.view.base.BaseActivity
import com.salonedriver.viewmodel.BookingVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookingDetailsActivity : BaseActivity<ActivityBookingDetailsBinding>() {

    lateinit var binding: ActivityBookingDetailsBinding
    private val bookingId by lazy { intent.getStringExtra("bookingId").orEmpty() }
    private val viewModel by viewModels<BookingVM>()


    /**
     * Get Layout Id
     * */
    override fun getLayoutId(): Int {
        return R.layout.activity_booking_details
    }


    /**
     * On Create
     * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        clickHandler()
        observeRideSummary()
        viewModel.rideSummary(tripId = bookingId)
    }


    /**
     * Click Handler
     * */
    private fun clickHandler(){
        binding.ivBack.setOnClickListener {
            finish()
        }
    }


    /**
     * Set Up UI
     * */
    @SuppressLint("SetTextI18n")
    private fun setUpUi(dataClass: RideSummaryDC) = try {
        binding.tvCustomerName.text = dataClass.customerName.orEmpty()
        binding.tvAmountReceived.text = getString(R.string.your_received_amount_rs_60, "${SharedPreferencesManager.getCurrencySymbol()} ${dataClass.totalFare.orEmpty().formatAmount()}")
        binding.tvRideFare.text = "${SharedPreferencesManager.getCurrencySymbol()} ${dataClass.rideFare.orEmpty().ifEmpty { "0.0" }.formatAmount()}"
        binding.tvSubtotal.text = "${SharedPreferencesManager.getCurrencySymbol()} ${dataClass.subTotalRideFare.orEmpty().ifEmpty { "0.0" }.formatAmount()}"
        binding.tvWaitingTime.text = dataClass.waitTime.orEmpty().ifEmpty { "0" }.plus(" min")
        binding.tvDateTime.text = dataClass.createdAt.getTime(input = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", output = "dd/MM/yyyy, hh:mm a", applyTimeZone = true)

        Glide.with(binding.ivMap).load(dataClass.trackingImage).into(binding.ivMap)
    }catch (e:Exception){
        e.printStackTrace()
    }


    private fun observeRideSummary() = viewModel.rideSummaryData.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        this?.let { setUpUi(it) }
    }, onError = {
        hideProgressDialog()
        showToastShort(this)
    })
}