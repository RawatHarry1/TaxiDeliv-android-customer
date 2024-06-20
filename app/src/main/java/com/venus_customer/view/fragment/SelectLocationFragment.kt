package com.venus_customer.view.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.venus_customer.R
import com.venus_customer.VenusApp
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.customClasses.trackingData.vectorToBitmap
import com.venus_customer.databinding.DialogAddAddressBinding
import com.venus_customer.databinding.FragmentSelectLocationBinding
import com.venus_customer.model.api.Status
import com.venus_customer.model.api.getJsonRequestBody
import com.venus_customer.util.convertDouble
import com.venus_customer.util.showSnackBar
import com.venus_customer.util.textChanges
import com.venus_customer.view.activity.walk_though.ui.home.PickDropAdapter
import com.venus_customer.view.base.BaseFragment
import com.venus_customer.viewmodel.SearchLocationVM
import com.venus_customer.viewmodel.rideVM.CreateRideData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL


@AndroidEntryPoint
class SelectLocationFragment : BaseFragment<FragmentSelectLocationBinding>() {
    lateinit var binding: FragmentSelectLocationBinding
    private val args: SelectLocationFragmentArgs by navArgs()
    private var googleMap: GoogleMap? = null
    private var locationData: CreateRideData.LocationData? = null
    private val viewModel by viewModels<SearchLocationVM>()
    private var isSearchEnable: Boolean = true
    private val placesClient by lazy { Places.createClient(requireContext()) }
    private val adapter by lazy { PickDropAdapter(::adapterClick) }
    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_select_location
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        setMapFragment(view, savedInstanceState)
        observeSearchPlaces()
        observeAddAddress()
//        placesInitializer()

        val type = args.selectLocationType
        if (type == "add_address") {
            setPickDropAdapter(binding.rvAddAddressPickDrop, view.context)
        } else
            setPickDropAdapter(binding.rvPickDrop, view.context)
        if (type == "pickUp" || type == "add_address")
            lifecycleScope.launch {
                getLocationDataFromLatLng(
                    LatLng(
                        VenusApp.latLng.latitude,
                        VenusApp.latLng.longitude
                    )
                )
            }
        binding.tvConfirmBtn.setOnSingleClickListener {
            if (locationData != null) {
                if (type == "add_address") {
                    addAddressBottomSheet(requireActivity())
                } else {
                    findNavController().previousBackStackEntry?.savedStateHandle?.let {
                        it[if (type == "pickUp") "pickUpLocation" else "dropLocation"] =
                            locationData
                    }
                    findNavController().popBackStack()
                }
            } else {
                showSnackBar("*Please select location.")
            }
        }
        binding.btnCancel.setOnSingleClickListener { findNavController().popBackStack() }
        binding.rlMyGps.setOnSingleClickListener {
            lifecycleScope.launch {
                getLocationDataFromLatLng(
                    LatLng(
                        VenusApp.latLng.latitude,
                        VenusApp.latLng.longitude
                    )
                )
            }
        }

        binding.tvSelectPickDrop.text =
            getString(if (type == "pickUp") R.string.txt_pick_up else R.string.txt_drop_off)

        binding.clPickDrop.visibility = if (type == "add_address") View.GONE else View.VISIBLE
        binding.cvAddress.visibility = if (type == "add_address") View.VISIBLE else View.GONE
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun setMapFragment(view: View, savedInstanceState: Bundle?) {

        MapsInitializer.initialize(view.context, MapsInitializer.Renderer.LATEST) {
            binding.fragmentMapFullScreen.onCreate(savedInstanceState)

            binding.fragmentMapFullScreen.getMapAsync {
                googleMap = it
                val latLng = LatLng(VenusApp.latLng.latitude, VenusApp.latLng.longitude)
                googleMap?.clear()
                if (args.selectLocationType == "pickUp" || args.selectLocationType == "add_address") {
                    googleMap?.addMarker(
                        MarkerOptions().position(latLng).draggable(true)
                            .apply { icon(requireActivity().vectorToBitmap(R.drawable.new_location_placeholder)) })
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                }
                setUpMapListeners()
            }
            if (!Places.isInitialized()) {
                Places.initialize(
                    requireContext().applicationContext,
                    getString(R.string.map_api_key)
                )
            }
        }

        binding.etSearchLocation.textChanges().debounce(1500)
            .onEach {
                if (it.toString().isNotEmpty() && isSearchEnable) {
                    searchPlaces(it.toString())
                }
//                    viewModel.searchPlaces(it.toString())
            }
            .launchIn(lifecycleScope)

        binding.etAddAddressSearchLocation.textChanges().debounce(1500)
            .onEach {
                if (it.toString().isNotEmpty() && isSearchEnable) {
                    searchPlaces(it.toString())
                }
//                    viewModel.searchPlaces(it.toString())
            }.launchIn(lifecycleScope)
    }


    private fun setPickDropAdapter(rvPickDrop: RecyclerView, context: Context) {
        rvPickDrop.adapter = adapter
    }

    private fun observeSearchPlaces() = viewModel.searchLocationData.observe(viewLifecycleOwner) {
        when (it.status) {
            //When api in loading state
            com.venus_customer.model.api.Status.LOADING -> {
                requireActivity().runOnUiThread {
                    if (args.selectLocationType == "add_address")
                        binding.addAddressProgress.isVisible = true
                    else
                        binding.progress.isVisible = true
                }
            }

            //When api getting success
            com.venus_customer.model.api.Status.SUCCESS -> {
                Log.e("dsfsdfdsf", "vv  ${it.data}")
                requireActivity().runOnUiThread {
                    if (args.selectLocationType == "add_address")
                        binding.addAddressProgress.isVisible = false
                    else
                        binding.progress.isVisible = false
                    adapter.submitList(it.data ?: emptyList())
                }
            }

            //When api getting error
            com.venus_customer.model.api.Status.ERROR -> {
                requireActivity().runOnUiThread {
                    if (args.selectLocationType == "add_address")
                        binding.addAddressProgress.isVisible = false
                    else
                        binding.progress.isVisible = false
                }
            }

            //No Status
            else -> Unit
        }
    }

    private fun observeAddAddress() = viewModel.addAddress.observe(viewLifecycleOwner) {
        when (it.status) {
            //When api in loading state
            Status.LOADING -> {
                requireActivity().runOnUiThread {
                    showProgressDialog()
                }
            }

            //When api getting success
            Status.SUCCESS -> {
                requireActivity().runOnUiThread {
                    hideProgressDialog()
                    findNavController().previousBackStackEntry?.savedStateHandle?.let { save ->
                        save["add_address"] = "add_address"
                    }
                    findNavController().popBackStack()
                }
            }

            //When api getting error
            Status.ERROR -> {
                requireActivity().runOnUiThread {
                    hideProgressDialog()
                }
            }

            //No Status
            else -> Unit
        }
    }


    private fun adapterClick(data: CreateRideData.LocationData) {
        try {
            isSearchEnable = false
            adapter.submitList(emptyList())

            if (args.selectLocationType == "add_address") {
                binding.rvAddAddressPickDrop.isVisible = false
                binding.etAddAddressSearchLocation.setText(data.address.orEmpty())
            } else
                binding.etSearchLocation.setText(data.address.orEmpty())
            getLatLngFromPlaces(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun searchPlaces(text: String) = try {
        requireActivity().runOnUiThread {
            if (args.selectLocationType == "add_address")
                binding.addAddressProgress.isVisible = true
            else
                binding.progress.isVisible = true
        }
        val token = AutocompleteSessionToken.newInstance()
        val request =
            FindAutocompletePredictionsRequest.builder()
                .setOrigin(LatLng(VenusApp.latLng.latitude, VenusApp.latLng.longitude))
                .setSessionToken(token)
                .setLocationBias(
                    RectangularBounds.newInstance(
                        LatLng(VenusApp.latLng.latitude, VenusApp.latLng.longitude),
                        LatLng(VenusApp.latLng.latitude, VenusApp.latLng.longitude)
                    )
                )
                .setQuery(text)
                .build()
        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                val array = ArrayList<CreateRideData.LocationData>()
                for (prediction in response.autocompletePredictions) {
                    array.add(
                        CreateRideData.LocationData(
                            address = prediction.getFullText(null).toString(),
                            placeId = prediction.placeId
                        )
                    )
                }
                requireActivity().runOnUiThread {
                    adapter.submitList(array)
                    if (args.selectLocationType == "add_address") {
                        if (adapter.itemCount != 0)
                            binding.rvAddAddressPickDrop.isVisible = true
                        binding.addAddressProgress.isVisible = false
                    } else
                        binding.progress.isVisible = false
                }
            }.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    showSnackBar(exception.localizedMessage.orEmpty())
                }
                requireActivity().runOnUiThread {
                    adapter.submitList(emptyList())
                    if (adapter.itemCount != 0)
                        binding.rvAddAddressPickDrop.isVisible = true
                    if (args.selectLocationType == "add_address")
                        binding.addAddressProgress.isVisible = false
                    else
                        binding.progress.isVisible = false
                }
            }
    } catch (e: Exception) {
        e.printStackTrace()
    }


    private fun getLatLngFromPlaces(model: CreateRideData.LocationData) {
        try {
            val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
            val request = FetchPlaceRequest.newInstance(model.placeId.orEmpty(), placeFields)
            placesClient.fetchPlace(request)
                .addOnSuccessListener { response: FetchPlaceResponse ->
                    val place = response.place
                    model.latitude = place.latLng?.latitude.toString()
                    model.longitude = place.latLng?.longitude.toString()
                    googleMap?.clear()
                    val latLng =
                        LatLng(model.latitude.convertDouble(), model.longitude.convertDouble())
                    googleMap?.clear()
                    googleMap?.addMarker(MarkerOptions().position(latLng).draggable(true)
                        .apply { icon(requireActivity().vectorToBitmap(R.drawable.new_location_placeholder)) })
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                    locationData = model
                    Handler(Looper.getMainLooper()).postDelayed({
                        isSearchEnable = true
                    }, 2000)
                }.addOnFailureListener { exception: Exception ->
                    if (exception is ApiException) {
                        showSnackBar(exception.localizedMessage.orEmpty())
                        Handler(Looper.getMainLooper()).postDelayed({
                            isSearchEnable = true
                        }, 2000)
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun setUpMapListeners() {
        googleMap?.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {

            override fun onMarkerDragStart(marker: Marker) {
                // Optionally handle drag start event
            }

            override fun onMarkerDrag(marker: Marker) {
                // Optionally handle drag event
            }

            override fun onMarkerDragEnd(marker: Marker) {
                val position = marker.position
                val latLng = LatLng(position.latitude, position.longitude)
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                lifecycleScope.launch {
                    getLocationDataFromLatLng(latLng)
                }
            }
        })
    }

    private var addressType = "Home"
    private fun addAddressBottomSheet(context: Context) {
        val dialog = BottomSheetDialog(context, R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = DialogAddAddressBinding.inflate(LayoutInflater.from(context), null, false)
        dialog.setContentView(binding.root)
        setSaveAsUI(binding.tvHome, binding)
        binding.tvAddress.text = locationData?.address
        if (locationData != null && locationData?.address != null && locationData?.address?.contains(
                ","
            ) == true
        ) {
            binding.etNickName.setText(locationData?.address?.split(",")?.get(0) ?: "")
        }
        binding.tvHome.setOnSingleClickListener {
            setSaveAsUI(binding.tvHome, binding)
        }
        binding.tvWork.setOnSingleClickListener {
            setSaveAsUI(binding.tvWork, binding)
        }
        binding.tvOther.setOnSingleClickListener {
            setSaveAsUI(binding.tvOther, binding)
        }
        binding.tvAddAddress.setOnSingleClickListener {
            if (locationData != null) {
                Log.i("AddressType", addressType)
                if (addressType == "Other") {
                    if (binding.etOther.text.toString().trim().isEmpty())
                        showSnackBar("Please enter name for Save as!!", binding.clRootView)
                    else {
                        hitAddAddress(
                            binding.etNickName.text.toString().trim(),
                            binding.etOther.text.toString().trim()
                        )
                        dialog.dismiss()
                    }
                } else {
                    hitAddAddress(binding.etNickName.text.toString().trim(), addressType)
                    dialog.dismiss()
                }
            }
        }
        binding.tvChange.setOnSingleClickListener { dialog.dismiss() }
        dialog.setCancelable(true)
        dialog.show()
    }

    private fun hitAddAddress(nickName: String, other: String) {
        viewModel.addAddress(
            JSONObject().apply {
                put("address", locationData?.address)
                put("latitude", locationData?.latitude)
                put("longitude", locationData?.longitude)
                put("google_place_id", locationData?.placeId)
                put("keep_duplicate", 1)
                put("type", other)
                put("nick_name", nickName)
            }.getJsonRequestBody()
        )
    }

    private fun setSaveAsUI(textView: View, binding: DialogAddAddressBinding) {
        if (textView !is TextView)
            return
        clearSaveAsUI(binding)
        when (textView.id) {
            R.id.tvHome -> {
                addressType = "Home"
                binding.etOther.isVisible = false
            }

            R.id.tvWork -> {
                addressType = "Work"
                binding.etOther.isVisible = false
            }

            R.id.tvOther -> {
                addressType = "Other"
                binding.etOther.isVisible = true
            }
        }
        textView.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.bg_filled_gray_4dp)
    }

    private fun clearSaveAsUI(binding: DialogAddAddressBinding) {
        binding.tvHome.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.bg_outline_gray_4dp)
        binding.tvWork.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.bg_outline_gray_4dp)
        binding.tvOther.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.bg_outline_gray_4dp)
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
                        locationData = CreateRideData.LocationData(
                            address = address,
                            latitude = latLng.latitude.toString(),
                            longitude = latLng.longitude.toString(),
                            placeId = placeId
                        )
                        // Optionally update the UI with the new location data
//                    // Ensure to switch back to the main thread for UI updates
                        withContext(Dispatchers.Main) {
                            isSearchEnable = false
                            if (args.selectLocationType == "add_address") {
                                binding.etAddAddressSearchLocation.setText(locationData?.address.orEmpty())
                            } else
                                binding.etSearchLocation.setText(locationData?.address.orEmpty())
                            if (args.selectLocationType != "pickUp" && args.selectLocationType != "add_address") {
                                googleMap?.clear()
                                googleMap?.addMarker(
                                    MarkerOptions().position(latLng).draggable(true)
                                        .apply { icon(requireActivity().vectorToBitmap(R.drawable.new_location_placeholder)) })
                                googleMap?.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        latLng,
                                        12f
                                    )
                                )
                            }
                            Handler(Looper.getMainLooper()).postDelayed({
                                isSearchEnable = true
                            }, 2000)
                        }
                    } else {
                        null
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}