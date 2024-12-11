package com.superapp_customer.view.activity.walk_though.ui.home


import SwipeToShowDeleteCallback
import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
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
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.ncorti.slidetoact.SlideToActView
import com.superapp_customer.R
import com.superapp_customer.VenusApp
import com.superapp_customer.customClasses.LocationResultHandler
import com.superapp_customer.customClasses.SingleFusedLocation
import com.superapp_customer.customClasses.singleClick.setOnSingleClickListener
import com.superapp_customer.customClasses.trackingData.animateDriver
import com.superapp_customer.customClasses.trackingData.clearMap
import com.superapp_customer.customClasses.trackingData.showPath
import com.superapp_customer.customClasses.trackingData.vectorToBitmap
import com.superapp_customer.databinding.DialogDateTimeBinding
import com.superapp_customer.databinding.DialogShowPackagesBinding
import com.superapp_customer.databinding.FragmentHomeBinding
import com.superapp_customer.databinding.ItemBannersBinding
import com.superapp_customer.databinding.ItemPackageImagesBinding
import com.superapp_customer.databinding.ItemPackageListBinding
import com.superapp_customer.databinding.ItemSuggestionsBinding
import com.superapp_customer.dialogs.DialogUtils
import com.superapp_customer.firebaseSetup.NotificationInterface
import com.superapp_customer.model.api.getJsonRequestBody
import com.superapp_customer.model.api.observeData
import com.superapp_customer.model.dataClass.addedAddresses.SavedAddresse
import com.superapp_customer.model.dataClass.findDriver.FindDriverDC
import com.superapp_customer.model.dataClass.userData.UserDataDC
import com.superapp_customer.socketSetup.SocketInterface
import com.superapp_customer.socketSetup.SocketSetup
import com.superapp_customer.util.GenericAdapter
import com.superapp_customer.util.SharedPreferencesManager
import com.superapp_customer.util.TripStatus
import com.superapp_customer.util.constants.AppConstants
import com.superapp_customer.util.formatString
import com.superapp_customer.util.getBottomSheetBehaviour
import com.superapp_customer.util.safeCall
import com.superapp_customer.util.showSnackBar
import com.superapp_customer.view.activity.chat.ChatActivity
import com.superapp_customer.view.activity.walk_though.Home
import com.superapp_customer.view.activity.walk_though.PaymentActivity
import com.superapp_customer.view.base.BaseActivity
import com.superapp_customer.view.base.BaseFragment
import com.superapp_customer.viewmodel.HomeVM
import com.superapp_customer.viewmodel.SearchLocationVM
import com.superapp_customer.viewmodel.rideVM.CreateRideData
import com.superapp_customer.viewmodel.rideVM.RideVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

data class NearByDriverMarkers(val latLng: LatLng, val bearing: Float)
data class RideTypes(val drawable: Drawable?, val name: String, val type: Int)

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(), NotificationInterface, SocketInterface {
    lateinit var binding: FragmentHomeBinding
    private val rideVM by activityViewModels<RideVM>()
    private val homeViewModel by viewModels<HomeVM>()
    private val searchLocationVM by viewModels<SearchLocationVM>()
    private var locationAlertState: BottomSheetBehavior<View>? = null
    private var carTypeAlertState: BottomSheetBehavior<View>? = null
    private var carDetailAlertState: BottomSheetBehavior<View>? = null
    private var startRideAlertState: BottomSheetBehavior<View>? = null

    // private var deliveryVehicleTypeAlertState: BottomSheetBehavior<View>? = null
    private var googleMap: GoogleMap? = null
    private var nearByDriverMap: GoogleMap? = null
    private var isVehicleShowing = false

    private val bannerArrayList = ArrayList<UserDataDC.Login.Banners>()
    private val promoBannerArrayList = ArrayList<UserDataDC.Login.Banners>()
    private var nearByDriverLatLanArrayList = ArrayList<NearByDriverMarkers>()
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>
    private val addressAdapter by lazy {
        AddedAddressAdapter(
            requireActivity(), ::addressAdapterClick
        )
    }
    private var onGoingRideType = 0
    private val rideTypeArrayList = ArrayList<RideTypes>()
    private lateinit var rideTypeAdapter: RideTypeAdapter

    private lateinit var bannerAdapter: BannerAdapter
    private lateinit var promotionalBannerAdapter: BannerAdapter
    private var currentPage = 0
    private var currentPagePromotional = 0
    private val handler = Handler(Looper.getMainLooper())
    private val promoHandler = Handler(Looper.getMainLooper())

    private var showOfferAnimationOnce = true
    private val stateHandle: SavedStateHandle by lazy {
        findNavController().currentBackStackEntry?.savedStateHandle
            ?: throw IllegalStateException("State Handle is null")
    }

    private val slideRunnable = object : Runnable {
        override fun run() {
            if (bannerArrayList.isNotEmpty()) {
                currentPage = (currentPage + 1) % bannerArrayList.size
                binding.bannerViewPager.setCurrentItem(currentPage, true)
                handler.postDelayed(this, 3000) // 3 seconds
            } else
                handler.removeCallbacks(this)
        }
    }

    private val slideRunnablePromotions = object : Runnable {
        override fun run() {
            if (promoBannerArrayList.isNotEmpty()) {
                currentPagePromotional = (currentPagePromotional + 1) % promoBannerArrayList.size
                binding.promotionBannerViewPager.setCurrentItem(currentPagePromotional, true)
                promoHandler.postDelayed(this, 3000) // 3 seconds
            } else
                promoHandler.removeCallbacks(this)

        }
    }

    companion object {
        var notificationInterface: NotificationInterface? = null
        var selectedGoodType = -1
    }

    var rideStatus = AppConstants.DRIVER_PICKUP

    override fun initialiseFragmentBaseViewModel() {
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_home
    }

    private fun observeDeliveryState() = rideVM.delivery.observe(requireActivity()) {
        Log.i("RIDESTATE", "$it")
        if (it) {
            hideAllBottomSheets()
            rideVM.updateUiState(RideVM.RideAlertUiState.FindDriverDialog)
//            isVehicleShowing = true
//            startRepeatingJob()
        } else {

        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("HOMEFRAGMENT", "onViewCreated")
        binding = getViewDataBinding()
//        rideVM.createRideData = CreateRideData()
        setBannerAdapter(view.context)
        setMapFragment(view, savedInstanceState)
        getSavedStateData()
        observeUiState()
        observeFindDriver()
        observeFindDriverInLoop()
        observeRequestRide()
        observeRequestSchedule()
        observeFareEstimate()
        observePromoCode()
        observeFetchOngoingTrip()
        observeNearDriver()
        clickHandler()
        backButtonMapView()
        observeAddedAddress()
        addAdapterSetupAndApiHit()
        getNearByDrivers()
        observeSOS()
        observeDeliveryState()
        observeProfileData()
        rideVM.rideAlertUiState.value?.let { uiStateHandler(it) }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                val phone = rideVM.createRideData.driverDetail?.driverPhoneNo ?: ""
                if (phone.isEmpty()) showSnackBar("There is some issue in call. Please try after sometimes.")
                else makePhoneCall(phone) // Replace with the phone number you want to call
            } else {
                DialogUtils.getPermissionDeniedDialog(
                    requireActivity(),
                    1,
                    getString(R.string.allow_call_permission),
                    ::onDialogCallPermissionAllowClick
                )
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireActivity().registerReceiver(
                showMessageIndicatorBroadcastReceiver,
                IntentFilter("newMsg"),
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            requireActivity().registerReceiver(
                showMessageIndicatorBroadcastReceiver, IntentFilter("newMsg")
            )
        }
        if (VenusApp.isReferee) {
            startConfettiAnimation()
            VenusApp.isReferee = false
        }
        if (VenusApp.referralMsg.isNotEmpty()) {
            showSnackBar(VenusApp.referralMsg)
            VenusApp.referralMsg = ""
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
        Glide.with(requireActivity()).asGif().load(R.drawable.promotional_gif).into(binding.ivGif)

        lifecycleScope.launch {
            getLocationDataFromLatLng(VenusApp.latLng,true)
        }
    }


    private fun setupRideTypeAndBanner() {
        rideTypeArrayList.clear()
        bannerArrayList.clear()
        if (SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID) == 1) {
//            rideTypeArrayList.add(
//                RideTypes(
//                    ContextCompat.getDrawable(
//                        requireActivity(),
//                        R.drawable.ic_ride_now
//                    ), "Now", 1
//                )
//            )
//            rideTypeArrayList.add(
//                RideTypes(
//                    ContextCompat.getDrawable(
//                        requireActivity(),
//                        R.drawable.ic_ride_schedule
//                    ), "Schedule", 2
//                )
//            )
//            rideTypeArrayList.add(
//                RideTypes(
//                    ContextCompat.getDrawable(
//                        requireActivity(),
//                        R.drawable.ic_outstation
//                    ), "Outstation", 3
//                )
//            )
//            rideTypeArrayList.add(
//                RideTypes(
//                    ContextCompat.getDrawable(
//                        requireActivity(),
//                        R.drawable.ic_rental
//                    ), "Rental", 4
//                )
//            )
//            ContextCompat.getDrawable(requireActivity(), R.drawable.ride_banner)
//                ?.let { bannerArrayList.add(it) }
//            ContextCompat.getDrawable(requireActivity(), R.drawable.ride_banner)
//                ?.let { bannerArrayList.add(it) }
        } else {
            binding.ivSuggestions.setImageDrawable(
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.ic_delivery_now
                )
            )
            binding.ivRideSchedule.setImageDrawable(
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.ic_delivery_schedule
                )
            )
            binding.ivRideOutStation.setImageDrawable(
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.ic_delivery_outstation
                )
            )
            binding.ivRideRental.setImageDrawable(
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.ic_delivery_truck
                )
            )
            binding.tvRideRental.text = "Truck"


//            rideTypeArrayList.add(
//                RideTypes(
//                    ContextCompat.getDrawable(
//                        requireActivity(),
//                        R.drawable.ic_delivery_now
//                    ), "Now", 1
//                )
//            )
//            rideTypeArrayList.add(
//                RideTypes(
//                    ContextCompat.getDrawable(
//                        requireActivity(),
//                        R.drawable.ic_delivery_schedule
//                    ), "Schedule", 2
//                )
//            )
//            rideTypeArrayList.add(
//                RideTypes(
//                    ContextCompat.getDrawable(
//                        requireActivity(),
//                        R.drawable.ic_delivery_outstation
//                    ), "Outstation", 3
//                )
//            )
//            rideTypeArrayList.add(
//                RideTypes(
//                    ContextCompat.getDrawable(
//                        requireActivity(),
//                        R.drawable.ic_delivery_truck
//                    ), "Truck", 4
//                )
//            )
//            ContextCompat.getDrawable(requireActivity(), R.drawable.delivery_banner)
//                ?.let { bannerArrayList.add(it) }
//            ContextCompat.getDrawable(requireActivity(), R.drawable.delivery_banner)
//                ?.let { bannerArrayList.add(it) }
        }
//        rideTypeAdapter = RideTypeAdapter()
//        binding.rvSuggestions.adapter = rideTypeAdapter


        bannerAdapter = BannerAdapter(bannerArrayList)
        promotionalBannerAdapter = BannerAdapter(promoBannerArrayList)

        binding.bannerViewPager.adapter = bannerAdapter
        binding.promotionBannerViewPager.adapter = promotionalBannerAdapter

        binding.tabLayoutDots.setSelectedTabIndicatorColor(Color.WHITE)
        binding.tabLayoutDotsPromo.setSelectedTabIndicatorColor(Color.WHITE)

        TabLayoutMediator(
            binding.tabLayoutDotsPromo, binding.promotionBannerViewPager
        ) { tab, position ->
            val unselectedIcon =
                ContextCompat.getDrawable(requireContext(), R.drawable.banner_indicator_unselected)
            unselectedIcon?.setTintMode(PorterDuff.Mode.SRC_IN)
            tab.setIcon(unselectedIcon)
            tab.icon?.setTint(ContextCompat.getColor(requireContext(), R.color.white))
        }.attach()

        TabLayoutMediator(
            binding.tabLayoutDots, binding.bannerViewPager
        ) { tab, position ->
            val unselectedIcon =
                ContextCompat.getDrawable(requireContext(), R.drawable.banner_indicator_unselected)
            unselectedIcon?.setTintMode(PorterDuff.Mode.SRC_IN)
            tab.setIcon(unselectedIcon)
            tab.icon?.setTint(ContextCompat.getColor(requireContext(), R.color.white))
        }.attach()


        // Customize dot appearance based on selection
        binding.promotionBannerViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                for (i in 0 until binding.tabLayoutDotsPromo.tabCount) {
                    val unselectedIcon = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.banner_indicator_unselected
                    )
                    unselectedIcon?.setTintMode(PorterDuff.Mode.SRC_IN)
                    val selectedIcon = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.banner_indicator_selected
                    )
                    selectedIcon?.setTintMode(PorterDuff.Mode.SRC_IN)
                    binding.tabLayoutDotsPromo.getTabAt(i)?.setIcon(
                        if (i == position) selectedIcon else unselectedIcon
                    )
                    binding.tabLayoutDotsPromo.getTabAt(i)?.icon?.setTint(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                }
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                for (i in 0 until binding.tabLayoutDotsPromo.tabCount) {
                    val unselectedIcon = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.banner_indicator_unselected
                    )
                    unselectedIcon?.setTintMode(PorterDuff.Mode.SRC_IN)
                    val selectedIcon = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.banner_indicator_selected
                    )
                    selectedIcon?.setTintMode(PorterDuff.Mode.SRC_IN)
                    binding.tabLayoutDotsPromo.getTabAt(i)?.setIcon(
                        if (i == position) selectedIcon else unselectedIcon
                    )
                    binding.tabLayoutDotsPromo.getTabAt(i)?.icon?.setTint(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                }
            }
        })



        TabLayoutMediator(
            binding.tabLayoutDots, binding.bannerViewPager
        ) { tab, position ->
            val unselectedIcon =
                ContextCompat.getDrawable(requireContext(), R.drawable.banner_indicator_unselected)
            unselectedIcon?.setTintMode(PorterDuff.Mode.SRC_IN)
            tab.setIcon(unselectedIcon)
            tab.icon?.setTint(ContextCompat.getColor(requireContext(), R.color.white))
        }.attach()
        // Customize dot appearance based on selection
        binding.bannerViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                for (i in 0 until binding.tabLayoutDots.tabCount) {
                    val unselectedIcon = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.banner_indicator_unselected
                    )
                    unselectedIcon?.setTintMode(PorterDuff.Mode.SRC_IN)
                    val selectedIcon = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.banner_indicator_selected
                    )
                    selectedIcon?.setTintMode(PorterDuff.Mode.SRC_IN)
                    binding.tabLayoutDots.getTabAt(i)?.setIcon(
                        if (i == position) selectedIcon else unselectedIcon
                    )
                    binding.tabLayoutDots.getTabAt(i)?.icon?.setTint(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                }
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                for (i in 0 until binding.tabLayoutDots.tabCount) {
                    val unselectedIcon = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.banner_indicator_unselected
                    )
                    unselectedIcon?.setTintMode(PorterDuff.Mode.SRC_IN)
                    val selectedIcon = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.banner_indicator_selected
                    )
                    selectedIcon?.setTintMode(PorterDuff.Mode.SRC_IN)
                    binding.tabLayoutDots.getTabAt(i)?.setIcon(
                        if (i == position) selectedIcon else unselectedIcon
                    )
                    binding.tabLayoutDots.getTabAt(i)?.icon?.setTint(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                }
            }
        })
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

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                if (result.data?.hasExtra("tripId") == true) {
                    val tripId = result.data?.getStringExtra("tripId")
                    val driverId = result.data?.getStringExtra("driverId")
                    val driverName = result.data?.getStringExtra("driverName")
                    val engagementId = result.data?.getStringExtra("engagementId")
                    findNavController().navigate(
                        R.id.navigation_rate_driver, bundleOf(
                            "engagementId" to tripId,
                            "driverName" to driverName,
                            "driverId" to driverId,
                            "tripId" to tripId
                        )
                    )
                } else {
                    requireActivity().runOnUiThread {
                        binding.clWhereMain.visibility = View.VISIBLE
                        binding.clMapMain.visibility = View.GONE
                        hideAllBottomSheets()
                        rideVM.hideHomeNav(false)
                        rideVM.updateUiState(RideVM.RideAlertUiState.HomeScreen)
                    }
                    rideVM.fetchOngoingTrip()
                }
            } catch (e: Exception) {
            }
        }
    }

    private val activityResultLauncherForPayment = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                rideVM.cardId = result.data?.getStringExtra("cardId") ?: ""
                rideVM.last4 = result.data?.getStringExtra("last4") ?: ""
                binding.viewCarDetail.tvSelectedCard.isVisible = true
                binding.viewCarDetail.tvChangeCard.isVisible = true
                binding.viewCarDetail.tvSelectedCard.text = "Selected card: **** ${rideVM.last4}"
            } catch (e: Exception) {
            }
        }
    }

    private fun getNearByDrivers() {
        if (VenusApp.latLng.latitude != 0.0) {
            rideVM.findNearDriver(VenusApp.latLng.latitude, VenusApp.latLng.longitude)
        } else {
            var alreadyHittingApi = false
//        rideVM.updateUiState(RideVM.RideAlertUiState.HomeScreen)
            SingleFusedLocation.initialize(requireContext(), object : LocationResultHandler {
                override fun updatedLocation(location: Location) {
                    if (!alreadyHittingApi) {
                        alreadyHittingApi = true
                        nearByDriverMap?.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    location.latitude, location.longitude
                                ), 12f
                            )
                        )
                        rideVM.findNearDriver(location.latitude, location.longitude)
                    }
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        viewLifecycleOwner.lifecycle.removeObserver(object : DefaultLifecycleObserver {
//            override fun onResume(owner: LifecycleOwner) {
//                super.onResume(owner)
//                getSavedStateData()
//            }
//        })
        handler.removeCallbacks(slideRunnable)
        promoHandler.removeCallbacks(slideRunnablePromotions)
    }

    private fun addAdapterSetupAndApiHit() {
        binding.viewLocation.rvAddress.adapter = addressAdapter
        val swipeToDeleteCallback = SwipeToShowDeleteCallback(addressAdapter, requireActivity())
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.viewLocation.rvAddress)
        rideVM.fetchAddedAddresses()
    }

    override fun onResume() {
        super.onResume()
        checkAndRequestNotificationPermission()
        notificationInterface = this
        SocketSetup.initializeInterface(this)
        SocketSetup.connectSocket()
        setupRideTypeAndBanner()
        homeViewModel.loginViaToken2()
    }

    private fun startRepeatingJob() {
        lifecycleScope.launch {
            while (true) {
                withContext(Dispatchers.IO) {
                    Log.i("CarType", "Api hit")
                    rideVM.findDriverInLoop(rideVM.schedule)
                }
                delay(10000) // 15 seconds
            }
        }
    }

    private fun hideAllBottomSheets() {
        Log.i("SavedStateData", "in hideAllBottomSheets")
        locationAlertState = binding.viewLocation.clLocationAlert.getBottomSheetBehaviour(
            isDraggableAlert = true, showFull = true
        )
        carTypeAlertState = binding.viewCarType.clRootView.getBottomSheetBehaviour(
            isDraggableAlert = true, showFull = true
        )

        carDetailAlertState =
            binding.viewCarDetail.clRootView.getBottomSheetBehaviour(isDraggableAlert = false, true)
        startRideAlertState = binding.viewStartRide.clRootView.getBottomSheetBehaviour()
//        deliveryVehicleTypeAlertState =
//            binding.viewDeliveryVehicleType.clRootView.getBottomSheetBehaviour()
        carTypeAlertState?.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        isVehicleShowing = false
                        lifecycleScope.coroutineContext.cancelChildren()
                    }

                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        // Bottom sheet is collapsed
                        Log.i("CarType", "in STATE_COLLAPSED")
                    }

                    BottomSheetBehavior.STATE_DRAGGING -> {
                        // User is dragging the bottom sheet down
                        Log.i("CarType", "in STATE_DRAGGING")
                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        // Bottom sheet is expanded
                        Log.i("CarType", "in STATE_EXPANDED")
                        isVehicleShowing = true
                        startRepeatingJob()
                    }

                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        Log.i("CarType", "in STATE_HALF_EXPANDED")
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
        })
        locationAlertState?.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        rideVM.hideHomeNav(false)
                        showOfferAnimationOnce = true
                        rideVM.cardId = ""
                        rideVM.last4 = ""
                        rideVM.couponToApply = 0
                        rideVM.promoCode = ""
                        rideVM.updateUiState(RideVM.RideAlertUiState.HomeScreen)
                    }

                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        // Bottom sheet is collapsed
                        Log.i("SavedStateData", "in STATE_COLLAPSED")
                    }

                    BottomSheetBehavior.STATE_DRAGGING -> {
                        // User is dragging the bottom sheet down
                        Log.i("SavedStateData", "in STATE_DRAGGING")
                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        // Bottom sheet is expanded
                        Log.i("SavedStateData", "in STATE_EXPANDED")
                    }

                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        rideVM.hideHomeNav(false)
                        // Bottom sheet is expanded
                        Log.i("SavedStateData", "in STATE_HALF_EXPANDED")
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // When the bottom sheet is sliding, you can handle any custom behavior here
            }
        })
    }

    private fun clearSavedStateData() {
        val clearStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        clearStateHandle?.let {
            it.remove<CreateRideData.LocationData>("pickUpLocation")
            it.remove<CreateRideData.LocationData>("dropLocation")
            it.remove<String>("add_address")
        } ?: run {
            Log.e("ClearStateError", "Failed to access savedStateHandle")
        }
    }

    private fun getSavedStateData() {
        try {
            Log.i("SavedStateData", "in getSavedStateData()")
            findNavController().currentBackStackEntry?.savedStateHandle?.let {
                if (it.contains("pickUpLocation")) {
                    Log.i("SavedStateData", "in pickUpLocation")
                    rideVM.createRideData.pickUpLocation =
                        it.get<CreateRideData.LocationData>("pickUpLocation")
                    rideVM.updateUiState(RideVM.RideAlertUiState.ShowLocationDialog)
                }
                if (it.contains("dropLocation")) {
                    Log.i("SavedStateData", "in dropLocation")
                    rideVM.createRideData.dropLocation =
                        it.get<CreateRideData.LocationData>("dropLocation")
                    rideVM.updateUiState(RideVM.RideAlertUiState.ShowLocationDialog)
                }
                if (it.contains("add_address")) {
                    Log.i("SavedStateData", "in add_address")
                    rideVM.updateUiState(RideVM.RideAlertUiState.ShowLocationDialog)
                }
//                hideAllBottomSheets()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            hideAllBottomSheets()
        }
        hideAllBottomSheets()
    }

    private fun clickHandler() {
//        binding.rlRide.setOnSingleClickListener {
////            binding.tvNow.text = requireContext().getString(R.string.schedule)
//            rideVM.createRideData = CreateRideData()
//            schedule = false
//            rideVM.updateUiState(RideVM.RideAlertUiState.ShowLocationDialog)
//        }
//
//
        binding.rlRideSchedule.setOnSingleClickListener {
//            binding.tvNow.text = requireContext().getString(R.string.schedule)
            rideVM.createRideData = CreateRideData()
            hideAllBottomSheets()
            startTimeDialog(requireContext())
        }
//        binding.tvWhereTo.setOnSingleClickListener {
//            binding.tvNow.text = requireContext().getString(R.string.txt_now)
//            rideVM.createRideData = CreateRideData()
//            schedule = false
//            rideVM.updateUiState(RideVM.RideAlertUiState.ShowLocationDialog)
//        }
        binding.rlRide.setOnSingleClickListener {
//            binding.tvNow.text = requireContext().getString(R.string.txt_now)
            rideVM.createRideData = CreateRideData()
            rideVM.schedule = false
            rideVM.updateUiState(RideVM.RideAlertUiState.ShowLocationDialog)
        }

        Log.i("rideAlertUiState", "on home  ${rideVM.rideAlertUiState.value}")

        if (rideVM.rideAlertUiState.value == RideVM.RideAlertUiState.HomeScreen || rideVM.rideAlertUiState.value == RideVM.RideAlertUiState.ShowLocationDialog) {
            rideVM.fetchOngoingTrip()
        } else if (rideVM.rideAlertUiState.value == RideVM.RideAlertUiState.FindDriverDialog) {
            binding.clWhereMain.visibility = View.GONE
            binding.clMapMain.visibility = View.VISIBLE
            startRideDialog(requireContext(), RideVM.RideAlertUiState.FindDriverDialog)
            startRideAlertState?.state = BottomSheetBehavior.STATE_EXPANDED
        }
        binding.tvSOS.setOnSingleClickListener {
            rideVM.sosApi(rideVM.createRideData.tripId ?: "")
        }
    }

    private fun observeSOS() =
        rideVM.sosData.observeData(lifecycle = viewLifecycleOwner, onLoading = {
//            showProgressDialog()
        }, onSuccess = {
//            hideProgressDialog()
//            if (activity != null) {
            binding.tvSOS.isVisible = false
//            }
        }, onError = {
//            hideProgressDialog()
            showToastShort(this)
        })

    private fun startWhereDialog() {
        with(binding.viewLocation) {
            tvPickUpValue.text = ""
            tvDropOffValue.text = ""
            clLocationAlert.setOnTouchListener { view, motionEvent ->
                true
            }
            rideVM.createRideData.pickUpLocation?.address?.let {
                tvPickUpValue.text = it
            }
            rideVM.createRideData.dropLocation?.address?.let {
                tvDropOffValue.text = it
            }

            tvConfirmBtn.setOnSingleClickListener {
                if (rideVM.createRideData.pickUpLocation?.address.isNullOrEmpty()) {
                    showSnackBar("*Please select pickup location.", clLocationAlert)
                } else if (rideVM.createRideData.dropLocation?.address.isNullOrEmpty()) {
                    showSnackBar("*Please select drop location.", clLocationAlert)
                } else {
                    locationAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
                    rideVM.findDriver(rideVM.schedule)
                }
            }

            llAddAddress.setOnSingleClickListener {
                locationAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
                val bundle = bundleOf("selectLocationType" to "add_address")
                findNavController().navigate(R.id.navigation_select_location, bundle)
            }

            tvSelectPickUp.setOnSingleClickListener {
                locationAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
                val bundle = bundleOf("selectLocationType" to "pickUp")
                findNavController().navigate(R.id.navigation_select_location, bundle)
            }

            tvSelectDropOff.setOnSingleClickListener {
                locationAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
                val bundle = bundleOf("selectLocationType" to "dropOff")
                findNavController().navigate(R.id.navigation_select_location, bundle)
            }

            tvPickUpValue.setOnSingleClickListener {
                locationAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
                val bundle = bundleOf("selectLocationType" to "pickUp")
                findNavController().navigate(R.id.navigation_select_location, bundle)
            }
            tvDropOffValue.setOnSingleClickListener {
                locationAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
                val bundle = bundleOf("selectLocationType" to "dropOff")
                findNavController().navigate(R.id.navigation_select_location, bundle)
            }
        }
    }

    private fun carDetailDialog(context: Context) {
        with(binding.viewCarDetail) {
            rideVM.createRideData.vehicleData?.let {
                when (SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID)) {
                    1 -> {
                        etNotes.isVisible = true
                        tvNumber.isVisible = true
                        tvCustomerNameValue.isVisible = true
                        tvContactDetails.isVisible = true
                        tvCustomerName.isVisible = true
                    }

                    2 -> {
                        etReceiverName.isVisible = true
                        etReceiverNumber.isVisible = true
                        etSelectGoods.isVisible = false
                    }
                }

                if (rideVM.cardId.isNotEmpty()) {
                    tvSelectedCard.isVisible = true
                    tvChangeCard.isVisible = true
                    tvSelectedCard.text = "Selected card: **** ${rideVM.last4}"
                }
                tvCarType.text = it.name.orEmpty()
                tvTime.text = it.eta.orEmpty().ifEmpty { "0" }.plus(" min away")
                tvPersonCount.text = it.totalCapacity.orEmpty().ifEmpty { "0" }
//                tvPrice.text =
//                    "${it.price.formatString()} ${rideVM.createRideData.currencyCode.orEmpty()}"
                if ((it.discount ?: 0.0) > 0.0) {
                    tvOriginalPrice.isVisible = true
                    viewCross.isVisible = true
                    tvOriginalPrice.text = "${it.currency} ${it.original_fare}"
                    tvPrice.text = "${it.currency} ${it.fare}"
                    tvOfferTitle.text = VenusApp.offerTitle
                    tvOfferTitle.isVisible = true
                } else {
                    tvOriginalPrice.isVisible = false
                    viewCross.isVisible = false
                    tvPrice.text = "${it.currency} ${it.fare}"
                    tvOfferTitle.isVisible = false
                }

                Glide.with(requireContext()).load(it.image.orEmpty()).error(R.mipmap.ic_launcher)
                    .into(ivCarImage)
            }

            SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
                ?.let {
                    tvCustomerNameValue.text = it.login?.userName.orEmpty()
                    tvNumber.text = it.login?.phoneNo.orEmpty()
                }

            tvConfirmBtn.setOnSingleClickListener {
                if (rideVM.schedule) {
                    val operatorId =
                        SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID)
                    if (operatorId == 2 && etReceiverName.text.toString().trim().isEmpty())
                        showSnackBar("Please enter Receiver Name", tvConfirmBtn)
                    else if (operatorId == 2 && etReceiverNumber.text.toString().trim().isEmpty())
                        showSnackBar("Please enter Receiver Phone Number", tvConfirmBtn)
//                    else if (operatorId == 2 && etSelectGoods.text.toString().trim().isEmpty())
//                        showSnackBar("Please select Goods Type", tvConfirmBtn)
                    else if (rideVM.paymentOption == 9 && rideVM.cardId.isEmpty())
                        showSnackBar("*Please select card for payment*", tvConfirmBtn)
                    else {
                        carDetailAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
                        val notes = if (operatorId == 1) etNotes.text?.trim()
                            .toString() else etSelectGoods.text?.trim().toString()
                        rideVM.scheduleRideData(
                            receiverName = etReceiverName.text.toString().trim(),
                            receiverNumber = etReceiverNumber.text.toString().trim(),
                            notes,
                            rideVM.selectedPickDateTimeForSchedule
                        )
                        etNotes.clearFocus()
                        etNotes.setText("")
                        etReceiverName.clearFocus()
                        etReceiverName.setText("")
                        etReceiverNumber.clearFocus()
                        etReceiverNumber.setText("")
                        etSelectGoods.clearFocus()
                        etSelectGoods.setText("")
                        selectedGoodType = -1
                    }
                } else {
                    val operatorId =
                        SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID)
                    if (operatorId == 2 && etReceiverName.text.toString().trim().isEmpty())
                        showSnackBar("Please enter Receiver Name", tvConfirmBtn)
                    else if (operatorId == 2 && etReceiverNumber.text.toString().trim().isEmpty())
                        showSnackBar("Please enter Receiver Phone Number", tvConfirmBtn)
//                    else if (operatorId == 2 && etSelectGoods.text.toString().trim().isEmpty())
//                        showSnackBar("Please select Goods Type", tvConfirmBtn)
                    else if (rideVM.paymentOption == 9 && rideVM.cardId.isEmpty())
                        showSnackBar("*Please select card for payment*", tvConfirmBtn)
                    else {
                        carDetailAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
                        val notes = if (operatorId == 1) etNotes.text?.trim()
                            .toString() else etSelectGoods.text?.trim().toString()
                        rideVM.requestRideData(
                            receiverName = etReceiverName.text.toString().trim(),
                            receiverNumber = etReceiverNumber.text.toString().trim(), notes
                        )
                        etNotes.clearFocus()
                        etNotes.setText("")
                        etReceiverName.clearFocus()
                        etReceiverName.setText("")
                        etReceiverNumber.clearFocus()
                        etReceiverNumber.setText("")
                        etSelectGoods.clearFocus()
                        etSelectGoods.setText("")
                        selectedGoodType = -1
                    }
                }
            }
            tvRemove.setOnSingleClickListener {
                tvPromoCode.text = getString(R.string.txt_apply_promo_code)
                rideVM.couponToApply = 0
                tvRemove.isVisible = false
            }

            tvPromoCode.setOnSingleClickListener {
                DialogUtils.getPromoDialog(
                    requireActivity(), ::onDialogClick
                )
            }
            clPayByCash.setOnSingleClickListener {
                changeSelection(1)
            }
            clPayByCard.setOnSingleClickListener {
                changeSelection(0)
                if (rideVM.cardId.isEmpty())
                    activityResultLauncherForPayment.launch(
                        Intent(
                            requireActivity(),
                            PaymentActivity::class.java
                        ).putExtra("whileRide", true)
                    )
            }
            tvChangeCard.setOnSingleClickListener {
                activityResultLauncherForPayment.launch(
                    Intent(
                        requireActivity(),
                        PaymentActivity::class.java
                    ).putExtra("whileRide", true).putExtra("cardId", rideVM.cardId)
                )
            }
            etSelectGoods.setOnSingleClickListener {
                DialogUtils.getGoodsTypeDialog(
                    requireActivity(),
                    ::onGoodClick
                )
            }
            changeSelection(1)
        }
    }

    private fun onGoodClick(good: String) {
        binding.viewCarDetail.etSelectGoods.setText(good)
    }

    private fun changeSelection(selection: Int) {
        binding.viewCarDetail.ivPayByCash.setImageDrawable(
            ContextCompat.getDrawable(
                requireActivity(), R.drawable.bg_circular_stroke_black
            )
        )
        binding.viewCarDetail.ivPayByCard.setImageDrawable(
            ContextCompat.getDrawable(
                requireActivity(), R.drawable.bg_circular_stroke_black
            )
        )
        if (selection == 1) {
            rideVM.paymentOption = 1
            binding.viewCarDetail.ivPayByCash.setImageDrawable(
                ContextCompat.getDrawable(
                    requireActivity(), R.drawable.ic_tick_circle
                )
            )
        } else {
            rideVM.paymentOption = 9
            binding.viewCarDetail.ivPayByCard.setImageDrawable(
                ContextCompat.getDrawable(
                    requireActivity(), R.drawable.ic_tick_circle
                )
            )
        }
    }

    private fun onDialogClick(promoCode: String) {
        rideVM.enterPromoCode(
            promoCode,
            regionId = rideVM.createRideData?.regionId ?: "",
            vehicleType = rideVM.createRideData?.vehicleType ?: "",
            fare = rideVM.createRideData?.vehicleData?.fare ?: "",
            distance = rideVM.createRideData?.vehicleData?.distance ?: "",
            currency = rideVM.createRideData?.vehicleData?.currency ?: ""
        )
    }


    private fun setMapFragment(view: View, savedInstanceState: Bundle?) {
        val smallMapFragment =
            childFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
        val fragmentMapFullScreen =
            childFragmentManager.findFragmentById(R.id.fragmentMapFullScreen) as SupportMapFragment
        smallMapFragment.getMapAsync {
            nearByDriverMap = it
        }
        fragmentMapFullScreen.getMapAsync {
            googleMap = it
        }
//        MapsInitializer.initialize(view.context, MapsInitializer.Renderer.LATEST) {
//            binding.fragmentMap.onCreate(savedInstanceState)
//            binding.fragmentMapFullScreen.onCreate(savedInstanceState)
//            binding.fragmentMap.getMapAsync {
//                nearByDriverMap = it
//                OnMapReadyCallback {
//                    it.moveCamera(CameraUpdateFactory.newLatLng(LatLng(5454.1, 556.9)))
//                }
//            }
//            binding.fragmentMapFullScreen.getMapAsync {
//                googleMap = it
//            }
//        }
    }

    //    private var selectedPickDateTimeForSchedule = ""
    private fun startTimeDialog(context: Context) {
        val dialog = BottomSheetDialog(context, R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = DialogDateTimeBinding.inflate(LayoutInflater.from(context), null, false)
        dialog.setContentView(binding.root)


//        binding.cvPickDrop.visibility = if (schedule) View.VISIBLE else View.GONE
//        binding.clHome.visibility = if (schedule) View.VISIBLE else View.GONE

//        rideVM.createRideData.pickUpLocation?.address?.let {
//            binding.tvPickUpValue.text = it
//        }
//        rideVM.createRideData.dropLocation?.address?.let {
//            binding.tvDropOffValue.text = it
//        }

        setCurrentDate(binding.tvDateValue)
        setCurrentTime(binding.tvTimeValue)
        // Show date picker dialog
        binding.tvDateValue.setOnSingleClickListener {
            showDatePickerDialog(binding.tvDateValue)
        }
        binding.tvTimeValue.setOnSingleClickListener {
            showTimePickerDialog(binding.tvTimeValue)
        }
        binding.tvSubmitBtn.setOnSingleClickListener {
//            this@HomeFragment.binding.tvNow.text = context.getString(R.string.schedule)
            val past =
                isDateTimeInPast(binding.tvDateValue.text.toString() + " " + binding.tvTimeValue.text.toString())
            if (past) {
                showSnackBar(
                    "Time must be at least 30 minutes from the current time", binding.clRoot
                )
            } else {
                rideVM.schedule = true
                rideVM.selectedPickDateTimeForSchedule =
                    convertToUTC(binding.tvDateValue.text.toString() + " " + binding.tvTimeValue.text.toString())
                dialog.dismiss()
                rideVM.updateUiState(RideVM.RideAlertUiState.ShowLocationDialog)
            }
        }


//        binding.clHome.setOnSingleClickListener {
//            dialog.dismiss()
//            val bundle = bundleOf("selectLocationType" to "add_address")
//            findNavController().navigate(R.id.navigation_select_location, bundle)
//        }
//        binding.tvSelectPickUp.setOnSingleClickListener {
//            dialog.dismiss()
//            val bundle = bundleOf("selectLocationType" to "pickUp")
//            findNavController().navigate(R.id.navigation_select_location, bundle)
//        }
//        binding.tvSelectDropOff.setOnSingleClickListener {
//            dialog.dismiss()
//            val bundle = bundleOf("selectLocationType" to "dropOff")
//            findNavController().navigate(R.id.navigation_select_location, bundle)
//
//        }
//
//        binding.tvPickUpValue.setOnSingleClickListener {
//            dialog.dismiss()
//            val bundle = bundleOf("selectLocationType" to "pickUp")
//
//            findNavController().navigate(R.id.navigation_select_location, bundle)
//        }
//        binding.tvDropOffValue.setOnSingleClickListener {
//            dialog.dismiss()
//            val bundle = bundleOf("selectLocationType" to "dropOff")
//            findNavController().navigate(R.id.navigation_select_location, bundle)
//
//        }

        dialog.setCancelable(true)
        dialog.show()
    }

    private fun isDateTimeInPast(dateTimeString: String): Boolean {
        // Get the current year
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        // Append the current year to the dateTimeString
        val dateTimeWithYear = "$dateTimeString $currentYear"
        Log.i("SelectedDateWithYear", dateTimeWithYear)

        // Define the input format
        val inputFormat = SimpleDateFormat("E, MMM dd hh:mm a yyyy", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getDefault() // Ensure the time zone is set correctly

        // Parse the input date string to a Date object
        val date: Date = inputFormat.parse(dateTimeWithYear)
            ?: throw IllegalArgumentException("Invalid date and time format")

        // Get the current date and time with 30 minutes added
        val currentCalendar = Calendar.getInstance()
        currentCalendar.add(Calendar.MINUTE, 30)

        // Logging for debugging
        Log.i("CurrentDate", currentCalendar.time.toString())
        Log.i("ParsedDate", date.toString())

        // Compare the parsed date with the current date
        return date.before(currentCalendar.time)
    }

    private fun convertToUTC(dateString: String): String {
        // Define the input format
        // Get the current year
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        // Append the current year to the dateTimeString
        val dateTimeWithYear = "$dateString $currentYear"
        val inputFormat = SimpleDateFormat("E, MMM dd hh:mm a yyyy", Locale.getDefault())
        // Parse the input date string to a Date object
        val date: Date = inputFormat.parse(dateTimeWithYear)
        // Define the output format as UTC
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getTimeZone("UTC")
        // Convert the Date object to a UTC formatted string
        return outputFormat.format(date)
    }


    private fun showTimePickerDialog(textView: TextView) {
        val calendar = Calendar.getInstance()
        // Time 30 minutes ahead from the current time
        calendar.add(Calendar.MINUTE, 30)
        val initialHour = calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinute = calendar.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(
            requireContext(), { _, selectedHour, selectedMinute ->
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                val formattedTime =
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
                textView.text = formattedTime
            }, initialHour, initialMinute, false // `false` for 12-hour format
        )
//        timePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setOnSingleClickListener {
//            showToastShort("$shour$sMin")
//            val selectedCalendar = Calendar.getInstance()
//            selectedCalendar.set(Calendar.HOUR_OF_DAY, selectedHour)
//            selectedCalendar.set(Calendar.MINUTE, selectedMinute)
//
//        }
        timePickerDialog.show()
    }

    private fun setCurrentDate(textView: TextView) {
        // Set the minimum date to the current date
        val minDate = Calendar.getInstance()
        val dateFormatter = SimpleDateFormat("E, MMM dd", Locale.getDefault())
        val formattedDate = dateFormatter.format(minDate.time)
        textView.text = formattedDate
    }

    private fun setCurrentTime(textView: TextView) {
        val calendar = Calendar.getInstance()
        // Time 30 minutes ahead from the current time
        calendar.add(Calendar.MINUTE, 35)
        val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
        textView.text = formattedTime
    }

    private fun showDatePickerDialog(textView: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val dateFormatter = SimpleDateFormat("E, MMM dd", Locale.getDefault())
        val datePickerDialog = DatePickerDialog(
            requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                val formattedDate = dateFormatter.format(calendar.time)
                textView.text = formattedDate
            }, year, month, day
        )

        // Set the minimum date to the current date
        val minDate = Calendar.getInstance()

        // Set the maximum date to four months from the current month
        val maxDate = Calendar.getInstance().apply {
            add(Calendar.MONTH, 3)
        }

        datePickerDialog.datePicker.minDate = minDate.timeInMillis
        datePickerDialog.datePicker.maxDate = maxDate.timeInMillis
        datePickerDialog.show()
    }

    private fun checkPermissionAndMakeCall(phoneNumber: String) {
        when {
            ContextCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted
                makePhoneCall(phoneNumber)
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE) -> {
                // Show rationale and request permission
                // You can show a dialog explaining why you need this permission
                DialogUtils.getPermissionDeniedDialog(
                    requireActivity(),
                    0,
                    getString(R.string.allow_call_permission),
                    ::onDialogCallPermissionAllowClick
                )
            }

            else -> {
                // Directly request the permission
                DialogUtils.getPermissionDeniedDialog(
                    requireActivity(),
                    1,
                    getString(R.string.allow_call_permission),
                    ::onDialogCallPermissionAllowClick
                )
            }
        }
    }

    private fun onDialogCallPermissionAllowClick(type: Int) {
        if (type == 0) {
            requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        } else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", requireActivity().packageName, null)
            }
            startActivity(intent)
        }
    }

    private fun makePhoneCall(phoneNumber: String) {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$phoneNumber")
        startActivity(callIntent)
    }

    private val showMessageIndicatorBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            binding.viewStartRide.ivChatIndicator.isVisible = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            requireActivity().unregisterReceiver(showMessageIndicatorBroadcastReceiver)
        } catch (e: Exception) {
        }

    }

    private fun startRideDialog(context: Context, rideAlertUiState: RideVM.RideAlertUiState) {
        with(binding.viewStartRide) {
            clConnecting.visibility = View.VISIBLE
            this@HomeFragment.binding.cvTitle.visibility = View.INVISIBLE
            this@HomeFragment.binding.ivBack.visibility = View.GONE
            with(rideVM.createRideData) {
                tvDriverRating.text =
                    driverDetail?.driverRating.orEmpty().ifEmpty { "0.0" }.formatString(1)
                tvUserName.text = driverDetail?.driverName.orEmpty()
                tvNumber.text = vehicleData?.vehicleNumber.orEmpty()
                tvCarName.text = vehicleData?.name.orEmpty()
                tvNumber.text = vehicleData?.vehicleNumber.orEmpty()
                tvAddress.text = pickUpLocation?.address.orEmpty()
                tvPickUpAddress.text = pickUpLocation?.address.orEmpty()
                tvDestinationAddress.text = dropLocation?.address.orEmpty()
                tvDistanceValue.text = vehicleData?.distance.orEmpty()
                tvTimeValue.text = "${vehicleData?.eta.orEmpty()} min"
                tvDistanceTime.text = "${vehicleData?.eta.orEmpty()}"
                tvDistanceTimeMin.text = "min"
                tvPriceValue.text = "${vehicleData?.fare}"
                Glide.with(requireView()).load(driverDetail?.driverImage.orEmpty())
                    .error(R.mipmap.ic_launcher).into(ivProfileImage)
                Glide.with(requireView()).load(vehicleData?.image.orEmpty())
                    .error(R.mipmap.ic_launcher).into(ivCar)
            }


            if (rideAlertUiState == RideVM.RideAlertUiState.FindDriverDialog) {
                clConnecting.visibility = View.VISIBLE
                clPickUpLocation.visibility = View.GONE
            } else {
                clConnecting.visibility = View.GONE
                clPickUpLocation.visibility = View.VISIBLE
            }
            tvViewDetails.setOnSingleClickListener {
                rideVM.createRideData.deliveryPackages?.let { showPackages(requireActivity(), it) }
            }

            ivChat.setOnSingleClickListener {
                ivChatIndicator.isVisible = false
//                startActivity(
                val intent = Intent(
                    (activity as BaseActivity<*>), ChatActivity::class.java
                ).putExtra("customerId", "${rideVM.createRideData.customerId}")
                    .putExtra("driverId", "${rideVM.createRideData.driverDetail?.driverId}")
                    .putExtra("engagementId", "${rideVM.createRideData.tripId}")
                    .putExtra("driverName", "${rideVM.createRideData.driverDetail?.driverName}")
                    .putExtra(
                        "driverImage", "${rideVM.createRideData.driverDetail?.driverImage}"
                    )
//                )
                activityResultLauncher.launch(intent)
            }
//            tvConnecting.setOnSingleClickListener {
//                clConnecting.visibility = View.GONE
//                clPickUpLocation.visibility = View.VISIBLE
//            }
            ivCall.setOnSingleClickListener {
                val phone = rideVM.createRideData.driverDetail?.driverPhoneNo ?: ""
                if (phone.isEmpty()) showSnackBar("There is some issue in call. Please try after sometime.")
                else checkPermissionAndMakeCall(phone)
            }

            tvSlideCancelBtn.onSlideCompleteListener =
                object : SlideToActView.OnSlideCompleteListener {
                    override fun onSlideComplete(view: SlideToActView) {
                        safeCall {
                            tvSlideCancelBtn.setCompleted(completed = false, withAnimation = true)
                            findNavController().navigate(
                                R.id.navigation_cancel_ride,
                                bundleOf("sessionId" to rideVM.createRideData.sessionId)
                            )
                        }
                    }
                }

            googleMap?.clearMap()
            Log.e("RideStatus", "is --->  ${rideVM.createRideData.status}")
            Log.e("RideStatus", "all data --->  ${Gson().toJson(rideVM.createRideData)}")
            when (rideVM.createRideData.status) {
                TripStatus.REQUESTED.type -> {
                    Log.e("RideStatus", "is --->  Requested ride")
                    rideVM.hideHomeNav(true)
                    binding.tvSOS.isVisible = false
                    binding.viewStartRide.llDistanceTime.isVisible = false
                    Handler(Looper.getMainLooper()).postDelayed({
                        requireContext().showPath(
                            srcLat = LatLng(
                                rideVM.createRideData.pickUpLocation?.latitude?.toDouble() ?: 0.0,
                                rideVM.createRideData.pickUpLocation?.longitude?.toDouble() ?: 0.0
                            ), desLat = LatLng(
                                rideVM.createRideData.dropLocation?.latitude?.toDouble() ?: 0.0,
                                rideVM.createRideData.dropLocation?.longitude?.toDouble() ?: 0.0
                            ), mMap = googleMap, isTracking = false
                        ) {
//                        setPathTimeAndDistance(
//                            durationDistance = it.distanceText.orEmpty(),
//                            durationTime = it.durationText.orEmpty()
//                        )
                        }
                    }, 200)

                }

                TripStatus.ACCEPTED.type -> {
                    binding.tvSOS.isVisible = true
                    binding.viewStartRide.llDistanceTime.isVisible = false
                    Log.i(
                        "DRIVERLOCATION",
                        "on accept     lat::${rideVM.createRideData.driverLocation?.latitude?.toDouble() ?: 0.0} lan::${rideVM.createRideData.driverLocation?.longitude?.toDouble() ?: 0.0}"
                    )
                    requireContext().showPath(
                        srcLat = LatLng(
                            rideVM.createRideData.driverLocation?.latitude?.toDouble() ?: 0.0,
                            rideVM.createRideData.driverLocation?.longitude?.toDouble() ?: 0.0
                        ), desLat = LatLng(
                            rideVM.createRideData.pickUpLocation?.latitude?.toDouble() ?: 0.0,
                            rideVM.createRideData.pickUpLocation?.longitude?.toDouble() ?: 0.0
                        ), mMap = googleMap
                    ) {
//                        setPathTimeAndDistance(
//                            durationDistance = it.distanceText.orEmpty(),
//                            durationTime = it.durationText.orEmpty()
//                        )
                    }
                }

                TripStatus.ARRIVED.type -> {
                    binding.tvSOS.isVisible = true
                    tvAddress.isVisible = true
                    clPickAndDrop.isVisible = false
                    tvAddress.text = context.getString(R.string.txt_driver_arrived)
                    tvAddress.setCompoundDrawables(null, null, null, null)
                    clDistanceTime.visibility = View.GONE
                    this@HomeFragment.binding.tvTitleStatus.text =
                        context.getString(R.string.txt_your_volt_has_arrived)
                    binding.viewStartRide.llDistanceTime.isVisible = true
                    requireContext().showPath(
                        srcLat = LatLng(
                            rideVM.createRideData.driverLocation?.latitude?.toDouble() ?: 0.0,
                            rideVM.createRideData.driverLocation?.longitude?.toDouble() ?: 0.0
                        ), desLat = LatLng(
                            rideVM.createRideData.pickUpLocation?.latitude?.toDouble() ?: 0.0,
                            rideVM.createRideData.pickUpLocation?.longitude?.toDouble() ?: 0.0
                        ), mMap = googleMap
                    ) {
//                        setPathTimeAndDistance(
//                            durationDistance = it.distanceText.orEmpty(),
//                            durationTime = it.durationText.orEmpty()
//                        )
                    }
                }

                TripStatus.STARTED.type -> {
                    binding.tvSOS.isVisible = true
                    tvAddress.text = context.getString(R.string.txt_ride_in_progress)
                    tvAddress.isVisible = false
                    clPickAndDrop.isVisible = true
                    tvAddress.setCompoundDrawables(null, null, null, null)
                    clDistanceTime.visibility = View.GONE
                    this@HomeFragment.binding.tvTitleStatus.text =
                        context.getString(R.string.txt_volt_ride_initiated)
                    binding.viewStartRide.llDistanceTime.isVisible = true
                    requireContext().showPath(
                        srcLat = LatLng(
                            rideVM.createRideData.pickUpLocation?.latitude?.toDouble() ?: 0.0,
                            rideVM.createRideData.pickUpLocation?.longitude?.toDouble() ?: 0.0
                        ), desLat = LatLng(
                            rideVM.createRideData.dropLocation?.latitude?.toDouble() ?: 0.0,
                            rideVM.createRideData.dropLocation?.longitude?.toDouble() ?: 0.0
                        ), mMap = googleMap
                    ) {
//                        setPathTimeAndDistance(
//                            durationDistance = it.distanceText.orEmpty(),
//                            durationTime = it.durationText.orEmpty()
//                        )
                    }
                }

                TripStatus.ENDED.type -> {
                    binding.tvSOS.isVisible = true
                    binding.viewStartRide.llDistanceTime.isVisible = false
                    startRideAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
                    googleMap?.clearMap()
                    findNavController().navigate(R.id.navigation_ride_complete)
                }

                else -> {}
            }
        }
    }

    private lateinit var carTypeAdapter: CarsTypeAdapter
    private fun startCarTypesDialog() {
        with(binding.viewCarType) {
            if ((VenusApp.offerApplied) > 0.0) {
                tvOfferTitle.isVisible = true
                tvOfferTitle.text = VenusApp.offerTitle
                if (showOfferAnimationOnce) {
                    startConfettiAnimation()
                    showOfferAnimationOnce = false
                }
            } else {
                tvOfferTitle.isVisible = false
            }
            tvSelectTypeBtn.setOnSingleClickListener {
                if (rideVM.createRideData.regionId.isNullOrEmpty()) {
                    showSnackBar(
                        getString(R.string.please_choose_vehicle_type), this@with.clRootView
                    )
                    return@setOnSingleClickListener
                }
                isVehicleShowing = false
                lifecycleScope.coroutineContext.cancelChildren()
                carTypeAlertState?.isHideable = true
                carTypeAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
                if (SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID) == 1) {
//                    rideVM.updateUiState(RideVM.RideAlertUiState.ShowCustomerDetailPaymentDialog)
                    findNavController().navigate(R.id.packageReviewDetailsFragment)
                } else {
                    findNavController().navigate(R.id.addPackageFragment)
                }
            }
            carTypeAdapter = CarsTypeAdapter(
                requireActivity(),
                rideVM.regionsList,
                currencyCode = rideVM.createRideData.currencyCode.orEmpty(),
                rideVM.customerETA
            ) {
                rideVM.createRideData.regionId = it?.regionId.orEmpty()
                rideVM.createRideData.vehicleType = it?.vehicleType.orEmpty()
                rideVM.createRideData.vehicleData = CreateRideData.VehicleData(
                    image = it?.images?.rideNowNormal2x.orEmpty(),
                    name = it?.regionName.orEmpty(),
                    totalCapacity = it?.maxPeople.orEmpty(),
                    eta = it?.eta.orEmpty(),
                    fare = it?.region_fare?.fare.toString(),
                    currency = it?.region_fare?.currency,
                    distance = rideVM.customerETA.rideDistance.toString(),
                    discount = it?.region_fare?.discount ?: 0.0,
                    original_fare = it?.region_fare?.original_fare ?: 0.0
                )
            }
            rvCarsType.adapter = carTypeAdapter
        }

    }

    private lateinit var deliveryVehicleTypeAdapter: DeliveryVehicleTypeAdapter
    private fun startDeliveryVehicleTypeDialog() {
        with(binding.viewDeliveryVehicleType) {
            val vehicleList = ArrayList<UserDataDC.Login.VehicleType>()
            SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
                ?.let {
                    it.login?.vehicleTypes?.let { it1 -> vehicleList.addAll(it1) }
                }
            vehicleList.forEach {
                if (rideVM.vehicleType != null) {
                    it.isSelected = it.regionId == rideVM.vehicleType?.regionId
                }
            }
            tvPickUpAddress.text = rideVM.createRideData.pickUpLocation?.address
            tvDropOffAddress.text = rideVM.createRideData.dropLocation?.address
            deliveryVehicleTypeAdapter = DeliveryVehicleTypeAdapter(
                requireActivity(),
                vehicleList,
            ) {
                if (it != null) {
                    rideVM.vehicleType = it
                }
            }

            rvVehicleType.adapter = deliveryVehicleTypeAdapter
            tvConfirmBtn.setOnSingleClickListener {
                if (rideVM.vehicleType != null)
                    findNavController().navigate(R.id.addPackageFragment)
                else
                    showSnackBar("Please Select Vehicle Type", tvConfirmBtn)

            }
        }
    }


    /**
     * Set Path Time and Distance
     * */
    private fun setPathTimeAndDistance(
        durationDistance: String, durationTime: String
    ) {
        binding.viewStartRide.tvDistanceValue.text = durationDistance
        binding.viewStartRide.tvTimeValue.text = durationTime
        durationTime.trim().split(" ").let { distance ->
            binding.viewStartRide.tvDistanceTime.text = distance.first()
            binding.viewStartRide.tvDistanceTimeMin.text = distance.last()
        }
    }


    private fun setBannerAdapter(context: Context) {
        val listData = mutableListOf<BannerData>()
        listData.add(BannerData(ContextCompat.getDrawable(context, R.drawable.ic_banner_image_new)))
        listData.add(BannerData(ContextCompat.getDrawable(context, R.drawable.ic_banner_image_new)))
        listData.add(BannerData(ContextCompat.getDrawable(context, R.drawable.ic_banner_image_new)))
        listData.add(BannerData(ContextCompat.getDrawable(context, R.drawable.ic_banner_image_new)))
//        binding.vpBanners.adapter = BannerAdapter(listData)
    }


    private fun backButtonMapView() {
        binding.ivBack.setOnSingleClickListener {
            carTypeAlertState?.isHideable = true
            locationAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
            carTypeAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
            carDetailAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
            startRideAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
//            deliveryVehicleTypeAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
            when (rideVM.rideAlertUiState.value) {
                RideVM.RideAlertUiState.ShowVehicleTypesDialog -> {
//                    if (SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID) == 1)
//                        rideVM.updateUiState(RideVM.RideAlertUiState.ShowLocationDialog)
//                    else
//                        rideVM.updateUiState(RideVM.RideAlertUiState.ShowDeliveryVehicleTypeDialog)

                    rideVM.updateUiState(RideVM.RideAlertUiState.ShowLocationDialog)
                }

                RideVM.RideAlertUiState.ShowCustomerDetailPaymentDialog -> {
                    rideVM.updateUiState(RideVM.RideAlertUiState.ShowVehicleTypesDialog)
                }

                RideVM.RideAlertUiState.FindDriverDialog -> {
                    rideVM.updateUiState(RideVM.RideAlertUiState.ShowCustomerDetailPaymentDialog)
                }

                RideVM.RideAlertUiState.ShowCustomerDetailDialog -> {
                    rideVM.updateUiState(RideVM.RideAlertUiState.ShowCustomerDetailPaymentDialog)
                }

//                RideVM.RideAlertUiState.ShowDeliveryVehicleTypeDialog -> {
//                    rideVM.updateUiState(RideVM.RideAlertUiState.ShowLocationDialog)
//                }

                else -> Unit
            }
        }
    }


    /**
     * Observe UI State
     * */
    private fun observeUiState() = rideVM.rideAlertUiState.observe(viewLifecycleOwner) {
        uiStateHandler(it)
    }

    // Register back press callback
    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (rideVM.rideAlertUiState.value == RideVM.RideAlertUiState.FindDriverDialog) {
                AlertDialog.Builder(requireActivity()).apply {
                    setTitle("Cancel Ride")
                    setMessage("Your ride request will get cancelled")
                    setPositiveButton("Okay") { _, _ ->
                        findNavController().navigate(
                            R.id.navigation_cancel_ride,

                            bundleOf("sessionId" to rideVM.createRideData.sessionId)
                        )
                    }
                    setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    create()
                    show()
                }
            } else {
//                hideAllBottomSheets()
                if (rideVM.rideAlertUiState.value == RideVM.RideAlertUiState.HomeScreen || rideVM.rideAlertUiState.value == RideVM.RideAlertUiState.ShowCustomerDetailDialog)
                    requireActivity().finish()
                else
                    binding.ivBack.performClick()
            }
        }
    }

    private fun setNearByDriverMarkersOnMainMap(googleMap: GoogleMap) {
        nearByDriverLatLanArrayList.forEach { it ->
            val marker = googleMap.addMarker(MarkerOptions().position(it.latLng).apply {
                icon(context?.vectorToBitmap(R.drawable.car_icon))
                anchor(0.5f, 1f)
            })
            marker?.rotation = it.bearing
        }
    }

    private fun uiStateHandler(state: RideVM.RideAlertUiState) {
        try {
            when (state) {
                RideVM.RideAlertUiState.HomeScreen -> {
//                    rideVM.hideHomeNav(false)
//                    Intent(requireActivity(), Home::class.java).apply {
//                        startActivity(this)
//                        requireActivity().finishAffinity()
//                    }
                }

                RideVM.RideAlertUiState.ShowLocationDialog -> {
                    rideVM.hideHomeNav(true)
                    Log.i("SavedStateData", "in ShowLocationDialog")
                    if (rideVM.createRideData.pickUpLocation?.address.isNullOrEmpty())
                        lifecycleScope.launch {
                            getLocationDataFromLatLng(VenusApp.latLng)
                        }
                    startWhereDialog()
                    binding.clWhereMain.visibility = View.VISIBLE
                    binding.clMapMain.visibility = View.GONE
                    locationAlertState?.state = BottomSheetBehavior.STATE_EXPANDED
                }

                RideVM.RideAlertUiState.ShowVehicleTypesDialog -> {
                    rideVM.hideHomeNav(true)
                    binding.clWhereMain.visibility = View.GONE
                    binding.clMapMain.visibility = View.VISIBLE
                    startCarTypesDialog()
                    carTypeAlertState?.isHideable = false
                    carTypeAlertState?.state = BottomSheetBehavior.STATE_EXPANDED
                    try {
                        findNavController().currentBackStackEntry?.savedStateHandle?.remove<CreateRideData.LocationData>(
                            "pickUpLocation"
                        )
                        findNavController().currentBackStackEntry?.savedStateHandle?.remove<CreateRideData.LocationData>(
                            "dropLocation"
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    binding.viewStartRide.llDistanceTime.isVisible = false
                    requireContext().showPath(
                        srcLat = LatLng(
                            rideVM.createRideData.pickUpLocation?.latitude?.toDouble() ?: 0.0,
                            rideVM.createRideData.pickUpLocation?.longitude?.toDouble() ?: 0.0
                        ), desLat = LatLng(
                            rideVM.createRideData.dropLocation?.latitude?.toDouble() ?: 0.0,
                            rideVM.createRideData.dropLocation?.longitude?.toDouble() ?: 0.0
                        ), mMap = googleMap, isTracking = false
                    ) {
//                        setPathTimeAndDistance(
//                            durationDistance = it.distanceText.orEmpty(),
//                            durationTime = it.durationText.orEmpty()
//                        )
                        googleMap?.let { setNearByDriverMarkersOnMainMap(it) }
                    }

                }

                RideVM.RideAlertUiState.ShowCustomerDetailPaymentDialog -> {
                    rideVM.hideHomeNav(true)
                    binding.clWhereMain.visibility = View.GONE
                    binding.clMapMain.visibility = View.VISIBLE
                    carDetailDialog(requireContext())
                    carDetailAlertState?.state = BottomSheetBehavior.STATE_EXPANDED
                }

                RideVM.RideAlertUiState.FindDriverDialog -> {
                    rideVM.hideHomeNav(true)
                    binding.clWhereMain.visibility = View.GONE
                    binding.clMapMain.visibility = View.VISIBLE
                    startRideDialog(requireContext(), RideVM.RideAlertUiState.FindDriverDialog)
                    startRideAlertState?.state = BottomSheetBehavior.STATE_EXPANDED
                }

                RideVM.RideAlertUiState.ShowCustomerDetailDialog -> {
                    rideVM.hideHomeNav(true)
                    binding.clWhereMain.visibility = View.GONE
                    binding.clMapMain.visibility = View.VISIBLE
                    rideStatus = AppConstants.DRIVER_ARRIVED
                    startRideDialog(
                        requireContext(), RideVM.RideAlertUiState.ShowCustomerDetailDialog
                    )
                    startRideAlertState?.state = BottomSheetBehavior.STATE_EXPANDED
                }

//                RideVM.RideAlertUiState.ShowDeliveryVehicleTypeDialog -> {
//                    rideVM.hideHomeNav(true)
//                    binding.clWhereMain.visibility = View.GONE
//                    binding.clMapMain.visibility = View.VISIBLE
//                    try {
//                        findNavController().currentBackStackEntry?.savedStateHandle?.remove<CreateRideData.LocationData>(
//                            "pickUpLocation"
//                        )
//                        findNavController().currentBackStackEntry?.savedStateHandle?.remove<CreateRideData.LocationData>(
//                            "dropLocation"
//                        )
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                    startDeliveryVehicleTypeDialog()
////                    deliveryVehicleTypeAlertState?.state = BottomSheetBehavior.STATE_EXPANDED
//                    binding.viewStartRide.llDistanceTime.isVisible = false
//                    requireContext().showPath(
//                        srcLat = LatLng(
//                            rideVM.createRideData.pickUpLocation?.latitude?.toDouble() ?: 0.0,
//                            rideVM.createRideData.pickUpLocation?.longitude?.toDouble() ?: 0.0
//                        ), desLat = LatLng(
//                            rideVM.createRideData.dropLocation?.latitude?.toDouble() ?: 0.0,
//                            rideVM.createRideData.dropLocation?.longitude?.toDouble() ?: 0.0
//                        ), mMap = googleMap, isTracking = false
//                    ) {
////                        setPathTimeAndDistance(
////                            durationDistance = it.distanceText.orEmpty(),
////                            durationTime = it.durationText.orEmpty()
////                        )
//                        googleMap?.let { setNearByDriverMarkersOnMainMap(it) }
//                    }
//                }

                else -> {}

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun observeFetchOngoingTrip() =
        rideVM.fetchOngoingTripData.observeData(lifecycle = viewLifecycleOwner, onLoading = {
//            showProgressDialog()
            if (activity != null) binding.llInProgessRide.isVisible = true
        }, onError = {
//            hideProgressDialog()
            if (activity != null) binding.llInProgessRide.isVisible = false
            showSnackBar(this)
        }, onSuccess = {
//            hideProgressDialog()
            if (activity != null) binding.llInProgessRide.isVisible = false
            if (this?.trips?.isNotEmpty() == true) {
                rideVM.createRideData.deliveryPackages = this.deliveryPackages.orEmpty()
                this.trips.firstOrNull()?.let { trip ->
                    with(rideVM.createRideData) {
                        currencyCode = trip.currency.orEmpty()
                        tripId = trip.tripId.orEmpty()
                        customerId = trip.customerId.orEmpty()
                        sessionId = trip.sessionId.orEmpty()
                        status = trip.status
                        vehicleData = vehicleData?.copy(
                            image = trip.image.orEmpty(),
                            name = trip.modelName.orEmpty(),
                            totalCapacity = null,
                            eta = trip.dryEta.orEmpty(),
                            distance = trip.estimatedDistance.orEmpty(),
                            fare = trip.estimatedDriverFare ?: "0.0",
                            currency = trip.currency,
                            vehicleNumber = trip.licensePlate.orEmpty()
                        )
                        driverDetail = driverDetail?.copy(
                            driverImage = trip.driverImage.orEmpty(),
                            driverName = trip.driverName.orEmpty(),
                            driverId = trip.driverId.orEmpty(),
                            driverRating = trip.driverRating.orEmpty(),
                            driverPhoneNo = trip.driverPhoneNo.orEmpty()
                        )
                        pickUpLocation = pickUpLocation?.copy(
                            latitude = trip.latitude.orEmpty(),
                            longitude = trip.longitude.orEmpty(),
                            address = trip.pickupAddress.orEmpty()
                        )
                        dropLocation = dropLocation?.copy(
                            latitude = trip.dropLatitude.orEmpty(),
                            longitude = trip.dropLongitude.orEmpty(),
                            address = trip.dropAddress.orEmpty()
                        )
                        driverLocation = driverLocation?.copy(
                            latitude = trip.driverCurrentLatitude,
                            longitude = trip.driverCurrentLongitude
                        )
                    }
                    if (rideVM.createRideData.status == 0) {
                        rideVM.updateUiState(RideVM.RideAlertUiState.FindDriverDialog)
                    } else {
                        SocketSetup.startRideEmit(rideVM.createRideData.tripId.orEmpty())
                        rideVM.updateUiState(RideVM.RideAlertUiState.ShowCustomerDetailDialog)
                    }
                    if (Home.isFromMsgNotification) {
//                        startActivity(
                        val intent = Intent(
                            (activity as BaseActivity<*>), ChatActivity::class.java
                        ).apply {
                            flags =
                                Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra("customerId", "${rideVM.createRideData.customerId}")
                            putExtra(
                                "driverId", "${rideVM.createRideData.driverDetail?.driverId}"
                            )
                            putExtra("engagementId", "${rideVM.createRideData.tripId}")
                            putExtra(
                                "driverName", "${rideVM.createRideData.driverDetail?.driverName}"
                            )
                            putExtra(
                                "driverImage", "${rideVM.createRideData.driverDetail?.driverImage}"
                            )
                        }
//                        )
                        activityResultLauncher.launch(intent)

                    }
                }
            } else {
                Home.isFromMsgNotification = false
//                var alreadyHittingApi = false
//                rideVM.updateUiState(RideVM.RideAlertUiState.HomeScreen)
//                SingleFusedLocation.initialize(requireContext(), object : LocationResultHandler {
//                    override fun updatedLocation(location: Location) {
//                        if (!alreadyHittingApi) {
//                            alreadyHittingApi = true
//                            nearByDriverMap?.moveCamera(
//                                CameraUpdateFactory.newLatLngZoom(
//                                    LatLng(
//                                        location.latitude,
//                                        location.longitude
//                                    ), 12f
//                                )
//                            )
//                            rideVM.findNearDriver(location.latitude, location.longitude)
//                        }
//                    }
//                })
            }
        })


    /**
     * Observe Near Driver
     * */
    private fun observeNearDriver() =
        rideVM.findNearDriverData.observeData(lifecycle = viewLifecycleOwner, onLoading = {
//        showProgressDialog()
            if (activity != null) binding.progressMap.shimmerLayout.isVisible = true

        }, onSuccess = {
//        hideProgressDialog()
            if (activity != null) binding.progressMap.shimmerLayout.isVisible = false
            try {
                nearByDriverMap?.clear()
                nearByDriverLatLanArrayList.clear()
//                val latLongB = LatLngBounds.Builder()
                this?.drivers?.forEach {
                    nearByDriverLatLanArrayList.add(
                        NearByDriverMarkers(
                            LatLng(
                                it.latitude ?: 0.0, it.longitude ?: 0.0
                            ), it.bearing?.toFloatOrNull() ?: 0F
                        )
                    )
                    val marker = nearByDriverMap?.addMarker(MarkerOptions().position(
                        LatLng(
                            it.latitude ?: 0.0, it.longitude ?: 0.0
                        )
                    ).apply {
                        icon(context?.vectorToBitmap(R.drawable.car_icon))
                        anchor(0.5f, 1f)
                    })
                    marker?.rotation = it.bearing?.toFloatOrNull() ?: 0F
//                    latLongB.include(LatLng(it.latitude ?: 0.0, it.longitude ?: 0.0))
                }
//                val bounds = latLongB.build()
//                nearByDriverMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                nearByDriverMap?.addMarker(MarkerOptions().position(VenusApp.latLng).apply {
                    icon(context?.vectorToBitmap(R.drawable.new_location_placeholder))
                    anchor(0.5f, 1f)
                })
                nearByDriverMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        VenusApp.latLng, 12f
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, onError = {
//        hideProgressDialog()
            if (activity != null) binding.progressMap.shimmerLayout.isVisible = false
        })

    private fun observeAddedAddress() =
        rideVM.fetchAddedAddresses.observeData(lifecycle = viewLifecycleOwner, onLoading = {
//        showProgressDialog()

        }, onSuccess = {
//        hideProgressDialog()
            Log.i("ADDRESSSS", Gson().toJson(this))
            this?.saved_addresses?.let { addressAdapter.submitList(it) }
            binding.viewLocation.rvAddress.isVisible = addressAdapter.itemCount > 0
        }, onError = {
//        hideProgressDialog()
        })

    private fun addressAdapterClick(savedAddress: SavedAddresse, isDelete: Boolean = false) {
        if (isDelete) {
            searchLocationVM.addAddress(JSONObject().apply {
                put("address_id", savedAddress.id)
                put("delete_flag", 1)
            }.getJsonRequestBody())
        } else {
            rideVM.createRideData.pickUpLocation = CreateRideData.LocationData(
                address = savedAddress.addr,
                latitude = savedAddress.lat.toString(),
                longitude = savedAddress.lng.toString(),
                placeId = savedAddress.google_place_id
            )
            rideVM.createRideData.pickUpLocation?.address?.let {
                binding.viewLocation.tvPickUpValue.text = it
            }
        }
    }

    private fun observeFindDriver() =
        rideVM.findDriverData.observeData(lifecycle = viewLifecycleOwner, onLoading = {
            showProgressDialog()
        }, onError = {
            hideProgressDialog()
            showToastShort(this)
        }, onSuccess = {
            hideProgressDialog()
            rideVM.createRideData.currencyCode = this?.currency.orEmpty()
            rideVM.regionsList.clear()
            rideVM.fareStructureList.clear()
            rideVM.customerETA = this?.customerETA ?: FindDriverDC.CustomerETA()
            rideVM.fareStructureList.addAll(this?.fareStructure ?: emptyList())
            rideVM.regionsList.addAll(this?.regions ?: emptyList())
//            if (SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID) == 1)
            rideVM.updateUiState(RideVM.RideAlertUiState.ShowVehicleTypesDialog)
//            else
//                rideVM.updateUiState(RideVM.RideAlertUiState.ShowDeliveryVehicleTypeDialog)
//            rideVM.getDistanceFromGoogle(isLoading = {
//                showProgressDialog()
//            }, onSuccess = { distance, time ->
//                hideProgressDialog()
//                this?.regions?.map {
//                    it.rideTotalDistance = distance
//                    it.rideTotalTime = time
//                    it.vehicleAmount = it.calculateFearStructure(rideVM.fareStructureList)
//                }
//                rideVM.regionsList.addAll(this?.regions ?: emptyList())
//                rideVM.updateUiState(RideVM.RideAlertUiState.ShowVehicleTypesDialog)
//            }, onError = {
//                hideProgressDialog()
//            })
        })


    private fun observeFindDriverInLoop() =
        rideVM.findDriverDataInLoop.observeData(lifecycle = viewLifecycleOwner, onLoading = {
//            showProgressDialog()
        }, onError = {
//            hideProgressDialog()
//            showToastShort(this)
        }, onSuccess = {
//            hideProgressDialog()
            if (isVehicleShowing) {
                rideVM.createRideData.currencyCode = this?.currency.orEmpty()
                rideVM.regionsList.clear()
                rideVM.fareStructureList.clear()
                rideVM.customerETA = this?.customerETA ?: FindDriverDC.CustomerETA()
                rideVM.fareStructureList.addAll(this?.fareStructure ?: emptyList())
                rideVM.regionsList.addAll(this?.regions ?: emptyList())
                rideVM.regionsList.forEach { region ->
                    if (region.regionId == rideVM.createRideData.regionId) {
                        // Check if eta is null, if yes, clear selectedRegionId
                        if (region.eta == null) {
                            rideVM.createRideData.regionId = ""
                        }
                        // Set isSelected based on regionId match and eta not being null
                        region.isSelected = region.eta != null
                    } else {
                        // For other regions, ensure isSelected is false
                        region.isSelected = false
                    }
                }
                carTypeAdapter.changeCustomerETA(rideVM.customerETA)
                carTypeAdapter.notifyDataSetChanged()
            }
        })


    private fun observeRequestRide() =
        rideVM.requestRideData.observeData(lifecycle = viewLifecycleOwner, onLoading = {
            showProgressDialog()
        }, onError = {
            hideProgressDialog()
            showToastShort(this)
            rideVM.cardId = ""
            rideVM.last4 = ""
            rideVM.couponToApply = 0
            rideVM.promoCode = ""
            binding.clWhereMain.visibility = View.VISIBLE
            binding.clMapMain.visibility = View.GONE
            rideVM.hideHomeNav(false)
            rideVM.updateUiState(RideVM.RideAlertUiState.HomeScreen)
        }, onSuccess = {
            hideProgressDialog()
            rideVM.createRideData.sessionId = this?.sessionId.orEmpty()
            rideVM.createRideData.status = 0
            rideVM.cardId = ""
            rideVM.last4 = ""
            rideVM.couponToApply = 0
            rideVM.promoCode = ""
            rideVM.updateUiState(RideVM.RideAlertUiState.FindDriverDialog)
            SharedPreferencesManager.clearKeyData(SharedPreferencesManager.Keys.ADD_PACKAGE)
        })

    private fun observeRequestSchedule() =
        rideVM.scheduleRideData.observeData(lifecycle = viewLifecycleOwner, onLoading = {
            showProgressDialog()
        }, onError = {
            hideProgressDialog()
            showToastShort(this)
            rideVM.createRideData.pickUpLocation = null
            rideVM.createRideData.dropLocation = null
            binding.clWhereMain.visibility = View.VISIBLE
            binding.clMapMain.visibility = View.GONE
            rideVM.cardId = ""
            rideVM.last4 = ""
            rideVM.couponToApply = 0
            rideVM.promoCode = ""
            hideAllBottomSheets()
            rideVM.updateUiState(RideVM.RideAlertUiState.HomeScreen)
            rideVM.hideHomeNav(false)
        }, onSuccess = {
            hideProgressDialog()
            rideVM.hideHomeNav(false)
            showSnackBar("Your ride has been scheduled successfully!!")
            rideVM.createRideData.pickUpLocation = null
            rideVM.createRideData.dropLocation = null
            binding.clWhereMain.visibility = View.VISIBLE
            binding.clMapMain.visibility = View.GONE
            rideVM.cardId = ""
            rideVM.last4 = ""
            rideVM.couponToApply = 0
            rideVM.promoCode = ""
            hideAllBottomSheets()
            rideVM.updateUiState(RideVM.RideAlertUiState.HomeScreen)
            SharedPreferencesManager.clearKeyData(SharedPreferencesManager.Keys.ADD_PACKAGE)
        })


    private fun observeFareEstimate() =
        rideVM.fareEstimateData.observeData(lifecycle = viewLifecycleOwner, onLoading = {
            showProgressDialog()
        }, onError = {
            hideProgressDialog()
            showToastShort(this)
        }, onSuccess = {
            hideProgressDialog()
        })

    private fun observePromoCode() =
        rideVM.enterPromoCode.observeData(lifecycle = viewLifecycleOwner, onLoading = {
            showProgressDialog()
        }, onError = {
            hideProgressDialog()
            showSnackBar(this, binding.viewCarDetail.tvConfirmBtn)
        }, onSuccess = {
            hideProgressDialog()
            startConfettiAnimation()
            binding.viewCarDetail.tvPromoCode.text = "Applied: ${this?.promo_code}"
            binding.viewCarDetail.tvRemove.isVisible = true
            rideVM.couponToApply = this?.codeId ?: 0
            showSnackBar(this?.codeMessage ?: "", binding.viewCarDetail.tvConfirmBtn)
        })


    data class BannerData(var drawable: Drawable?)

    data class PickDropData(
        var drawable: Drawable?, var title: String = "", var address: String = ""
    )

    data class CarTypeData(
        var drawable: Drawable?,
        var title: String = "",
        var time: String = "",
        var personCount: String = "",
        var totalPrice: String = "",
        var desc: String = "",
        var actualPrice: String = "",
    )

    override fun acceptRide() {
        try {
            Log.i("fetchOngoingTrip", "acceptRide")
            rideVM.fetchOngoingTrip()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun rideStarted() {
        Log.i("fetchOngoingTrip", "rideStarted")
        rideVM.fetchOngoingTrip()
    }

    override fun callFetchRideApi() {
        Log.i("fetchOngoingTrip", "callFetchRideApi")
        rideVM.fetchOngoingTrip()
    }

    override fun rideRejectedByDriver() {
        super.rideRejectedByDriver()
        requireActivity().runOnUiThread {
            Log.i("PUSHNOTI", "IN RIDE REJCT HOME")
            binding.clWhereMain.visibility = View.VISIBLE
            binding.clMapMain.visibility = View.GONE
            hideAllBottomSheets()
            rideVM.hideHomeNav(false)
            rideVM.updateUiState(RideVM.RideAlertUiState.HomeScreen)
        }
        rideVM.fetchOngoingTrip()
    }

    override fun rideEnd(
        tripId: String, driverId: String, driverName: String, engagementId: String
    ) {
        requireActivity().runOnUiThread {
            try {
                Log.i("PUSHNOTI", "IN TRY:: ON HOME IN RIDE END")
                binding.clWhereMain.visibility = View.VISIBLE
                binding.clMapMain.visibility = View.GONE
                hideAllBottomSheets()
                rideVM.hideHomeNav(false)
                rideVM.updateUiState(RideVM.RideAlertUiState.HomeScreen)
                findNavController().navigate(
                    R.id.navigation_rate_driver, bundleOf(
                        "engagementId" to tripId,
                        "driverName" to driverName,
                        "driverId" to driverId,
                        "tripId" to tripId
                    )
                )

            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("PUSHNOTI", "IN CATCH:: ON HOME IN RIDE END ${e.message}")
            }
        }
        rideVM.fetchOngoingTrip()
    }

    override fun requestTimeout(msg: String) {
        requireActivity().runOnUiThread {
            binding.clWhereMain.visibility = View.VISIBLE
            binding.clMapMain.visibility = View.GONE
            hideAllBottomSheets()
            rideVM.hideHomeNav(false)
            rideVM.updateUiState(RideVM.RideAlertUiState.HomeScreen)
            showSnackBar(msg)
        }
        rideVM.fetchOngoingTrip()
    }


    override fun driverLocation(latLng: LatLng, bearing: Float, eta: Int) {
        super.driverLocation(latLng, bearing, eta)
        requireActivity().runOnUiThread {
            binding.viewStartRide.tvDistanceTime.text = eta.toString()
            binding.viewStartRide.tvTimeValue.text = eta.toString() + "min"
            requireContext().animateDriver(
                driverLatitude = latLng.latitude,
                driverLongitude = latLng.longitude,
                bearing = bearing.toDouble(),
                googleMap = googleMap
            ) { distance, duration ->
//                setPathTimeAndDistance(
//                    durationDistance = distance,
//                    durationTime = duration
//                )
            }
        }
    }


    private suspend fun getLocationDataFromLatLng(latLng: LatLng, forTop: Boolean = false) {
        withContext(Dispatchers.IO) {
            try {
                val apiKey = VenusApp.googleMapKey
                val url =
                    "https://maps.googleapis.com/maps/api/geocode/json?latlng=${latLng.latitude},${latLng.longitude}&key=$apiKey"
                val result = URL(url).readText()
                val jsonObject = JSONObject(result)
                val status = jsonObject.getString("status")
                if (status == "OK") {
                    val results = jsonObject.getJSONArray("results")
                    if (results.length() > 0) {
                        val address = results.getJSONObject(0).getString("formatted_address")
                        val placeId = results.getJSONObject(0).getString("place_id")
                        if (!forTop)
                        rideVM.createRideData.pickUpLocation = CreateRideData.LocationData(
                            address = address,
                            latitude = latLng.latitude.toString(),
                            longitude = latLng.longitude.toString(),
                            placeId = placeId
                        )
                        // Optionally update the UI with the new location data
//                    // Ensure to switch back to the main thread for UI updates
                        withContext(Dispatchers.Main) {
                            if (forTop)
                                binding.tvPickUpAddress.text = address
                            rideVM.createRideData.pickUpLocation?.address?.let {
                                if (!forTop)
                                    binding.viewLocation.tvPickUpValue.text = it
                            }
                        }
                    } else {
                        null
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.i(
                    "CurrentLocation", "In getLocationDataFromLatLng catch::: ${e.localizedMessage}"
                )
                e.printStackTrace()
                null
            }
        }
    }


    private fun startConfettiAnimation() {
        binding.konfettiView.start(
            Party(
                speed = 10f,
                maxSpeed = 20f,
                damping = 0.95f,
                spread = 360,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x70e8f8, 0xfffff),
                shapes = listOf(Shape.Circle, Shape.Square),
                size = listOf(
                    nl.dionsegijn.konfetti.core.models.Size.SMALL,
                    nl.dionsegijn.konfetti.core.models.Size.LARGE
                ),
                position = Position.Relative(0.5, 0.3),
                emitter = Emitter(duration = 500, TimeUnit.MILLISECONDS).max(500)
            )
        )
    }


    inner class BannerAdapter(private val bannerList: ArrayList<UserDataDC.Login.Banners>) :
        RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

        inner class BannerViewHolder(private val binding: ItemBannersBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(banners: UserDataDC.Login.Banners) {
                Glide.with(requireActivity()).load(banners.bannerImage ?: "")
                    .error(R.drawable.ic_banner_image_new).into(binding.ivBannerImage)

                binding.ivBannerImage.setOnClickListener {
                    if (!banners.actionUrl.isNullOrEmpty())
                        safeCall {
                            CustomTabsIntent.Builder().build()
                                .launchUrl(requireContext(), Uri.parse(banners.actionUrl))
                        }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
            return BannerViewHolder(ItemBannersBinding.inflate(layoutInflater, parent, false))
        }

        override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
            holder.bind(bannerList[position])
        }

        override fun getItemCount(): Int = bannerList.size
    }




    inner class RideTypeAdapter : RecyclerView.Adapter<RideTypeAdapter.RideTypeViewHolder>() {

        inner class RideTypeViewHolder(private val binding: ItemSuggestionsBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(rideType: RideTypes) {
                Glide.with(requireActivity()).load(rideType.drawable ?: "")
                    .error(R.drawable.ic_banner_image_new).into(binding.ivSuggestions)
//                binding.tvRide.text = rideType.name
//                if (rideType.type == 1 || rideType.type == 2) {
//                    binding.rlRide.alpha = 1f
//                    binding.tvRide.alpha = 1f
//                    binding.tvComingSoon.isVisible = false
//                } else {
//                    binding.rlRide.alpha = .5f
//                    binding.tvRide.alpha = .5f
//                    binding.tvComingSoon.isVisible = true
//                }
//                binding.rlRide.setOnClickListener {
//                    if (rideType.type == 2) {
//                        rideVM.createRideData = CreateRideData()
//                        hideAllBottomSheets()
//                        startTimeDialog(requireContext())
//                    } else if (rideType.type == 1) {
//                        rideVM.createRideData = CreateRideData()
//                        schedule = false
//                        rideVM.updateUiState(RideVM.RideAlertUiState.ShowLocationDialog)
//                    }
//                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideTypeViewHolder {
            return RideTypeViewHolder(ItemSuggestionsBinding.inflate(layoutInflater, parent, false))
        }

        override fun onBindViewHolder(holder: RideTypeViewHolder, position: Int) {
            holder.bind(rideTypeArrayList[position])
        }

        override fun getItemCount(): Int = rideTypeArrayList.size
    }

    private fun showPackages(context: Context, packageList: List<CreateRideData.Package>) {
        val dialog = BottomSheetDialog(context, R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = DialogShowPackagesBinding.inflate(LayoutInflater.from(context), null, false)
        dialog.setContentView(binding.root)
        binding.ivDismiss.setOnClickListener { dialog.dismiss() }
        setAdapter(binding, packageList)
        dialog.setCancelable(true)
        dialog.show()
    }


    private lateinit var packagesAdapter: GenericAdapter<CreateRideData.Package>
    private fun setAdapter(
        binding: DialogShowPackagesBinding,
        packageList: List<CreateRideData.Package>
    ) {
        packagesAdapter =
            object : GenericAdapter<CreateRideData.Package>(R.layout.item_package_list) {
                override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                    val binding = ItemPackageListBinding.bind(holder.itemView)
                    val data = getItem(position)
                    binding.tvPackageSize.text = data.package_size
                    binding.tvPackageType.text = data.package_type
                    binding.tvPackageQuantity.text = data.package_quantity.toString()
                    binding.llCustomerImages.isVisible = true

                    binding.ivEdit.isVisible = false
                    binding.ivDelete.isVisible = false
                    when (data.delivery_status) {
                        5 -> {
                            binding.rlStatus.isVisible = true
                            binding.statusViewLine.isVisible = true
                            binding.llPickupImages.isVisible = false
                            binding.llDropImages.isVisible = false
                        }

                        3 -> {
                            binding.rlStatus.isVisible = true
                            binding.statusViewLine.isVisible = true
                            binding.tvStatus.text = "Not Delivered"
                            binding.llDropImages.isVisible = false
                        }
                    }
                    val adapterCustomer =
                        object : GenericAdapter<String>(R.layout.item_package_images) {
                            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                                val bindingM = ItemPackageImagesBinding.bind(holder.itemView)
                                Glide.with(requireActivity())
                                    .load(getItem(position).toString())
                                    .into(bindingM.ivUploadedImage)
                                bindingM.root.setOnClickListener {
                                    fullImagesDialog(getItem(position))
                                }
                            }
                        }

                    val adapterPick =
                        object : GenericAdapter<String>(R.layout.item_package_images) {
                            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                                val bindingM = ItemPackageImagesBinding.bind(holder.itemView)
                                Glide.with(requireActivity()).load(getItem(position).toString())
                                    .into(bindingM.ivUploadedImage)
                                bindingM.root.setOnClickListener {
                                    fullImagesDialog(getItem(position))
                                }
                            }
                        }
                    val adapterDrop =
                        object : GenericAdapter<String>(R.layout.item_package_images) {
                            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                                val bindingM = ItemPackageImagesBinding.bind(holder.itemView)
                                Glide.with(requireActivity()).load(getItem(position).toString())
                                    .into(bindingM.ivUploadedImage)
                                bindingM.root.setOnClickListener {
                                    fullImagesDialog(getItem(position))
                                }
                            }
                        }
                    adapterCustomer.submitList(data.package_images_by_customer)
                    binding.rvCustomerImages.adapter = adapterCustomer

                    adapterPick.submitList(data.package_image_while_pickup)
                    binding.rvPickupImages.adapter = adapterPick

                    adapterDrop.submitList(data.package_image_while_drop_off)
                    binding.rvDropImages.adapter = adapterDrop
                    binding.llPickupImages.isVisible = adapterPick.itemCount > 0
                    binding.llDropImages.isVisible = adapterDrop.itemCount > 0
                }
            }
        binding.rvPackages.adapter = packagesAdapter
        packagesAdapter.submitList(packageList)
    }

    fun fullImagesDialog(
        string: String
    ): Dialog {
        val dialogView = Dialog(requireActivity())
        with(dialogView) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(R.layout.dialog_full_image)
            setCancelable(false)
            val rlDismiss = findViewById<RelativeLayout>(R.id.rlDismiss)
            rlDismiss.setOnClickListener { dismiss() }
            val ivPackageImage = findViewById<ImageView>(R.id.ivPackageImage)
            Glide.with(requireActivity()).load(string)
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

    private fun observeProfileData() = homeViewModel.loginViaToken.observeData(this, onLoading = {
//        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        SharedPreferencesManager.putModel(SharedPreferencesManager.Keys.USER_DATA, this)
        if (activity != null) {
            binding.shimmerBanners.shimmerLayout.isVisible = false
            bannerArrayList.clear()
            promoBannerArrayList.clear()
            this?.login?.homeBanners?.forEach {
                if (it.bannerTypeId == 1)
                    promoBannerArrayList.add(it)
                else
                    bannerArrayList.add(it)
            }
            handler.removeCallbacks(slideRunnable)
            promoHandler.removeCallbacks(slideRunnablePromotions)
            if (bannerArrayList.isNotEmpty()) {
                bannerAdapter.notifyDataSetChanged()
                // Start auto-sliding
                handler.post(slideRunnable)
                binding.bannerViewPager.isVisible = true
                binding.tabLayoutDots.isVisible = true
                binding.tabLayoutDots.isVisible = bannerAdapter.itemCount > 1
            } else {
                binding.bannerViewPager.isVisible = false
                binding.tabLayoutDots.isVisible = false

            }

            if (promoBannerArrayList.isNotEmpty()) {
                promotionalBannerAdapter.notifyDataSetChanged()
                // Start auto-sliding
                promoHandler.post(slideRunnablePromotions)
                binding.promotionBannerViewPager.isVisible = true
                binding.tabLayoutDotsPromo.isVisible = promotionalBannerAdapter.itemCount > 1
            } else {
                binding.promotionBannerViewPager.isVisible = false
                binding.tabLayoutDotsPromo.isVisible = false
            }
        }
    }, onError = {
        hideProgressDialog()
        showToastShort(this)
    })


}