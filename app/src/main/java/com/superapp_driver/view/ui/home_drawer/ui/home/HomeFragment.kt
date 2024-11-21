package com.superapp_driver.view.ui.home_drawer.ui.home

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import com.birjuvachhani.locus.Locus
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ncorti.slidetoact.SlideToActView
import com.superapp_driver.R
import com.superapp_driver.SaloneDriver
import com.superapp_driver.customClasses.LocationResultHandler
import com.superapp_driver.customClasses.SingleFusedLocation
import com.superapp_driver.databinding.DialogTripsBinding
import com.superapp_driver.databinding.FragmentHomeBinding
import com.superapp_driver.dialogs.DialogUtils
import com.superapp_driver.firebaseSetup.NewRideNotificationDC
import com.superapp_driver.firebaseSetup.NotificationInterface
import com.superapp_driver.firebaseSetup.SoundService
import com.superapp_driver.model.api.observeData
import com.superapp_driver.model.dataclassses.userData.UserDataDC
import com.superapp_driver.socketSetup.SocketSetup
import com.superapp_driver.socketSetup.locationServices.LocationService
import com.superapp_driver.trackingData.animateDriver
import com.superapp_driver.trackingData.clearMap
import com.superapp_driver.trackingData.showPath
import com.superapp_driver.trackingData.vectorToBitmap
import com.superapp_driver.util.AppUtils
import com.superapp_driver.util.DriverDocumentStatusForApp
import com.superapp_driver.util.SharedPreferencesManager
import com.superapp_driver.util.TripStatus
import com.superapp_driver.util.documentNotVerifiedBottomSheet
import com.superapp_driver.util.formatAmount
import com.superapp_driver.view.base.BaseFragment
import com.superapp_driver.view.ui.CancelTripActivity
import com.superapp_driver.view.ui.chat.ChatActivity
import com.superapp_driver.view.ui.home.AcceptTripActivity
import com.superapp_driver.view.ui.home.PackageListActivity
import com.superapp_driver.view.ui.home_drawer.HomeActivity
import com.superapp_driver.viewmodel.RideViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(), LocationResultHandler,
    NotificationInterface, CheckOnGoingBooking {
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>
    companion object {
        var notificationInterface: NotificationInterface? = null
        var checkOnGoingBooking: CheckOnGoingBooking? = null
    }

    lateinit var binding: FragmentHomeBinding
    var screenType = 0
    var routeType = 3
    private var userId = 0
    private var googleMap: GoogleMap? = null
    private val viewModel by viewModels<HomeViewModel>()
    private val rideViewModel by viewModels<RideViewModel>()
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>
    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_home
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        binding.alertView.clRoot.isVisible = false
        binding.bestRoute.clParent.isVisible = false
        checkOnGoingBooking = this
        binding.swOnOff.isChecked =
            SharedPreferencesManager.getBoolean(SharedPreferencesManager.Keys.DRIVER_ONLINE)
        SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
            ?.let {
                userId = it.login?.userId?.toIntOrNull() ?: 0
            }
        observeLoginAccessToken()
        observeChangeStatus()
        observeRejectTrip()
        observeOngoingTrip()
        observeEndTrip()
        observeMarkArrived()
        observeStartTrip()
        binding.ivMenuBurg.setOnClickListener {
            when (screenType) {
                0 -> (activity as HomeActivity).openDrawer()
                1 -> (activity as HomeActivity).openDrawer()
                2 -> {
                    binding.ivMsgIndicator.isVisible = false
                    (activity as HomeActivity).startActivity(
                        Intent(
                            activity, ChatActivity::class.java
                        ).putExtra(
                            "customerId",
                            "${rideViewModel.newRideNotificationData.customerId}"
                        )
                            .putExtra("driverId", "${userId}")
                            .putExtra(
                                "engagementId",
                                "${rideViewModel.newRideNotificationData.tripId}"
                            )
                            .putExtra(
                                "customerName",
                                "${rideViewModel.newRideNotificationData.customerName}"
                            )
                            .putExtra(
                                "customerImage",
                                "${rideViewModel.newRideNotificationData.customerImage}"
                            )
                    )
                }
            }
        }
        binding.swOnOff.setOnClickListener {
            viewModel.changeStatus(binding.swOnOff.isChecked)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireActivity().registerReceiver(
                showMessageIndicatorBroadcastReceiver,
                IntentFilter("newMsg"), Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            requireActivity().registerReceiver(
                showMessageIndicatorBroadcastReceiver,
                IntentFilter("newMsg")
            )
        }



        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true && permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true -> {
                    // Both foreground and background location permissions are granted
                    Log.i("RequestPermission", "when 1")
                    handleLocationPermissionGranted()
                }

                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                    // Foreground location permission granted, but background is not
                    Log.i("RequestPermission", "when 2")
                    showPermissionAreNotAllowed(true)
                }

                else -> {
                    // Permission denied
                    Log.i("RequestPermission", "when 3")
                    showPermissionAreNotAllowed(true)
                }
            }
        }

        // Initialize the permission launcher
        notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted
                Log.i("Permission", "Notification permission granted")

            } else {
                // Permission denied
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                        // Denied once, show rationale
                        Log.i(
                            "Permission",
                            "Notification permission denied once, showing rationale"
                        )
                        DialogUtils.getPermissionDeniedDialog(
                            requireActivity(),
                            0,
                            getString(R.string.allow_notifications),
                            ::onDialogPermissionAllowClick
                        )
                    } else {
                        // Denied multiple times or "Don't ask again"
                        Log.i("Permission", "Notification permission denied multiple times")
                        DialogUtils.getPermissionDeniedDialog(
                            requireActivity(),
                            1,
                            getString(R.string.allow_notifications),
                            ::onDialogPermissionAllowClick
                        )
                    }
                }
            }
        }


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            Log.i("RequestPermission", "onCreate home fragment")
//            if (ContextCompat.checkSelfPermission(
//                    requireActivity(),
//                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//            )
//                showBackgroundLocationRequired()
//        }


    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    Log.i("Permission", "Notification permission granted")

                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Denied once, show rationale dialog
                    Log.i("Permission", "Notification permission denied once")
                    DialogUtils.getPermissionDeniedDialog(
                        requireActivity(),
                        0,
                        getString(R.string.allow_notifications),
                        ::onDialogPermissionAllowClick
                    )
                }

                else -> {
                    // First-time request or denied with "Don't ask again"
                    Log.i("Permission", "Requesting notification permission")
                    DialogUtils.getPermissionDeniedDialog(
                        requireActivity(),
                        1,
                        getString(R.string.allow_notifications),
                        ::onDialogPermissionAllowClick
                    )
                }
            }
        } else {

        }
    }

    private fun showPermissionAreNotAllowed(isDeniedMultipleTimes: Boolean) {
        binding.swOnOff.isEnabled = false
        binding.swOnOff.isChecked = false
        viewModel.changeStatus(binding.swOnOff.isChecked)
        homePageAlerts(
            image = R.drawable.app_logo_image,
            message = getString(R.string.allow_location),
            btnText = getString(R.string.allow),
        ) {
            if (!isDeniedMultipleTimes)
                checkAndRequestLocationPermissions()
            else {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }
    }

    private fun onDialogPermissionAllowClick(type: Int) {
        if (type == 0) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", requireActivity().packageName, null)
            }
            startActivity(intent)
        }
    }

    private val locationUpdateBroadcast = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationService.LOCATION_BROADCAST) {
                val latitude = intent.getStringExtra("latitude").orEmpty()
                val longitude = intent.getStringExtra("longitude").orEmpty()
                val bearing = intent.getStringExtra("bearing").orEmpty()

                Log.e("LocationUpdate", "OnBroadcast   Lat: ${latitude}, Lng: ${longitude}")

                if (!rideViewModel.newRideNotificationData.tripId.isNullOrEmpty()) {
                    AppUtils.tripId = rideViewModel.newRideNotificationData.tripId.orEmpty()
                    SaloneDriver.latLng =
                        LatLng(latitude.toDoubleOrNull() ?: 0.0, longitude.toDoubleOrNull() ?: 0.0)
                    SocketSetup.emitLocation(
                        LatLng(latitude.toDoubleOrNull() ?: 0.0, longitude.toDoubleOrNull() ?: 0.0),
                        bearing,
                        rideViewModel.newRideNotificationData.tripId.orEmpty()
                    )
                    requireContext().animateDriver(
                        driverLatitude = latitude.toDoubleOrNull() ?: 0.0,
                        driverLongitude = longitude.toDoubleOrNull() ?: 0.0,
                        bearing = bearing.toDoubleOrNull() ?: 0.0,
                        googleMap = googleMap
                    )
                }
            }
        }
    }


    private fun getLocationUpdates() {
        unregisterReceiver()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            locationUpdateBroadcast, IntentFilter(LocationService.LOCATION_BROADCAST)
        )
        Locus.stopLocationUpdates()
        requireActivity().startService(Intent(requireContext(), LocationService::class.java))
    }

    private fun unregisterReceiver() {
        try {
            LocalBroadcastManager.getInstance(requireContext())
                .unregisterReceiver(locationUpdateBroadcast)
            requireActivity().unregisterReceiver(showMessageIndicatorBroadcastReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        screenType = 0
        notificationInterface = this
        checkAndRequestNotificationPermission()
        initializeMap()
    }


    /**
     * Set Screen Type
     * */
    private fun setScreenType(context: Context) {
        when (screenType) {
            0 -> {
                binding.ivMsgIndicator.isVisible = false
                binding.clOffOn.visibility =
                    if (binding.swOnOff.isChecked) View.GONE else View.VISIBLE
                binding.tvStatus.text =
                    if (binding.swOnOff.isChecked) getString(R.string.txt_online) else getString(R.string.txt_offline)
                binding.ivMenuBurg.setImageDrawable(
                    ContextCompat.getDrawable(
                        context, R.drawable.home
                    )
                )
                binding.swOnOff.visibility = View.VISIBLE
            }

            1 -> {
                binding.ivMsgIndicator.isVisible = false
                binding.swOnOff.visibility = View.INVISIBLE
                binding.clOffOn.visibility = View.GONE
                binding.tvStatus.text = ""
                processingDialog(context)
            }

            2 -> {
                binding.swOnOff.visibility = View.INVISIBLE
                binding.clOffOn.visibility = View.GONE
                binding.tvStatus.text = ""
                binding.ivMenuBurg.setImageDrawable(
                    ContextCompat.getDrawable(
                        context, R.drawable.ic_chat
                    )
                )
                bestRoute(context)
            }

        }
    }


    /**
     * Ride Show at Home Page
     * */
    private var dialog: BottomSheetDialog? = null
    private fun callTripsDialog(context: Context, data: NewRideNotificationDC) {
        if (dialog?.isShowing == true) return
        dialog = BottomSheetDialog(context, R.style.SheetDialog)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = DialogTripsBinding.inflate(LayoutInflater.from(requireContext()), null, false)
        dialog?.setContentView(binding.root)
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)

        binding.tvUser.text = data.customerName.orEmpty()
        binding.tvPrice.text =
            "${data.currency} ${data.estimatedDriverFare.orEmpty().formatAmount()}"
        binding.tvDistance.text =
            "${data.estimatedDistance.orEmpty().formatAmount()} ${data.distanceUnit ?: "Km"}"
        binding.tvPickUpAddress.text = data.pickUpAddress.orEmpty()
        binding.tvDestinationAddress.text = data.dropAddress.orEmpty()
        Glide.with(binding.ivUser).load(data.customerImage.orEmpty())
            .error(R.drawable.ic_profile_user).into(binding.ivUser)

        binding.tvAcceptBtn.setOnClickListener {
            requireActivity().stopService(Intent(requireActivity(), SoundService::class.java))
            screenType = 2
            dialog?.dismiss()
            context.startActivity(
                Intent(context, AcceptTripActivity::class.java).putExtra("screenType", "AcceptTrip")
                    .putExtra("rideData", data)
            )
        }
        binding.tvIgnoreBtn.setOnClickListener {
            requireActivity().stopService(Intent(requireActivity(), SoundService::class.java))
            rideViewModel.rejectRide(data.tripId.orEmpty())
            AppUtils.tripId = ""
            rideViewModel.newRideNotificationData = NewRideNotificationDC()
            screenType = 0
            SharedPreferencesManager.clearKeyData(SharedPreferencesManager.Keys.NEW_BOOKING)
            dialog?.dismiss()
        }
        dialog?.setCancelable(true)
        dialog?.show()
    }


    /**
     * Processing Dialog
     * */
    private fun processingDialog(context: Context) {
        if (dialog?.isShowing == true) dialog?.dismiss()
        dialog = BottomSheetDialog(context, R.style.SheetDialog)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(R.layout.dialog_processing)
        val tvSlide: TextView = dialog?.findViewById(R.id.tvSlide)!!
        val pbProcess: ProgressBar = dialog?.findViewById(R.id.pbProcess)!!

        tvSlide.setOnClickListener {
            (activity as HomeActivity).startActivity(
                Intent(
                    activity, CancelTripActivity::class.java
                )
            )
        }
        pbProcess.setOnClickListener {
            dialog?.dismiss()
            screenType = 2
            (activity as HomeActivity).startActivity(
                Intent(activity, AcceptTripActivity::class.java).putExtra(
                    "screenType", "TripAccepted"
                )
            )
        }
        dialog?.setCancelable(true)
        dialog?.show()
    }

    private val showMessageIndicatorBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (screenType == 2)
                binding.ivMsgIndicator.isVisible = true
        }
    }

    /**
     * Best Route
     * */
    private fun bestRoute(context: Context) {
        if (dialog?.isShowing == true) dialog?.dismiss()
        val viewLayout = binding.bestRoute
        viewLayout.clParent.isVisible = true
        setRouteType(
            viewLayout.tvSlideTrip,
            context,
            viewLayout.ivArrivePickup,
            viewLayout.tvArrivePickup,
            viewLayout.tvOnDestination,
            viewLayout.tvRideComplete,
            viewLayout.ivOnDestination,
            viewLayout.ivRideComplete,
            viewLayout.tvBestRoute
        )
        viewLayout.tvRideComplete.text = if ((rideViewModel.newRideNotificationData.serviceType
                ?: "0").toInt() == 2
        ) getString(R.string.txt_volt_delivery_completed) else getString(R.string.txt_volt_ride_completed)
        viewLayout.clReceiver.isVisible =
            (rideViewModel.newRideNotificationData.serviceType ?: "0").toInt() == 2
        viewLayout.tvReceiverNameValue.text =
            rideViewModel.newRideNotificationData.recipientName ?: ""
        viewLayout.tvReceiverPhoneValue.text =
            rideViewModel.newRideNotificationData.recipientPhoneNo ?: ""

        viewLayout.tvSlideTrip.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {
                override fun onSlideComplete(view: SlideToActView) {
                    viewLayout.tvSlideTrip.setCompleted(completed = false, withAnimation = true)
                    if (routeType == RideStateEnum.RIDE_ACCEPT.data) {
                        rideViewModel.markArrived(
                            customerId = rideViewModel.newRideNotificationData.customerId.orEmpty(),
                            tripId = rideViewModel.newRideNotificationData.tripId.orEmpty()
                        )
                    } else if (routeType == RideStateEnum.ARRIVE_AT_PICKUP.data) {
                        if ((rideViewModel.newRideNotificationData.serviceType ?: "0").toInt() == 2)
                            startActivity(
                                Intent(
                                    requireActivity(),
                                    PackageListActivity::class.java
                                ).putExtra("isEndTrip",false)
                            )
                        else
                            rideViewModel.startTrip(
                                customerId = rideViewModel.newRideNotificationData.customerId.orEmpty(),
                                tripId = rideViewModel.newRideNotificationData.tripId.orEmpty()
                            )
                    } else if (routeType == RideStateEnum.ON_THE_WAY.data) {
                        routeType = RideStateEnum.END_TRIP.data
                        bestRoute(requireContext())
                    } else {
                        if ((rideViewModel.newRideNotificationData.serviceType ?: "0").toInt() == 2) {
                            dialog?.dismiss()
                            screenType = 0
                            routeType = 1
                            setScreenType(requireContext())
                            viewLayout.clParent.isVisible = false
                            startActivity(
                                Intent(
                                    requireActivity(),
                                    PackageListActivity::class.java
                                ).putExtra("isEndTrip", true)
                            )
                        }
                        else {
                            rideViewModel.endTrip(
                                customerId = rideViewModel.newRideNotificationData.customerId.orEmpty(),
                                tripId = rideViewModel.newRideNotificationData.tripId.orEmpty(),
                                dropLatitude = rideViewModel.newRideNotificationData.dropLatitude.orEmpty(),
                                dropLongitude = rideViewModel.newRideNotificationData.dropLongitude.orEmpty(),
                                distanceTravelled = rideViewModel.newRideNotificationData.distanceTravelled.orEmpty(),
                                rideTime = rideViewModel.newRideNotificationData.rideTime.orEmpty(),
                                waitTime = rideViewModel.newRideNotificationData.waitTime.orEmpty()
                            )
                            dialog?.dismiss()
                        }
                    }
                }
            }
        viewLayout.tvArrivePickup.setOnClickListener {
            when (routeType) {
                0 -> {
                    setRouteType(
                        viewLayout.tvSlideTrip,
                        context,
                        viewLayout.ivArrivePickup,
                        viewLayout.tvArrivePickup,
                        viewLayout.tvOnDestination,
                        viewLayout.tvRideComplete,
                        viewLayout.ivOnDestination,
                        viewLayout.ivRideComplete,
                        viewLayout.tvBestRoute
                    )
                }
            }
        }
    }


    /**
     * Set Route Type
     * */
    private fun setRouteType(
        tvSlideTrip: SlideToActView,
        context: Context,
        ivArrivePickup: ImageView,
        tvArrivePickup: TextView,
        tvOnDestination: TextView,
        tvRideComplete: TextView,
        ivOnDestination: ImageView,
        ivRideComplete: ImageView,
        tvBestRoute: TextView
    ) {
        when (routeType) {
            RideStateEnum.RIDE_ACCEPT.data -> {
//                tvSlideTrip.backgroundTintList = context.getColorStateList(R.color.theme_button)
                tvSlideTrip.textColor = ContextCompat.getColor(context, R.color.white)
                tvSlideTrip.text = getString(R.string.txt_slide_to_mark_arrive)
                tvArrivePickup.setTextColor(context.getColorStateList(R.color.text_color))
                tvOnDestination.setTextColor(context.getColorStateList(R.color.text_color))
                ivArrivePickup.setImageDrawable(context.getDrawable(R.drawable.ic_location_pin_faded))
                ivOnDestination.setImageDrawable(context.getDrawable(R.drawable.ic_location_pin_faded))
                requireContext().showPath(
                    srcLat = LatLng(
                        SaloneDriver.latLng?.latitude ?: 0.0, SaloneDriver.latLng?.longitude ?: 0.0
                    ), desLat = LatLng(
                        rideViewModel.newRideNotificationData.latitude?.toDoubleOrNull() ?: 0.0,
                        rideViewModel.newRideNotificationData.longitude?.toDoubleOrNull() ?: 0.0
                    ), mMap = googleMap
                )
            }

            RideStateEnum.ARRIVE_AT_PICKUP.data -> {
//                tvSlideTrip.backgroundTintList = context.getColorStateList(R.color.theme_button)
                tvSlideTrip.textColor = ContextCompat.getColor(context, R.color.white)
                tvArrivePickup.setTextColor(context.getColorStateList(R.color.black))
                tvSlideTrip.text = if ((rideViewModel.newRideNotificationData.serviceType
                        ?: "0").toInt() == 2
                ) getString(R.string.txt_slide_to_start_delivery) else getString(R.string.txt_slide_to_start_trip)

                ivArrivePickup.setImageDrawable(context.getDrawable(R.drawable.ic_location_pin))
                requireContext().showPath(
                    srcLat = LatLng(
                        SaloneDriver.latLng?.latitude ?: 0.0, SaloneDriver.latLng?.longitude ?: 0.0
                    ), desLat = LatLng(
                        rideViewModel.newRideNotificationData.latitude?.toDoubleOrNull() ?: 0.0,
                        rideViewModel.newRideNotificationData.longitude?.toDoubleOrNull() ?: 0.0
                    ), mMap = googleMap
                )
            }

            RideStateEnum.ON_THE_WAY.data -> {
//                tvSlideTrip.backgroundTintList = context.getColorStateList(R.color.theme_button)
                tvSlideTrip.textColor = ContextCompat.getColor(context, R.color.white)
                tvSlideTrip.text = getString(R.string.reach_destination)
                tvArrivePickup.setTextColor(context.getColorStateList(R.color.black))
                tvOnDestination.setTextColor(context.getColorStateList(R.color.black))
                ivArrivePickup.setImageDrawable(context.getDrawable(R.drawable.ic_location_pin))
                ivOnDestination.setImageDrawable(context.getDrawable(R.drawable.ic_location_pin))
                requireContext().showPath(
                    srcLat = LatLng(
                        SaloneDriver.latLng?.latitude ?: 0.0, SaloneDriver.latLng?.longitude ?: 0.0
                    ), desLat = LatLng(
                        rideViewModel.newRideNotificationData.dropLatitude?.toDoubleOrNull() ?: 0.0,
                        rideViewModel.newRideNotificationData.dropLongitude?.toDoubleOrNull() ?: 0.0
                    ), mMap = googleMap
                )
            }

            RideStateEnum.END_TRIP.data -> {
//                tvSlideTrip.backgroundTintList = context.getColorStateList(R.color.theme_button)
                tvSlideTrip.textColor = ContextCompat.getColor(context, R.color.white)
                tvSlideTrip.text = getString(R.string.txt_complete_trip)
                tvBestRoute.text = getString(R.string.txt_complete_trip)
                tvArrivePickup.setTextColor(context.getColorStateList(R.color.black))
                tvOnDestination.setTextColor(context.getColorStateList(R.color.black))
                ivArrivePickup.setImageDrawable(context.getDrawable(R.drawable.ic_location_pin))
                ivOnDestination.setImageDrawable(context.getDrawable(R.drawable.ic_location_pin))
            }
        }
    }


    /**
     * Initialize Map
     * */
    private fun initializeMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync {
            googleMap = it
            googleMap?.clear()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    binding.swOnOff.isEnabled = true
                    SingleFusedLocation.initialize(requireContext(), this)
                } else {
                    showPermissionAreNotAllowed(true)
//                    if (showBackgroundLocationPermissionRationaleClicked) {
//                        showBackgroundLocationPermissionRationaleClicked = false
//                        showBackgroundLocationPermissionRationale()
//                    }
                }
            } else
                SingleFusedLocation.initialize(requireContext(), this)
        }
    }


    /**
     * Mark Arrived
     * */
    private fun observeMarkArrived() = rideViewModel.markArrived.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        screenType = 2
        routeType = RideStateEnum.ARRIVE_AT_PICKUP.data
        setScreenType(requireContext())
    }, onError = {
        hideProgressDialog()
        showToastLong(this)
    })


    /**
     * Observe Start Trip
     * */
    private fun observeStartTrip() = rideViewModel.startTrip.observeData(this, onLoading = {
        showProgressDialog()
    }, onError = {
        hideProgressDialog()
        showToastLong(this)
    }, onSuccess = {
        hideProgressDialog()
        screenType = 2
        routeType = RideStateEnum.ON_THE_WAY.data
        setScreenType(requireContext())
    })


    override fun updatedLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        googleMap?.addMarker(
            MarkerOptions().position(
                latLng
            ).apply {
                icon(requireActivity().vectorToBitmap(R.drawable.new_location_placeholder))
                anchor(0.5f, 1f)
            }
        )
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
        SaloneDriver.latLng = LatLng(location.latitude, location.longitude)
        viewModel.loginViaAccessToken(location.latitude, location.longitude)
    }

    private fun onDialogClick(promoCode: String) {

    }

    //Login Via Access Token
    private fun observeLoginAccessToken() =
        viewModel.loginViaAccessToken.observeData(viewLifecycleOwner, onLoading = {
            showProgressDialog()
        }, onSuccess = {
            hideProgressDialog()
            SharedPreferencesManager.putModel(SharedPreferencesManager.Keys.USER_DATA, this)
            if (this?.login != null && this.login?.popup != null) {
                if (this.login?.popup?.is_force != null
                    && this.login?.popup?.popup_text != null
                    && this.login?.popup?.download_link != null
                ) {
                    DialogUtils.getVersionUpdateDialog(
                        requireActivity(),
                        this.login?.popup?.is_force!!,
                        this.login?.popup?.popup_text!!,
                        this.login?.popup?.download_link!!,
                        ::onDialogClick
                    )
                }
            }
            SharedPreferencesManager.put(
                SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID,
                this?.login?.serviceType ?: 0
            )
            val isLowWalletBalance =
                (this?.login?.actualCreditBalance.orEmpty().ifEmpty { "0.0" }.toDoubleOrNull()
                    ?: 0.0) < (this?.login?.minDriverBalance.orEmpty().ifEmpty { "0.0" }
                    .toDoubleOrNull() ?: 0.0)
            val switchEnable =
                !isLowWalletBalance && (this?.login?.driverDocumentStatus?.requiredDocStatus.orEmpty() == DriverDocumentStatusForApp.APPROVED.type) && ((this?.login?.driverBlockedMultipleCancellation?.blocked
                    ?: 0) <= 0)
            binding.swOnOff.isEnabled = switchEnable
            if (!switchEnable) binding.swOnOff.isChecked = false
            binding.tvStatus.text =
                if (binding.swOnOff.isChecked) getString(R.string.txt_online) else getString(R.string.txt_offline)
            when {
                this?.login?.driverDocumentStatus?.requiredDocStatus.orEmpty() == DriverDocumentStatusForApp.PENDING.type -> {
                    homePageAlerts(
                        image = R.drawable.ic_dialog_clock,
                        message = getString(R.string.your_document_verification_is_in_process_please_wait_until_it_been_verified)
                    ) {
                        requireView().findNavController().navigate(R.id.nav_documents)
                    }
                }

                this?.login?.driverDocumentStatus?.requiredDocStatus.orEmpty() == DriverDocumentStatusForApp.REJECTED.type -> {
                    homePageAlerts(
                        image = R.drawable.ic_home_document_alert,
                        message = getString(R.string.need_to_take_action_on_your_documents_please_check_documents_in_menu),
                        btnText = getString(R.string.upload),
                    ) {
                        requireView().findNavController().navigate(R.id.nav_documents)
                    }
                }

                isLowWalletBalance -> {
                    homePageAlerts(
                        image = R.drawable.ic_home_walllet_alert,
                        message = getString(R.string.your_wallet_balance_is_currently_running_low_please_recharge_your_wallet)
                    ) {
                        requireView().findNavController().navigate(R.id.wallet)
                    }
                }

                (this?.login?.isThresholdReached ?: 0) == 1 -> {
                    homePageAlerts(
                        image = R.drawable.ic_home_block_alert,
                        message = "Your account has been blocked. Please contact Admin.",
                    )
                }

                ((this?.login?.driverBlockedMultipleCancellation?.blocked ?: 0) > 0) -> {
                    homePageAlerts(
                        image = R.drawable.ic_home_block_alert,
                        message = "Your account has been blocked. Please contact Admin.",
                    )
                }

                else -> {
                    binding.alertView.clRoot.isVisible = false
                    checkOnGoingBooking()
                }
            }
        }, onError = {
            hideProgressDialog()
            showToastLong(this)
        })


    private fun observeChangeStatus() =
        viewModel.changeStatusData.observeData(viewLifecycleOwner, onLoading = {
            showProgressDialog()
        }, onSuccess = {
            hideProgressDialog()
            binding.tvStatus.text =
                if (binding.swOnOff.isChecked) getString(R.string.txt_online) else getString(R.string.txt_offline)
            binding.clOffOn.isVisible = !binding.swOnOff.isChecked
            SharedPreferencesManager.put(
                SharedPreferencesManager.Keys.DOCUMENT_APPROVED,
                this?.docStatus.orEmpty() == "APPROVED"
            )
            binding.swOnOff.isChecked = (this?.autosAvailable == 1)
            if (this?.docStatus.orEmpty() != "APPROVED") {
                try {
                    documentNotVerifiedBottomSheet()
                } catch (e: Exception) {
                }
            }
            fetchNewNotification()
        }, onError = {
            binding.swOnOff.isChecked = !binding.swOnOff.isChecked
            SharedPreferencesManager.put(SharedPreferencesManager.Keys.DOCUMENT_APPROVED, false)
            hideProgressDialog()
            showToastLong(this)
        })


    private fun observeOngoingTrip() =
        rideViewModel.ongoingTrip.observeData(viewLifecycleOwner, onSuccess = {
            hideProgressDialog()
            SharedPreferencesManager.put(
                SharedPreferencesManager.Keys.DRIVER_ONLINE, this?.isDriverOnline != 0
            )
            binding.swOnOff.isChecked = this?.isDriverOnline != 0
            SharedPreferencesManager.put(
                SharedPreferencesManager.Keys.DOCUMENT_APPROVED, this?.isDriverOnline != 0
            )
            binding.tvStatus.text =
                if (binding.swOnOff.isChecked) getString(R.string.txt_online) else getString(R.string.txt_offline)
            binding.clOffOn.isVisible = !binding.swOnOff.isChecked
            if (this?.trips?.isNotEmpty() == true) {
                this.trips.firstOrNull()?.let {
                    getLocationUpdates()
                    googleMap?.clearMap()
                    rideViewModel.newRideNotificationData = it
                    Log.e("RideStatus", "is ${it.status}")
                    if ((it.status ?: 0) == TripStatus.STARTED.type) {
                        screenType = 2
                        routeType = RideStateEnum.ON_THE_WAY.data
                        setScreenType(requireContext())
                    } else if ((it.status ?: 0) == TripStatus.ACCEPTED.type) {
                        screenType = 2
                        routeType = RideStateEnum.RIDE_ACCEPT.data
                        if (dialog?.isShowing == true) {
                            dialog?.dismiss()
                        }
                        setScreenType(requireContext())
                    } else if ((it.status ?: 0) == TripStatus.ARRIVED.type) {
                        screenType = 2
                        routeType = RideStateEnum.ARRIVE_AT_PICKUP.data
                        setScreenType(requireContext())
                    }
                }
                if (HomeActivity.isMsgNotification)
                    (activity as HomeActivity).startActivity(
                        Intent(
                            activity, ChatActivity::class.java
                        ).apply {
                            flags =
                                Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra(
                                "customerId",
                                "${rideViewModel.newRideNotificationData.customerId}"
                            )
                            putExtra("driverId", "$userId")
                            putExtra(
                                "engagementId",
                                "${rideViewModel.newRideNotificationData.tripId}"
                            )
                            putExtra(
                                "customerName",
                                "${rideViewModel.newRideNotificationData.customerName}"
                            )
                            putExtra(
                                "customerImage",
                                "${rideViewModel.newRideNotificationData.customerImage}"
                            )
                        }
                    )
            } else {
                HomeActivity.isMsgNotification = false
                screenType = 0
                setScreenType(requireContext())
                googleMap?.clearMap()
                SaloneDriver.latLng?.let {
                    googleMap?.addMarker(
                        MarkerOptions().position(
                            it
                        ).apply {
                            icon(requireActivity().vectorToBitmap(R.drawable.new_location_placeholder))
                            anchor(0.5f, 1f)
                        }
                    )
                }
                binding.bestRoute.clParent.isVisible = false
                fetchNewNotification()
                stopService()
            }
        }, onError = {
            binding.bestRoute.clParent.isVisible = false
            hideProgressDialog()
            showToastLong(this)
        })


    override fun onStop() {
        super.onStop()
        notificationInterface = null
    }


    /**
     * Observe Reject Trip
     * */
    private fun observeRejectTrip() =
        rideViewModel.rejectRideData.observeData(viewLifecycleOwner, onLoading = {
            showProgressDialog()
        }, onSuccess = {
            hideProgressDialog()
            dialog?.dismiss()
            SharedPreferencesManager.clearKeyData(SharedPreferencesManager.Keys.NEW_BOOKING)
        }, onError = {
            hideProgressDialog()
            showToastLong(this)
        })


    /**
     * Observe End Trip
     * */
    private fun observeEndTrip() =
        rideViewModel.endTrip.observeData(viewLifecycleOwner, onLoading = {
            showProgressDialog()
        }, onSuccess = {
            hideProgressDialog()
            dialog?.dismiss()
            screenType = 0
            routeType = 1
            setScreenType(requireContext())
            rideViewModel.newRideNotificationData.also {
                it.estimatedDriverFare = this?.estimatedDriverFare
                it.customerName = this?.customerName
                it.customerImage = this?.customerImage
                it.rideTime = this?.date
                it.paidUsingWallet = this?.paidUsingWallet
            }
            googleMap?.clear()
            binding.bestRoute.clParent.isVisible = false
            AppUtils.tripId = ""
            stopService()
            Handler(Looper.getMainLooper()).postDelayed({
                (activity as HomeActivity).startActivity(
                    Intent(activity, AcceptTripActivity::class.java).putExtra(
                        "screenType", "RideCompleted"
                    ).putExtra("rideData", rideViewModel.newRideNotificationData)
                )
            }, 200)
        }, onError = {
            hideProgressDialog()
            showToastLong(this)
        })


    /**
     * New Ride Notification
     * */
    override fun newRide() {
        super.newRide()
        Log.i("PUSHNOTI", "in notification detail HOME")
        requireActivity().runOnUiThread {
            fetchNewNotification()
        }
    }

    /**
     * Time Out
     * */
    override fun timeOutRide() {
        super.timeOutRide()
        requireActivity().runOnUiThread {
            try {
                requireActivity().stopService(Intent(requireActivity(), SoundService::class.java))
                Log.i("PUSHNOTI", "IN TIME OUT")
                AppUtils.tripId = ""
                rideViewModel.newRideNotificationData = NewRideNotificationDC()
                screenType = 0
                SharedPreferencesManager.clearKeyData(SharedPreferencesManager.Keys.NEW_BOOKING)
                dialog?.dismiss()
            } catch (e: Exception) {
                Log.i("PUSHNOTI", "IN TIME OUT:: ${e.message}")
                e.printStackTrace()
            }
        }
    }


    private fun fetchNewNotification() {
        SharedPreferencesManager.getModel<NewRideNotificationDC>(SharedPreferencesManager.Keys.NEW_BOOKING)
            ?.let {
                Log.i("PUSHNOTI", "in notification detail FETCHNOTI")
                rideViewModel.newRideNotificationData = it
                callTripsDialog(requireContext(), it)
            }
    }


    override fun checkOnGoingBooking() {
        SharedPreferencesManager.getModel<NewRideNotificationDC>(SharedPreferencesManager.Keys.NEW_BOOKING)
            ?.let {
                rideViewModel.newRideNotificationData = it
                callTripsDialog(requireContext(), it)
            } ?: run {
            requireActivity().stopService(Intent(requireActivity(), SoundService::class.java))
            rideViewModel.ongoingTrip()
        }
    }


    private fun homePageAlerts(
        @DrawableRes image: Int,
        message: String? = null,
        btnText: String? = null,
        callback: () -> Unit = {}
    ) {
        with(binding.alertView) {
            clRoot.isVisible = true
            ivImage.setImageResource(image)
            tvText.text = message
            tvButton.isVisible = btnText.orEmpty().isNotEmpty()
            tvButton.text = btnText.orEmpty()
            tvButton.setOnClickListener {
                clRoot.isVisible = false
                callback()
            }
        }
    }

    override fun walletUpdate() {
        super.walletUpdate()
        requireActivity().runOnUiThread {
            try {
                binding.alertView.clRoot.isVisible = false
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                viewModel.loginViaAccessToken(
                    SaloneDriver.latLng?.latitude ?: 0.0, SaloneDriver.latLng?.longitude ?: 0.0
                )
            }
        }
    }

    override fun scheduleRide() {
        super.scheduleRide()
        rideViewModel.ongoingTrip()
    }


    override fun onDestroy() {
        unregisterReceiver()
        super.onDestroy()
    }


    private fun stopService() {
        try {
            AppUtils.tripId = ""
            requireActivity().stopService(Intent(requireContext(), LocationService::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkAndRequestLocationPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Foreground location permission granted
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (ContextCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Background location permission granted
                        handleLocationPermissionGranted()
                    } else {
                        // Request background location permission
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            )
                        )
                    }
                } else {
                    // No need for background permission on older versions
                    handleLocationPermissionGranted()
                }
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Show rationale for foreground location permission
                showPermissionAreNotAllowed(true)
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION) -> {
                // Show rationale for foreground location permission
                showPermissionAreNotAllowed(true)
            }

            else -> {
                // Request foreground and background location permissions
                locationPermissionLauncher.launch(
                    arrayOf(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Manifest.permission.ACCESS_BACKGROUND_LOCATION else Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        }
    }

    private fun handleLocationPermissionGranted() {
        // Location permission granted, proceed with location-related tasks
//        Toast.makeText(requireActivity(), "Location permission granted", Toast.LENGTH_SHORT).show()
    }

    private fun handleLocationPermissionDenied() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        ) {
            // Permission denied permanently, redirect to settings
            AlertDialog.Builder(requireActivity())
                .setTitle("Permission Denied")
                .setMessage("All time location permission is denied permanently. Please go to settings to enable it.")
                .setPositiveButton("Settings") { dialog, _ ->
                    dialog.dismiss()
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", requireActivity().packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
                .setCancelable(false)
//                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
//                .create()
//                .show()
        } else {
            // Permission denied temporarily

        }
    }


    private fun showLocationPermissionRationale() {
        AlertDialog.Builder(requireActivity())
            .setTitle("Location Permission Required")
            .setMessage("This app requires location access to function properly.")
            .setCancelable(false)
            .setPositiveButton("Allow") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
//            .setNegativeButton("Deny") { dialog, _ ->
//                dialog.dismiss()
//                handleLocationPermissionDenied()
//            }
            .create()
            .show()
    }

    private var showBackgroundLocationPermissionRationaleClicked = false

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showBackgroundLocationPermissionRationale() {
        AlertDialog.Builder(requireActivity())
            .setTitle("All Time Location Permission Required")
            .setMessage("Allow all time location permission in permissions section from settings of this app")
            .setPositiveButton("Setting") { dialog, _ ->
                dialog.dismiss()
                showBackgroundLocationPermissionRationaleClicked = true
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setCancelable(false)
//            .setNegativeButton("Deny") { dialog, _ ->
//                dialog.dismiss()
//                handleLocationPermissionDenied()
//            }
            .create()
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showBackgroundLocationRequired() {
        AlertDialog.Builder(requireActivity())
            .setTitle("Background Location Permission Required")
            .setMessage("This app required all time location access to provide continuous location updates.")
            .setPositiveButton("Allow") { dialog, _ ->
                dialog.dismiss()
                checkAndRequestLocationPermissions()
            }
            .setCancelable(false)
//            .setNegativeButton("Deny") { dialog, _ ->
//                dialog.dismiss()
//                handleLocationPermissionDenied()
//            }
            .create()
            .show()
    }


}

interface CheckOnGoingBooking {
    fun checkOnGoingBooking()
}
