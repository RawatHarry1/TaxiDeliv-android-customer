package com.venus_customer.view.activity.walk_though.ui.home

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ncorti.slidetoact.SlideToActView
import com.venus_customer.R
import com.venus_customer.customClasses.LocationResultHandler
import com.venus_customer.customClasses.SingleFusedLocation
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.customClasses.trackingData.animateDriver
import com.venus_customer.customClasses.trackingData.clearMap
import com.venus_customer.customClasses.trackingData.showPath
import com.venus_customer.customClasses.trackingData.vectorToBitmap
import com.venus_customer.databinding.DialogDateTimeBinding
import com.venus_customer.databinding.FragmentHomeBinding
import com.venus_customer.firebaseSetup.NotificationInterface
import com.venus_customer.model.api.observeData
import com.venus_customer.model.dataClass.userData.UserDataDC
import com.venus_customer.socketSetup.SocketInterface
import com.venus_customer.socketSetup.SocketSetup
import com.venus_customer.util.SharedPreferencesManager
import com.venus_customer.util.TripStatus
import com.venus_customer.util.constants.AppConstants
import com.venus_customer.util.formatString
import com.venus_customer.util.getBottomSheetBehaviour
import com.venus_customer.util.safeCall
import com.venus_customer.util.showSnackBar
import com.venus_customer.view.activity.chat.ChatActivity
import com.venus_customer.view.base.BaseActivity
import com.venus_customer.view.base.BaseFragment
import com.venus_customer.viewmodel.rideVM.CreateRideData
import com.venus_customer.viewmodel.rideVM.RideVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(), NotificationInterface, SocketInterface {

    lateinit var binding: FragmentHomeBinding
    private val rideVM by activityViewModels<RideVM>()
    private var locationAlertState: BottomSheetBehavior<View>? = null
    private var carTypeAlertState: BottomSheetBehavior<View>? = null
    private var carDetailAlertState: BottomSheetBehavior<View>? = null
    private var startRideAlertState: BottomSheetBehavior<View>? = null
    private var googleMap: GoogleMap? = null
    private var nearByDriverMap: GoogleMap? = null

    companion object {
        var notificationInterface: NotificationInterface? = null
    }

    var rideStatus = AppConstants.DRIVER_PICKUP
    var schedule = true

    override fun initialiseFragmentBaseViewModel() {
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_home
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("HOMEFRAGMENT", "onViewCreated")
        SocketSetup.initializeInterface(this)
        SocketSetup.connectSocket()
        binding = getViewDataBinding()
        rideVM.createRideData = CreateRideData()
        setBannerAdapter(view.context)
        setMapFragment(view, savedInstanceState)
        getSavedStateData()
        observeUiState()
        observeFindDriver()
        observeRequestRide()
        observeFareEstimate()
        observeFetchOngoingTrip()
        observeNearDriver()
        clickHandler()
        backButtonMapView()
    }


    override fun onResume() {
        super.onResume()
        notificationInterface = this
        Log.i("HOMEFRAGMENT", "onResume")
    }


    private fun hideAllBottomSheets() {
        locationAlertState =
            binding.viewLocation.clLocationAlert.getBottomSheetBehaviour(isDraggableAlert = true)
        carTypeAlertState = binding.viewCarType.clRootView.getBottomSheetBehaviour()
        carDetailAlertState = binding.viewCarDetail.clRootView.getBottomSheetBehaviour()
        startRideAlertState = binding.viewStartRide.clRootView.getBottomSheetBehaviour()
    }


    private fun getSavedStateData() {
        try {
            findNavController().currentBackStackEntry?.savedStateHandle?.let {
                Log.i("ADDADDRESS","ONHOME")
                if (it.contains("pickUpLocation")) {
                    rideVM.createRideData.pickUpLocation =
                        it.get<CreateRideData.LocationData>("pickUpLocation")
                    rideVM.updateUiState(RideVM.RideAlertUiState.ShowLocationDialog)
                }
                if (it.contains("dropLocation")) {
                    rideVM.createRideData.dropLocation =
                        it.get<CreateRideData.LocationData>("dropLocation")
                    rideVM.updateUiState(RideVM.RideAlertUiState.ShowLocationDialog)
                }
                if (it.contains("add_address")) {
                    Log.i("ADDADDRESS","ONHOME inside if")
                    rideVM.updateUiState(RideVM.RideAlertUiState.ShowLocationDialog)
                }
                hideAllBottomSheets()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        hideAllBottomSheets()
    }

    private fun clickHandler() {
        binding.tvNow.setOnSingleClickListener {
            binding.tvNow.text = requireContext().getString(R.string.schedule)
            startTimeDialog(requireContext())
        }

        binding.rlSchedule.setOnSingleClickListener {
            binding.tvNow.text = requireContext().getString(R.string.schedule)
            startTimeDialog(requireContext())
        }
        binding.tvWhereTo.setOnSingleClickListener {
            rideVM.updateUiState(RideVM.RideAlertUiState.ShowLocationDialog)
        }
        binding.rlRide.setOnSingleClickListener {
            rideVM.updateUiState(RideVM.RideAlertUiState.ShowLocationDialog)
        }

        if (rideVM.rideAlertUiState.value == RideVM.RideAlertUiState.HomeScreen || rideVM.rideAlertUiState.value == RideVM.RideAlertUiState.ShowLocationDialog) {
            Log.i("fetchOngoingTrip", "clickHandler")
            rideVM.fetchOngoingTrip()
        } else if (rideVM.rideAlertUiState.value == RideVM.RideAlertUiState.FindDriverDialog) {
            binding.clWhereMain.visibility = View.GONE
            binding.clMapMain.visibility = View.VISIBLE
            startRideDialog(requireContext(), RideVM.RideAlertUiState.FindDriverDialog)
            startRideAlertState?.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun startWhereDialog() {
        with(binding.viewLocation) {
            rideVM.createRideData.pickUpLocation?.address?.let {
                tvPickUpValue.text = it
            }
            rideVM.createRideData.dropLocation?.address?.let {
                tvDropOffValue.text = it
            }

            tvConfirmBtn.setOnSingleClickListener {
                if (rideVM.createRideData.pickUpLocation == null) {
                    showSnackBar("*Please select pickup location.", this)
                } else if (rideVM.createRideData.dropLocation == null) {
                    showSnackBar("*Please select drop location.", this)
                } else {
                    locationAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
                    rideVM.findDriver()
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
                tvCarType.text = it.name.orEmpty()
                tvTime.text = it.eta.orEmpty().plus(" min")
                tvPersonCount.text = it.totalCapacity.orEmpty().ifEmpty { "0" }
                tvPrice.text =
                    "${it.price.formatString()} ${rideVM.createRideData.currencyCode.orEmpty()}"
                Glide.with(requireContext()).load(it.image.orEmpty()).error(R.mipmap.ic_launcher)
                    .into(ivCarImage)
            }

            SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
                ?.let {
                    tvCustomerNameValue.text = it.login?.userName.orEmpty()
                    tvNumber.text = it.login?.phoneNo.orEmpty()
                }

            tvConfirmBtn.setOnSingleClickListener {
                carDetailAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
                rideVM.requestRideData(etNotes.text?.trim().toString())
            }
        }
    }


    private fun setMapFragment(view: View, savedInstanceState: Bundle?) {

        MapsInitializer.initialize(view.context, MapsInitializer.Renderer.LATEST) {
            binding.fragmentMap.onCreate(savedInstanceState)
            binding.fragmentMapFullScreen.onCreate(savedInstanceState)
            binding.fragmentMap.getMapAsync {
                nearByDriverMap = it
                OnMapReadyCallback {
//                    it.moveCamera(CameraUpdateFactory.newLatLng(LatLng(5454.1, 556.9)))
                }
            }
            binding.fragmentMapFullScreen.getMapAsync {
                googleMap = it
            }
        }


    }

    private fun startTimeDialog(context: Context) {
        val dialog = BottomSheetDialog(context, R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = DialogDateTimeBinding.inflate(LayoutInflater.from(context), null, false)
        dialog.setContentView(binding.root)


        binding.cvPickDrop.visibility = if (schedule) View.VISIBLE else View.GONE
        binding.clHome.visibility = if (schedule) View.VISIBLE else View.GONE

        rideVM.createRideData.pickUpLocation?.address?.let {
            binding.tvPickUpValue.text = it
        }
        rideVM.createRideData.dropLocation?.address?.let {
            binding.tvDropOffValue.text = it
        }


        binding.tvSubmitBtn.setOnSingleClickListener {
            this@HomeFragment.binding.tvNow.text = context.getString(R.string.schedule)
            schedule = true
            dialog.dismiss()
        }


        binding.clHome.setOnSingleClickListener {
            dialog.dismiss()
            val bundle = bundleOf("selectLocationType" to "add_address")
            findNavController().navigate(R.id.navigation_select_location, bundle)
        }
        binding.tvSelectPickUp.setOnSingleClickListener {
            dialog.dismiss()
            val bundle = bundleOf("selectLocationType" to "pickUp")
            findNavController().navigate(R.id.navigation_select_location, bundle)
        }
        binding.tvSelectDropOff.setOnSingleClickListener {
            dialog.dismiss()
            val bundle = bundleOf("selectLocationType" to "dropOff")
            findNavController().navigate(R.id.navigation_select_location, bundle)

        }

        binding.tvPickUpValue.setOnSingleClickListener {
            dialog.dismiss()
            val bundle = bundleOf("selectLocationType" to "pickUp")

            findNavController().navigate(R.id.navigation_select_location, bundle)
        }
        binding.tvDropOffValue.setOnSingleClickListener {
            dialog.dismiss()
            val bundle = bundleOf("selectLocationType" to "dropOff")
            findNavController().navigate(R.id.navigation_select_location, bundle)

        }

        dialog.setCancelable(true)
        dialog.show()
    }

    private fun startRideDialog(context: Context, rideAlertUiState: RideVM.RideAlertUiState) {
        with(binding.viewStartRide) {
            clConnecting.visibility = View.VISIBLE
            this@HomeFragment.binding.cvTitle.visibility = View.VISIBLE
            this@HomeFragment.binding.ivBack.visibility = View.GONE

            with(rideVM.createRideData) {
                tvDriverRating.text = driverDetail?.driverRating.orEmpty().ifEmpty { "0.0" }
                tvUserName.text = driverDetail?.driverName.orEmpty()
                tvNumber.text = vehicleData?.vehicleNumber.orEmpty()
                tvCarName.text = vehicleData?.name.orEmpty()
                tvNumber.text = vehicleData?.vehicleNumber.orEmpty()
                tvAddress.text = pickUpLocation?.address.orEmpty()
                tvDistanceValue.text = vehicleData?.distance.orEmpty()
                tvTimeValue.text = "${vehicleData?.eta.orEmpty()} min"
                tvPriceValue.text = "${vehicleData?.price.orEmpty().ifEmpty { "0.0" }}"
                Glide.with(requireView()).load(driverDetail?.driverImage.orEmpty())
                    .error(R.mipmap.ic_launcher).into(ivProfileImage)
                Glide.with(requireView()).load(vehicleData?.image.orEmpty())
                    .error(R.mipmap.ic_launcher)
                    .into(ivCar)
            }


            if (rideAlertUiState == RideVM.RideAlertUiState.FindDriverDialog) {
                clConnecting.visibility = View.VISIBLE
                clPickUpLocation.visibility = View.GONE
            } else {
                clConnecting.visibility = View.GONE
                clPickUpLocation.visibility = View.VISIBLE
            }


            ivChat.setOnSingleClickListener {
                startActivity(Intent((activity as BaseActivity<*>), ChatActivity::class.java))
            }
            tvConnecting.setOnSingleClickListener {
                clConnecting.visibility = View.GONE
                clPickUpLocation.visibility = View.VISIBLE
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
            when (rideVM.createRideData.status) {
                TripStatus.ACCEPTED.type -> {
                    binding.viewStartRide.llDistanceTime.isVisible = false
                    requireContext().showPath(
                        srcLat = LatLng(
                            rideVM.createRideData.driverLocation?.latitude?.toDouble() ?: 0.0,
                            rideVM.createRideData.driverLocation?.longitude?.toDouble() ?: 0.0
                        ),
                        desLat = LatLng(
                            rideVM.createRideData.pickUpLocation?.latitude?.toDouble() ?: 0.0,
                            rideVM.createRideData.pickUpLocation?.longitude?.toDouble() ?: 0.0
                        ),
                        mMap = googleMap
                    ) {
                        setPathTimeAndDistance(
                            durationDistance = it.distanceText.orEmpty(),
                            durationTime = it.durationText.orEmpty()
                        )
                    }
                }

                TripStatus.ARRIVED.type -> {
                    tvAddress.text = context.getString(R.string.txt_driver_arrived)
                    tvAddress.setCompoundDrawables(null, null, null, null)
                    clDistanceTime.visibility = View.GONE
                    this@HomeFragment.binding.tvTitleStatus.text =
                        context.getString(R.string.txt_your_volt_has_arrived)
                    binding.viewStartRide.llDistanceTime.isVisible = true
                }

                TripStatus.STARTED.type -> {
                    tvAddress.text = context.getString(R.string.txt_ride_in_progress)
                    tvAddress.setCompoundDrawables(null, null, null, null)
                    clDistanceTime.visibility = View.GONE
                    this@HomeFragment.binding.tvTitleStatus.text =
                        context.getString(R.string.txt_volt_ride_initiated)
                    binding.viewStartRide.llDistanceTime.isVisible = true
                    requireContext().showPath(
                        srcLat = LatLng(
                            rideVM.createRideData.pickUpLocation?.latitude?.toDouble() ?: 0.0,
                            rideVM.createRideData.pickUpLocation?.longitude?.toDouble() ?: 0.0
                        ),
                        desLat = LatLng(
                            rideVM.createRideData.dropLocation?.latitude?.toDouble() ?: 0.0,
                            rideVM.createRideData.dropLocation?.longitude?.toDouble() ?: 0.0
                        ),
                        mMap = googleMap
                    ) {
                        setPathTimeAndDistance(
                            durationDistance = it.distanceText.orEmpty(),
                            durationTime = it.durationText.orEmpty()
                        )
                    }
                }

                TripStatus.ENDED.type -> {
                    binding.viewStartRide.llDistanceTime.isVisible = false
                    startRideAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
                    googleMap?.clearMap()
                    findNavController().navigate(R.id.navigation_ride_complete)
                }
            }
        }
    }

    private fun startCarTypesDialog() {
        with(binding.viewCarType) {
            tvSelectTypeBtn.setOnSingleClickListener {
                if (rideVM.createRideData.regionId.isNullOrEmpty()) {
                    showSnackBar(
                        getString(R.string.please_choose_vehicle_type),
                        this@with.clRootView
                    )
                    return@setOnSingleClickListener
                }
                carTypeAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
                rideVM.updateUiState(RideVM.RideAlertUiState.ShowCustomerDetailPaymentDialog)
            }
            rvCarsType.adapter = CarsTypeAdapter(
                rideVM.regionsList, currencyCode = rideVM.createRideData.currencyCode.orEmpty()
            ) {
                rideVM.createRideData.regionId = it?.regionId.orEmpty()
                rideVM.createRideData.vehicleType = it?.vehicleType.orEmpty()
                rideVM.createRideData.vehicleData = CreateRideData.VehicleData(
                    image = it?.images?.rideNowNormal2x.orEmpty(),
                    name = it?.regionName.orEmpty(),
                    totalCapacity = it?.maxPeople.orEmpty(),
                    eta = it?.eta.orEmpty(),
                    price = it?.vehicleAmount.formatString()
                )
            }
        }

    }


    /**
     * Set Path Time and Distance
     * */
    private fun setPathTimeAndDistance(
        durationDistance: String,
        durationTime: String
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

        listData.add(BannerData(ContextCompat.getDrawable(context, R.drawable.ic_banner_image)))
        listData.add(BannerData(ContextCompat.getDrawable(context, R.drawable.ic_banner_image)))
        listData.add(BannerData(ContextCompat.getDrawable(context, R.drawable.ic_banner_image)))
        listData.add(BannerData(ContextCompat.getDrawable(context, R.drawable.ic_banner_image)))
        binding.vpBanners.adapter = BannerAdapter(listData)
    }


    private fun backButtonMapView() {
        binding.ivBack.setOnSingleClickListener {
            locationAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
            carTypeAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
            carDetailAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
            startRideAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
            when (rideVM.rideAlertUiState.value) {
                RideVM.RideAlertUiState.ShowVehicleTypesDialog -> {
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


    private fun uiStateHandler(state: RideVM.RideAlertUiState) {
        try {
            when (state) {
                RideVM.RideAlertUiState.HomeScreen -> {
//                    Intent(requireActivity(), Home::class.java).apply {
//                        startActivity(this)
//                        requireActivity().finishAffinity()
//                    }
                }

                RideVM.RideAlertUiState.ShowLocationDialog -> {
                    startWhereDialog()
                    binding.clWhereMain.visibility = View.VISIBLE
                    binding.clMapMain.visibility = View.GONE
                    locationAlertState?.state = BottomSheetBehavior.STATE_EXPANDED
                }

                RideVM.RideAlertUiState.ShowVehicleTypesDialog -> {
                    binding.clWhereMain.visibility = View.GONE
                    binding.clMapMain.visibility = View.VISIBLE
                    startCarTypesDialog()
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
                }

                RideVM.RideAlertUiState.ShowCustomerDetailPaymentDialog -> {
                    binding.clWhereMain.visibility = View.GONE
                    binding.clMapMain.visibility = View.VISIBLE
                    carDetailDialog(requireContext())
                    carDetailAlertState?.state = BottomSheetBehavior.STATE_EXPANDED
                }

                RideVM.RideAlertUiState.FindDriverDialog -> {
                    binding.clWhereMain.visibility = View.GONE
                    binding.clMapMain.visibility = View.VISIBLE
                    startRideDialog(requireContext(), RideVM.RideAlertUiState.FindDriverDialog)
                    startRideAlertState?.state = BottomSheetBehavior.STATE_EXPANDED
                }

                RideVM.RideAlertUiState.ShowCustomerDetailDialog -> {
                    binding.clWhereMain.visibility = View.GONE
                    binding.clMapMain.visibility = View.VISIBLE
                    rideStatus = AppConstants.DRIVER_ARRIVED
                    startRideDialog(
                        requireContext(), RideVM.RideAlertUiState.ShowCustomerDetailDialog
                    )
                    startRideAlertState?.state = BottomSheetBehavior.STATE_EXPANDED
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun observeFetchOngoingTrip() =
        rideVM.fetchOngoingTripData.observeData(lifecycle = viewLifecycleOwner, onLoading = {
//            showProgressDialog()
            if (activity != null)
                binding.llInProgessRide.isVisible = true
        }, onError = {
//            hideProgressDialog()
            if (activity != null)
                binding.llInProgessRide.isVisible = false
            showSnackBar(this)
        }, onSuccess = {
//            hideProgressDialog()
            if (activity != null)
                binding.llInProgessRide.isVisible = false
            if (this?.trips?.isNotEmpty() == true) {
                this.trips.firstOrNull()?.let { trip ->
                    with(rideVM.createRideData) {
                        currencyCode = trip.currency.orEmpty()
                        tripId = trip.tripId.orEmpty()
                        sessionId = trip.sessionId.orEmpty()
                        status = trip.status
                        vehicleData = vehicleData?.copy(
                            image = trip.image.orEmpty(),
                            name = trip.modelName.orEmpty(),
                            totalCapacity = null,
                            eta = trip.dryEta.orEmpty(),
                            distance = trip.estimatedDistance.orEmpty(),
                            price = trip.estimatedDriverFare.orEmpty(),
                            vehicleNumber = trip.licensePlate.orEmpty()
                        )
                        driverDetail = driverDetail?.copy(
                            driverImage = trip.driverImage.orEmpty(),
                            driverName = trip.driverName.orEmpty(),
                            driverId = trip.driverId.orEmpty(),
                            driverRating = trip.driverRating.orEmpty()
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
                    SocketSetup.startRideEmit(rideVM.createRideData.tripId.orEmpty())
                    rideVM.updateUiState(RideVM.RideAlertUiState.ShowCustomerDetailDialog)
                }
            } else {
                var alreadyHittingApi = false
                rideVM.updateUiState(RideVM.RideAlertUiState.HomeScreen)
                SingleFusedLocation.initialize(requireContext(), object : LocationResultHandler {
                    override fun updatedLocation(location: Location) {
                        if (!alreadyHittingApi) {
                            alreadyHittingApi = true
                            nearByDriverMap?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        location.latitude,
                                        location.longitude
                                    ), 12f
                                )
                            )
                            rideVM.findNearDriver(location.latitude, location.longitude)
                        }
                    }
                })
            }
        })


    /**
     * Observe Near Driver
     * */
    private fun observeNearDriver() =
        rideVM.findNearDriverData.observeData(lifecycle = viewLifecycleOwner, onLoading = {
//        showProgressDialog()
            if (activity != null)
                binding.progressMap.isVisible = true

        }, onSuccess = {
//        hideProgressDialog()
            if (activity != null)
                binding.progressMap.isVisible = false
            try {
                this?.drivers?.forEach {
                    val marker = nearByDriverMap?.addMarker(
                        MarkerOptions().position(LatLng(it.latitude ?: 0.0, it.longitude ?: 0.0))
                            .apply {
                                icon(context?.vectorToBitmap(R.drawable.car_icon))
                                anchor(0.5f, 1f)
                            }
                    )
                    marker?.rotation = it.bearing?.toFloatOrNull() ?: 0F
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, onError = {
//        hideProgressDialog()
            if (activity != null)
                binding.progressMap.isVisible = false
        })


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
            rideVM.fareStructureList.addAll(this?.fareStructure ?: emptyList())
            rideVM.getDistanceFromGoogle(isLoading = {
                showProgressDialog()
            }, onSuccess = { distance, time ->
                hideProgressDialog()
                this?.regions?.map {
                    it.rideTotalDistance = distance
                    it.rideTotalTime = time
                    it.vehicleAmount = it.calculateFearStructure(rideVM.fareStructureList)
                }
                rideVM.regionsList.addAll(this?.regions ?: emptyList())
                rideVM.updateUiState(RideVM.RideAlertUiState.ShowVehicleTypesDialog)
            }, onError = {
                hideProgressDialog()
            })
        })


    private fun observeRequestRide() =
        rideVM.requestRideData.observeData(lifecycle = viewLifecycleOwner, onLoading = {
            showProgressDialog()
        }, onError = {
            hideProgressDialog()
            showToastShort(this)
        }, onSuccess = {
            hideProgressDialog()
            rideVM.createRideData.sessionId = this?.sessionId.orEmpty()
            rideVM.updateUiState(RideVM.RideAlertUiState.FindDriverDialog)
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

    override fun rideEnd(tripId: String, driverId: String) {
        requireActivity().runOnUiThread {
            try {
                requireView().findNavController().navigate(
                    R.id.navigation_ride_details, bundleOf(
                        "tripId" to tripId,
                        "driverId" to driverId
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun requestTimeout() {
        super.requestTimeout()
        Log.i("fetchOngoingTrip", "requestTimeout")
        rideVM.fetchOngoingTrip()
    }


    override fun driverLocation(latLng: LatLng, bearing: Float) {
        super.driverLocation(latLng, bearing)
        requireActivity().runOnUiThread {
            requireContext().animateDriver(
                driverLatitude = latLng.latitude,
                driverLongitude = latLng.longitude,
                bearing = bearing.toDouble(),
                googleMap = googleMap
            ) { distance, duration ->
                setPathTimeAndDistance(
                    durationDistance = distance,
                    durationTime = duration
                )
            }
        }
    }
}