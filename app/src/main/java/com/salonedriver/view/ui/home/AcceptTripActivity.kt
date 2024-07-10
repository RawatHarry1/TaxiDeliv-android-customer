package com.salonedriver.view.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.salonedriver.R
import com.salonedriver.customClasses.singleClick.setOnSingleClickListener
import com.salonedriver.databinding.FragmentAcceptTripBinding
import com.salonedriver.firebaseSetup.NewRideNotificationDC
import com.salonedriver.model.api.observeData
import com.salonedriver.util.SharedPreferencesManager
import com.salonedriver.util.TripStatus
import com.salonedriver.util.cancelTrip
import com.salonedriver.util.formatAmount
import com.salonedriver.util.getTime
import com.salonedriver.util.gone
import com.salonedriver.util.visible
import com.salonedriver.view.base.BaseActivity
import com.salonedriver.view.ui.chat.ChatActivity
import com.salonedriver.view.ui.home.cancelTrip.CancelTrip
import com.salonedriver.viewmodel.RideViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AcceptTripActivity : BaseActivity<FragmentAcceptTripBinding>() {

    lateinit var binding: FragmentAcceptTripBinding
    private val viewModel by viewModels<RideViewModel>()
    private val screenType: String by lazy { intent?.getStringExtra("screenType") ?: "AcceptTrip" }
    private val rideData by lazy { intent.getParcelableExtra<NewRideNotificationDC>("rideData") }

    override fun getLayoutId(): Int {
        return R.layout.fragment_accept_trip
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        observeAcceptTrip()
        clickListener()
        setUI()
        observeMarkArrived()
        observeStartTrip()
    }

    @SuppressLint("SetTextI18n")
    private fun setUI() {
        if (screenType == "TripAccepted") {
            if ((rideData?.status ?: 0) == TripStatus.ACCEPTED.type) {
                binding.tvSignUpBtn.text = getString(R.string.go_to_pick_up)
                binding.tvCancel.visible()
                binding.ivBack.visibility = View.INVISIBLE
            } else {
                binding.tvSignUpBtn.text = getString(R.string.accept)
                binding.tvCancel.gone()
                binding.ivBack.visibility = View.VISIBLE
            }
            binding.tvTitle.text = getString(R.string.trip_accepted)
            binding.ivCall.gone()
            if (rideData?.customerNote.isNullOrEmpty()) {
                binding.tvCustomerNote.gone()
                binding.tvCustomerNoteValue.gone()
            } else {
                binding.tvCustomerNote.visible()
                binding.tvCustomerNoteValue.visible()
            }
        } else if (screenType == "RideCompleted") {
            binding.tvTitle.text = getString(R.string.ride_completed)
            binding.tvRateCustomer.visible()
            binding.tvSignUpBtn.gone()
            binding.tvCancel.gone()
            binding.ivCall.gone()
            binding.ivPath.gone()
            binding.titlePickUp.gone()
            binding.titleDestination.gone()
            binding.tvPickUp.gone()
            binding.tvDestination.gone()
            binding.tvCustomerNote.gone()
            binding.tvCustomerNoteValue.gone()
        }

        binding.tvCustomerName.text = rideData?.customerName.orEmpty()
        binding.tvCustomerNoteValue.text = rideData?.customerNote ?: ""
        binding.tvFare.text =
            "${if (rideData?.estimatedDriverFare?.contains(SharedPreferencesManager.getCurrencySymbol()) == true) "" else SharedPreferencesManager.getCurrencySymbol()}${
                rideData?.estimatedDriverFare.orEmpty().formatAmount()
            }"
        binding.tvPickUp.text = rideData?.pickUpAddress.orEmpty()
        binding.tvDestination.text = rideData?.dropAddress.orEmpty()
        binding.tvDateTime.text = rideData?.date.getTime(
            input = "yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'",
            output = "MMMM dd, yyyy, hh:mm a",
            applyTimeZone = true
        )
        Glide.with(binding.ivCustomerPic).load(rideData?.customerImage.orEmpty())
            .error(R.drawable.ic_profile_user).into(binding.ivCustomerPic)
    }


    private fun clickListener() {
        binding.tvSignUpBtn.setOnClickListener {
            when (binding.tvSignUpBtn.text) {
                getString(R.string.accept) -> {
                    viewModel.acceptRide(rideData?.customerId.orEmpty(), rideData?.tripId.orEmpty())
                }

                getString(R.string.go_to_pick_up) -> {
                    finish()
                }
            }
        }
        binding.tvRateCustomer.setOnSingleClickListener {
            startActivity(
                Intent(
                    this@AcceptTripActivity,
                    RateCustomerActivity::class.java
                ).putExtra("rideData", rideData)
            )
        }

        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.ivMsg.setOnClickListener { startActivity(Intent(this, ChatActivity::class.java)) }

        binding.tvCancel.setOnClickListener {
            cancelTrip {
                Intent(this, CancelTrip::class.java).apply {
                    putExtra("tripId", rideData?.tripId.orEmpty())
                    startActivity(this)
                }
            }
        }
    }


    /**
     * Observe Accept Trip
     * */
    private fun observeAcceptTrip() = viewModel.acceptRideData.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        SharedPreferencesManager.clearKeyData(SharedPreferencesManager.Keys.NEW_BOOKING)
        binding.tvSignUpBtn.text = getString(R.string.go_to_pick_up)
        binding.ivBack.visibility = View.INVISIBLE
        binding.tvCancel.visible()
    }, onError = {
        hideProgressDialog()
        showToastLong(this)
        SharedPreferencesManager.clearKeyData(SharedPreferencesManager.Keys.NEW_BOOKING)
        viewModel.newRideNotificationData = NewRideNotificationDC()
        finish()
    })


    /**
     * Mark Arrived
     * */
    private fun observeMarkArrived() = viewModel.markArrived.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        viewModel.startTrip(rideData?.customerId.orEmpty(), rideData?.tripId.orEmpty())
    }, onError = {
        hideProgressDialog()
        showErrorMessage(this)
    })


    /**
     * Observe Start Trip
     * */
    private fun observeStartTrip() = viewModel.startTrip.observeData(this, onLoading = {
        showProgressDialog()
    }, onError = {
        hideProgressDialog()
        showErrorMessage(this)
    }, onSuccess = {
        hideProgressDialog()
//        HomeFragment.checkOnGoingBooking?.checkOnGoingBooking()
        finish()
    })


}