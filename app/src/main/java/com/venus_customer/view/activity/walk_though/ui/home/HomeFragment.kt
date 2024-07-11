package com.venus_customer.view.activity.walk_though.ui.home


import SwipeToShowDeleteCallback
import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.ncorti.slidetoact.SlideToActView
import com.venus_customer.R
import com.venus_customer.VenusApp
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
import com.venus_customer.model.api.getJsonRequestBody
import com.venus_customer.model.api.observeData
import com.venus_customer.model.dataClass.addedAddresses.SavedAddresse
import com.venus_customer.model.dataClass.findDriver.FindDriverDC
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
import com.venus_customer.viewmodel.SearchLocationVM
import com.venus_customer.viewmodel.rideVM.CreateRideData
import com.venus_customer.viewmodel.rideVM.RideVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class NearByDriverMarkers(val latLng: LatLng, val bearing: Float)

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(), NotificationInterface, SocketInterface {

    lateinit var binding: FragmentHomeBinding
    private val rideVM by activityViewModels<RideVM>()
    private val searchLocationVM by viewModels<SearchLocationVM>()
    private var locationAlertState: BottomSheetBehavior<View>? = null
    private var carTypeAlertState: BottomSheetBehavior<View>? = null
    private var carDetailAlertState: BottomSheetBehavior<View>? = null
    private var startRideAlertState: BottomSheetBehavior<View>? = null
    private var googleMap: GoogleMap? = null
    private var nearByDriverMap: GoogleMap? = null
    private var nearByDriverLatLanArrayList = ArrayList<NearByDriverMarkers>()
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val addressAdapter by lazy {
        AddedAddressAdapter(
            requireActivity(),
            ::addressAdapterClick
        )
    }
    private val stateHandle: SavedStateHandle by lazy {
        findNavController().currentBackStackEntry?.savedStateHandle
            ?: throw IllegalStateException("State Handle is null")
    }

    companion object {
        var notificationInterface: NotificationInterface? = null
    }

    var rideStatus = AppConstants.DRIVER_PICKUP
    var schedule = false

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
//        rideVM.createRideData = CreateRideData()
        setBannerAdapter(view.context)
        setMapFragment(view, savedInstanceState)
        getSavedStateData()
        observeUiState()
        observeFindDriver()
        observeRequestRide()
        observeRequestSchedule()
        observeFareEstimate()
        observeFetchOngoingTrip()
        observeNearDriver()
        clickHandler()
        backButtonMapView()
        observeAddedAddress()
        addAdapterSetupAndApiHit()
        getNearByDrivers()
        observeSOS()
//        rideVM.rideAlertUiState.value?.let { uiStateHandler(it) }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                val phone = rideVM.createRideData.driverDetail?.driverPhoneNo ?: ""
                if (phone.isEmpty())
                    showSnackBar("There is some issue in call. Please try after sometimes.")
                else
                    makePhoneCall(phone) // Replace with the phone number you want to call
            } else {
                // Permission denied, show a message to the user
                showSnackBar("Permission denied. Cannot make phone calls.")
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        viewLifecycleOwner.lifecycle.removeObserver(object : DefaultLifecycleObserver {
//            override fun onResume(owner: LifecycleOwner) {
//                super.onResume(owner)
//                getSavedStateData()
//            }
//        })
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
        notificationInterface = this
    }


    private fun hideAllBottomSheets() {
        Log.i("SavedStateData", "in hideAllBottomSheets")
        locationAlertState =
            binding.viewLocation.clLocationAlert.getBottomSheetBehaviour(
                isDraggableAlert = true,
                showFull = true
            )
        carTypeAlertState = binding.viewCarType.clRootView.getBottomSheetBehaviour(
            isDraggableAlert = true,
            showFull = true
        )

        carDetailAlertState =
            binding.viewCarDetail.clRootView.getBottomSheetBehaviour(isDraggableAlert = false)
        startRideAlertState = binding.viewStartRide.clRootView.getBottomSheetBehaviour()
        locationAlertState?.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        rideVM.hideHomeNav(false)
                        rideVM.updateUiState(RideVM.RideAlertUiState.HomeScreen)
                        // Bottom sheet is hidden, attempt to clear state
//                        Handler(Looper.getMainLooper()).postDelayed({
//                            clearSavedStateData()
//                            Log.i("SavedStateData", "State cleared after STATE_HIDDEN")
//                        }, 200) // Adding a delay to ensure state changes have completed
//                        rideVM.createRideData = CreateRideData()
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
                hideAllBottomSheets()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        hideAllBottomSheets()
    }

    private fun clickHandler() {
        binding.tvNow.setOnSingleClickListener {
            binding.tvNow.text = requireContext().getString(R.string.schedule)
            hideAllBottomSheets()
            startTimeDialog(requireContext())
        }

        binding.rlSchedule.setOnSingleClickListener {
            binding.tvNow.text = requireContext().getString(R.string.schedule)
            hideAllBottomSheets()
            startTimeDialog(requireContext())
        }
        binding.tvWhereTo.setOnSingleClickListener {
            rideVM.createRideData = CreateRideData()
            schedule = false
            rideVM.updateUiState(RideVM.RideAlertUiState.ShowLocationDialog)
        }
        binding.rlRide.setOnSingleClickListener {
            rideVM.createRideData = CreateRideData()
            schedule = false
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

    private fun observeSOS() = rideVM.sosData.observeData(
        lifecycle = viewLifecycleOwner,
        onLoading = {
//            showProgressDialog()
        }, onSuccess = {
//            hideProgressDialog()
//            if (activity != null) {
            binding.tvSOS.isVisible = false
//            }
        }, onError = {
//            hideProgressDialog()
            showToastShort(this)
        }
    )

    private fun startWhereDialog() {
        with(binding.viewLocation) {
            tvPickUpValue.text = ""
            tvDropOffValue.text = ""
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
                    rideVM.findDriver(schedule)
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
                tvTime.text = it.eta.orEmpty().ifEmpty { "0" }.plus(" min away")
                tvPersonCount.text = it.totalCapacity.orEmpty().ifEmpty { "0" }
//                tvPrice.text =
//                    "${it.price.formatString()} ${rideVM.createRideData.currencyCode.orEmpty()}"
                tvPrice.text =
                    "${it.currency} ${it.fare}"
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
                if (schedule) {
                    rideVM.scheduleRideData(
                        etNotes.text?.trim().toString(),
                        selectedPickDateTimeForSchedule
                    )
                } else
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

    private var selectedPickDateTimeForSchedule = ""
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
            this@HomeFragment.binding.tvNow.text = context.getString(R.string.schedule)
            val past =
                isDateTimeInPast(binding.tvDateValue.text.toString() + " " + binding.tvTimeValue.text.toString())
            if (past) {
                showSnackBar(
                    "Time must be at least 30 minutes from the current time",
                    binding.clRoot
                )
            } else {
                schedule = true
                selectedPickDateTimeForSchedule =
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
        val inputFormat = SimpleDateFormat("E, MMM dd hh:mm a yyyy", Locale.US)
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
            requireContext(),
            { _, selectedHour, selectedMinute ->
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                val formattedTime =
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
                textView.text = formattedTime
            },
            initialHour, initialMinute, false // `false` for 12-hour format
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
        val formattedTime =
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
        textView.text = formattedTime
    }

    private fun showDatePickerDialog(textView: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val dateFormatter = SimpleDateFormat("E, MMM dd", Locale.getDefault())
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
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
                requireActivity(),
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted
                makePhoneCall(phoneNumber)
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE) -> {
                // Show rationale and request permission
                // You can show a dialog explaining why you need this permission
                showSnackBar("Permission denied. Cannot make phone calls.")
                requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
            }

            else -> {
                // Directly request the permission
                requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
            }
        }
    }

    private fun makePhoneCall(phoneNumber: String) {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$phoneNumber")
        startActivity(callIntent)
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
                tvDistanceValue.text =
                    vehicleData?.distance.orEmpty()
                tvTimeValue.text = "${vehicleData?.eta.orEmpty()} min"
                tvDistanceTime.text = "${vehicleData?.eta.orEmpty()}"
                tvDistanceTimeMin.text = "min"
                tvPriceValue.text = "${vehicleData?.fare}"
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
            ivCall.setOnSingleClickListener {
                val phone = rideVM.createRideData.driverDetail?.driverPhoneNo ?: ""
                if (phone.isEmpty())
                    showSnackBar("There is some issue in call. Please try after sometimes.")
                else
                    checkPermissionAndMakeCall(phone)
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
                            ),
                            desLat = LatLng(
                                rideVM.createRideData.dropLocation?.latitude?.toDouble() ?: 0.0,
                                rideVM.createRideData.dropLocation?.longitude?.toDouble() ?: 0.0
                            ),
                            mMap = googleMap, isTracking = false
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
                        ),
                        desLat = LatLng(
                            rideVM.createRideData.pickUpLocation?.latitude?.toDouble() ?: 0.0,
                            rideVM.createRideData.pickUpLocation?.longitude?.toDouble() ?: 0.0
                        ),
                        mMap = googleMap
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
                        ),
                        desLat = LatLng(
                            rideVM.createRideData.pickUpLocation?.latitude?.toDouble() ?: 0.0,
                            rideVM.createRideData.pickUpLocation?.longitude?.toDouble() ?: 0.0
                        ),
                        mMap = googleMap
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
                        ),
                        desLat = LatLng(
                            rideVM.createRideData.dropLocation?.latitude?.toDouble() ?: 0.0,
                            rideVM.createRideData.dropLocation?.longitude?.toDouble() ?: 0.0
                        ),
                        mMap = googleMap
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
                carTypeAlertState?.isHideable = true
                carTypeAlertState?.state = BottomSheetBehavior.STATE_HIDDEN
                rideVM.updateUiState(RideVM.RideAlertUiState.ShowCustomerDetailPaymentDialog)
            }
            rvCarsType.adapter = CarsTypeAdapter(
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
                    distance = rideVM.customerETA.rideDistance.toString()
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
            carTypeAlertState?.isHideable = true
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
            val marker = googleMap.addMarker(
                MarkerOptions().position(it.latLng)
                    .apply {
                        icon(context?.vectorToBitmap(R.drawable.car_icon))
                        anchor(0.5f, 1f)
                    }
            )
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
                        ),
                        desLat = LatLng(
                            rideVM.createRideData.dropLocation?.latitude?.toDouble() ?: 0.0,
                            rideVM.createRideData.dropLocation?.longitude?.toDouble() ?: 0.0
                        ),
                        mMap = googleMap, isTracking = false
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
                    SocketSetup.startRideEmit(rideVM.createRideData.tripId.orEmpty())
                    rideVM.updateUiState(RideVM.RideAlertUiState.ShowCustomerDetailDialog)
                }
            } else {
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
            if (activity != null)
                binding.progressMap.shimmerLayout.isVisible = true

        }, onSuccess = {
//        hideProgressDialog()
            if (activity != null)
                binding.progressMap.shimmerLayout.isVisible = false
            try {
                nearByDriverLatLanArrayList.clear()
//                val latLongB = LatLngBounds.Builder()
                this?.drivers?.forEach {
                    nearByDriverLatLanArrayList.add(
                        NearByDriverMarkers(
                            LatLng(
                                it.latitude ?: 0.0,
                                it.longitude ?: 0.0
                            ), it.bearing?.toFloatOrNull() ?: 0F
                        )
                    )
                    val marker = nearByDriverMap?.addMarker(
                        MarkerOptions().position(LatLng(it.latitude ?: 0.0, it.longitude ?: 0.0))
                            .apply {
                                icon(context?.vectorToBitmap(R.drawable.car_icon))
                                anchor(0.5f, 1f)
                            }
                    )
                    marker?.rotation = it.bearing?.toFloatOrNull() ?: 0F
//                    latLongB.include(LatLng(it.latitude ?: 0.0, it.longitude ?: 0.0))
                }
//                val bounds = latLongB.build()
//                nearByDriverMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                nearByDriverMap?.addMarker(
                    MarkerOptions().position(VenusApp.latLng)
                        .apply {
                            icon(context?.vectorToBitmap(R.drawable.new_location_placeholder))
                            anchor(0.5f, 1f)
                        }
                )
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
            if (activity != null)
                binding.progressMap.shimmerLayout.isVisible = false
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
            rideVM.updateUiState(RideVM.RideAlertUiState.ShowVehicleTypesDialog)
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


    private fun observeRequestRide() =
        rideVM.requestRideData.observeData(lifecycle = viewLifecycleOwner, onLoading = {
            showProgressDialog()
        }, onError = {
            hideProgressDialog()
            showToastShort(this)
            binding.clWhereMain.visibility = View.VISIBLE
            binding.clMapMain.visibility = View.GONE
            rideVM.hideHomeNav(false)
            rideVM.updateUiState(RideVM.RideAlertUiState.HomeScreen)
        }, onSuccess = {
            hideProgressDialog()
            rideVM.createRideData.sessionId = this?.sessionId.orEmpty()
            rideVM.createRideData.status = 0
            rideVM.updateUiState(RideVM.RideAlertUiState.FindDriverDialog)
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
            hideAllBottomSheets()
            rideVM.hideHomeNav(false)
        }, onSuccess = {
            hideProgressDialog()
            rideVM.hideHomeNav(false)
            showSnackBar("Your ride has been scheduled successfully!!")
            rideVM.createRideData.pickUpLocation = null
            rideVM.createRideData.dropLocation = null
            binding.clWhereMain.visibility = View.VISIBLE
            binding.clMapMain.visibility = View.GONE
            hideAllBottomSheets()
//            rideVM.createRideData.sessionId = this?.sessionId.orEmpty()
//            rideVM.updateUiState(RideVM.RideAlertUiState.FindDriverDialog)
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

    override fun rideEnd(
        tripId: String,
        driverId: String,
        driverName: String,
        engagementId: String
    ) {
        requireActivity().runOnUiThread {
            try {
                findNavController().navigate(
                    R.id.navigation_rate_driver,
                    bundleOf(
                        "engagementId" to tripId,
                        "driverName" to driverName,
                        "driverId" to driverId,
                        "tripId" to tripId
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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


    private suspend fun getLocationDataFromLatLng(latLng: LatLng) {
        withContext(Dispatchers.IO) {
            try {
                val apiKey = getString(R.string.map_api_key)
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
                        rideVM.createRideData.pickUpLocation = CreateRideData.LocationData(
                            address = address,
                            latitude = latLng.latitude.toString(),
                            longitude = latLng.longitude.toString(),
                            placeId = placeId
                        )


                        // Optionally update the UI with the new location data
//                    // Ensure to switch back to the main thread for UI updates
                        withContext(Dispatchers.Main) {
                            rideVM.createRideData.pickUpLocation?.address?.let {
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
                    "CurrentLocation",
                    "In getLocationDataFromLatLng catch::: ${e.localizedMessage}"
                )
                e.printStackTrace()
                null
            }
        }
    }
}