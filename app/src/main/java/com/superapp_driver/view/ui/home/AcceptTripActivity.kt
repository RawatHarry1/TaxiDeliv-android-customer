package com.superapp_driver.view.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.superapp_driver.R
import com.superapp_driver.customClasses.singleClick.setOnSingleClickListener
import com.superapp_driver.databinding.FragmentAcceptTripBinding
import com.superapp_driver.databinding.ItemPackageImagesBinding
import com.superapp_driver.databinding.ItemPackageListBinding
import com.superapp_driver.dialogs.DialogUtils
import com.superapp_driver.firebaseSetup.NewRideNotificationDC
import com.superapp_driver.firebaseSetup.PackageDetails
import com.superapp_driver.model.api.observeData
import com.superapp_driver.model.dataclassses.userData.UserDataDC
import com.superapp_driver.util.GenericAdapter
import com.superapp_driver.util.SharedPreferencesManager
import com.superapp_driver.util.TripStatus
import com.superapp_driver.util.cancelTrip
import com.superapp_driver.util.convertUTCToLocal
import com.superapp_driver.util.formatAmount
import com.superapp_driver.util.getTime
import com.superapp_driver.util.gone
import com.superapp_driver.util.inVisible
import com.superapp_driver.util.visible
import com.superapp_driver.view.base.BaseActivity
import com.superapp_driver.view.ui.chat.ChatActivity
import com.superapp_driver.view.ui.home.cancelTrip.CancelTrip
import com.superapp_driver.viewmodel.RideViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AcceptTripActivity : BaseActivity<FragmentAcceptTripBinding>() {
    private lateinit var packagesAdapter: GenericAdapter<PackageDetails>
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
                )
                // Replace with the phone number you want to call
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
        if ((rideData?.serviceType
                ?: "1").ifEmpty { "1" }.toInt() == 1
        ) {
            binding.viewLine1.isVisible = false
            binding.rvAddedPackages.isVisible = false
            binding.rlPackage.isVisible = false
            binding.viewLine.isVisible = false
        } else {
            setAdapter()
            binding.viewLine1.isVisible = true
            binding.rvAddedPackages.isVisible = true
            binding.rlPackage.isVisible = true
            binding.viewLine.isVisible = true
        }
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
            binding.viewLine1.isVisible = false
            binding.rvAddedPackages.isVisible = false
            binding.rlPackage.isVisible = false
            binding.viewLine.isVisible = false
        } else {
            if (rideData?.serviceType == "1")
                binding.tvTitle.text =
                    if (rideData?.isForRental == "1") getString(R.string.accept_trip) + " (Rental)" else getString(
                        R.string.accept_trip
                    )
            else
                binding.tvTitle.text =
                    if (rideData?.isForRental == "1") getString(R.string.accept_delivery) + " (Rental)" else getString(
                        R.string.accept_delivery
                    )

            if (rideData?.isForRental == "1") {
                binding.tvEndDate.isVisible = true
                binding.tvEndDateValue.isVisible = true
                binding.tvEndDateValue.text = rideData?.rentalDropDate?.convertUTCToLocal()
            }
        }

        binding.tvCustomerNote.text = if ((rideData?.serviceType
                ?: "1").ifEmpty { "1" }.toInt() == 1
        ) "Customer Note: " else "Customer Goods: "



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
                    viewModel.acceptRide(
                        rideData?.customerId.orEmpty(),
                        rideData?.tripId.orEmpty(),
                        (rideData?.isRor ?: "0").toInt()
                    )
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
        if ((rideData?.isRor ?: "0") == "0") {
            binding.tvSignUpBtn.text = getString(R.string.go_to_pick_up)
            binding.ivBack.visibility = View.INVISIBLE
            binding.tvCancel.visible()
            binding.ivMsg.visible()
        } else {
            showToastLong("Accepted successfully and added to queue")
            finish()
        }
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

    private fun setAdapter() {
        packagesAdapter = object : GenericAdapter<PackageDetails>(R.layout.item_package_list) {
            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val binding = ItemPackageListBinding.bind(holder.itemView)
                val data = getItem(position)
                binding.tvPackageSize.text = data.package_size
                binding.tvPackageType.text = data.type
                binding.tvPackageQuantity.text = data.quantity
                binding.llCustomerImages.isVisible = true
                val adapter = object : GenericAdapter<String>(R.layout.item_package_images) {
                    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                        val bindingM = ItemPackageImagesBinding.bind(holder.itemView)
                        Glide.with(this@AcceptTripActivity).load(getItem(position).toString())
                            .into(bindingM.ivUploadedImage)
                        bindingM.root.setOnClickListener {
                            fullImagesDialog(getItem(position))
                        }
                    }
                }
                adapter.submitList(data.image)
                binding.rvCustomerImages.adapter = adapter
            }
        }
        packagesAdapter.submitList(
            rideData?.deliveryPackages.orEmpty()
        )
        binding.rvAddedPackages.adapter = packagesAdapter
    }


    fun fullImagesDialog(
        string: String
    ): Dialog {
        val dialogView = Dialog(this)
        with(dialogView) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(R.layout.dialog_full_image)
            setCancelable(false)
            val rlDismiss = findViewById<RelativeLayout>(R.id.rlDismiss)
            rlDismiss.setOnClickListener { dismiss() }
            val ivPackageImage = findViewById<ImageView>(R.id.ivPackageImage)
            Glide.with(this@AcceptTripActivity).load(string)
                .into(ivPackageImage)
            // Set width to full screen
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            show()
        }
        return dialogView
    }

}