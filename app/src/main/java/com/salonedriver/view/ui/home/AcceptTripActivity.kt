package com.salonedriver.view.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.salonedriver.R
import com.salonedriver.customClasses.singleClick.setOnSingleClickListener
import com.salonedriver.databinding.FragmentAcceptTripBinding
import com.salonedriver.dialogs.DialogUtils
import com.salonedriver.firebaseSetup.NewRideNotificationDC
import com.salonedriver.model.api.observeData
import com.salonedriver.model.dataclassses.userData.UserDataDC
import com.salonedriver.util.SharedPreferencesManager
import com.salonedriver.util.TripStatus
import com.salonedriver.util.cancelTrip
import com.salonedriver.util.formatAmount
import com.salonedriver.util.getTime
import com.salonedriver.util.gone
import com.salonedriver.util.inVisible
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
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var userId = 0
    override fun getLayoutId(): Int {
        return R.layout.fragment_accept_trip
    }

    private val showMessageIndicatorBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            binding.ivMsgIndicator.isVisible = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
            ?.let {
                userId = it.login?.userId?.toIntOrNull() ?: 0
            }
        observeAcceptTrip()
        clickListener()
        setUI()
        observeMarkArrived()
        observeStartTrip()
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                makePhoneCall(
                    rideData?.userPhoneNo ?: ""
                ) // Replace with the phone number you want to call
            } else {
                DialogUtils.getPermissionDeniedDialog(
                    this,
                    1,
                    getString(R.string.allow_call_permission),
                    ::onDialogCallPermissionAllowClick
                )
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                showMessageIndicatorBroadcastReceiver,
                IntentFilter("newMsg"), Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(
                showMessageIndicatorBroadcastReceiver,
                IntentFilter("newMsg")
            )
        }
    }

    private fun onDialogCallPermissionAllowClick(type: Int) {
        if (type == 0) {
            requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        } else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(showMessageIndicatorBroadcastReceiver)
    }

    @SuppressLint("SetTextI18n")
    private fun setUI() {
        if (screenType == "TripAccepted") {
            if ((rideData?.status ?: 0) == TripStatus.ACCEPTED.type) {
                binding.tvSignUpBtn.text = getString(R.string.go_to_pick_up)
                binding.tvCancel.visible()
                binding.ivBack.visibility = View.INVISIBLE
                binding.ivMsg.visible()
            } else {
                binding.tvSignUpBtn.text = getString(R.string.accept)
                binding.tvCancel.gone()
                binding.ivBack.visibility = View.VISIBLE
                binding.ivMsg.inVisible()
            }
            binding.tvTitle.text = getString(R.string.trip_accepted)
//            binding.ivCall.gone()
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
        if (rideData?.customerNote.isNullOrEmpty()) {
            binding.tvCustomerNote.gone()
            binding.tvCustomerNoteValue.gone()
        } else {
            binding.tvCustomerNote.visible()
            binding.tvCustomerNoteValue.visible()
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

    private fun checkPermissionAndMakeCall(phoneNumber: String) {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted
                makePhoneCall(phoneNumber)
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE) -> {
                // Show rationale and request permission
                // You can show a dialog explaining why you need this permission
                DialogUtils.getPermissionDeniedDialog(
                    this,
                    0,
                    getString(R.string.allow_call_permission),
                    ::onDialogCallPermissionAllowClick
                )
            }

            else -> {
                // Directly request the permission
                DialogUtils.getPermissionDeniedDialog(
                    this,
                    1,
                    getString(R.string.allow_call_permission),
                    ::onDialogCallPermissionAllowClick
                )
            }
        }
    }

    private fun makePhoneCall(phoneNumber: String) {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$phoneNumber")
        startActivity(callIntent)
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
        binding.ivMsg.setOnClickListener {
            binding.ivMsgIndicator.isVisible = false
            startActivity(
                Intent(this, ChatActivity::class.java).apply {
                    flags =
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("customerId", "${rideData?.customerId}")
                    putExtra("driverId", "${userId}")
                    putExtra("engagementId", "${rideData?.tripId}")
                    putExtra("customerName", "${rideData?.customerName}")
                    putExtra("customerImage", "${rideData?.customerImage}")
                }


            )
        }
        binding.ivCall.setOnSingleClickListener {
            checkPermissionAndMakeCall(rideData?.userPhoneNo ?: "")
        }
        binding.tvCancel.setOnClickListener {
            cancelTrip {
                Intent(this, CancelTrip::class.java).apply {
                    putExtra("tripId", rideData?.tripId.orEmpty())
                    putExtra("customerId", rideData?.customerId.orEmpty())
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
        binding.ivMsg.visible()
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