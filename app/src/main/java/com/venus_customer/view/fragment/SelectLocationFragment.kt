package com.venus_customer.view.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
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
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.venus_customer.R
import com.venus_customer.VenusApp
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.FragmentSelectLocationBinding
import com.venus_customer.util.convertDouble
import com.venus_customer.util.showSnackBar
import com.venus_customer.util.textChanges
import com.venus_customer.view.activity.walk_though.ui.home.PickDropAdapter
import com.venus_customer.view.base.BaseFragment
import com.venus_customer.viewmodel.SearchLocationVM
import com.venus_customer.viewmodel.rideVM.CreateRideData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


@AndroidEntryPoint
class SelectLocationFragment: BaseFragment<FragmentSelectLocationBinding>() {

    lateinit var binding: FragmentSelectLocationBinding
    private val args: SelectLocationFragmentArgs by navArgs()
    private var googleMap: GoogleMap? = null
    private var locationData: CreateRideData.LocationData? = null
    private val viewModel by viewModels<SearchLocationVM>()
    private var isSearchEnable: Boolean = true
    private val placesClient by lazy { Places.createClient(requireContext()) }

    private val adapter by lazy { PickDropAdapter(::adapterClick)}

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
        setPickDropAdapter(binding.rvPickDrop,view.context)
//        placesInitializer()
        val type  = args.selectLocationType
        binding.tvConfirmBtn.setOnSingleClickListener {
        if (locationData != null){
            findNavController().previousBackStackEntry?.savedStateHandle?.let {
                it[if(type=="pickUp") "pickUpLocation" else "dropLocation"] = locationData
            }
            findNavController().popBackStack()
        } else {
            showSnackBar("*Please select location.")
        }
        }

        binding.tvSelectPickDrop.text =  getString(if(type=="pickUp") R.string.txt_pick_up else R.string.txt_drop_off)

        binding.clPickDrop.visibility =  if(type=="add_address") View.GONE else View.VISIBLE
        binding.cvAddress.visibility =  if(type=="add_address") View.VISIBLE else View.GONE
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun setMapFragment(view: View, savedInstanceState: Bundle?) {

        MapsInitializer.initialize(view.context, MapsInitializer.Renderer.LATEST) {
            binding.fragmentMapFullScreen.onCreate(savedInstanceState)

            binding.fragmentMapFullScreen.getMapAsync {
                googleMap = it
                val latLng = LatLng(VenusApp.latLng.latitude, VenusApp.latLng.longitude)
                googleMap?.clear()
                googleMap?.addMarker(MarkerOptions().position(latLng))
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
            }
            if (!Places.isInitialized()){
                Places.initialize(requireContext().applicationContext, getString(R.string.map_api_key))
            }
        }

        binding.etSearchLocation.textChanges().debounce(1500)
            .onEach {
                if (it.toString().isNotEmpty() && isSearchEnable){
                    searchPlaces(it.toString())
                }
//                    viewModel.searchPlaces(it.toString())
            }
            .launchIn(lifecycleScope)
    }


    private fun setPickDropAdapter(rvPickDrop: RecyclerView, context: Context) {
        rvPickDrop.adapter = adapter
    }

    private fun observeSearchPlaces() = viewModel.searchLocationData.observe(viewLifecycleOwner){
        when (it.status) {
            //When api in loading state
            com.venus_customer.model.api.Status.LOADING -> {
                requireActivity().runOnUiThread {
                    binding.progress.isVisible = true
                }
            }

            //When api getting success
            com.venus_customer.model.api.Status.SUCCESS -> {
                Log.e("dsfsdfdsf", "vv  ${it.data}")
                requireActivity().runOnUiThread {
                    binding.progress.isVisible = false
                    adapter.submitList(it.data ?: emptyList())
                }
            }

            //When api getting error
            com.venus_customer.model.api.Status.ERROR -> {
                requireActivity().runOnUiThread {
                    binding.progress.isVisible = false
                }
            }

            //No Status
            else -> Unit
        }
    }


    private fun adapterClick(data: CreateRideData.LocationData){
        try {
            isSearchEnable = false
            adapter.submitList(emptyList())
            binding.etSearchLocation.setText(data.address.orEmpty())
            getLatLngFromPlaces(data)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }



    private fun searchPlaces(text: String) = try {
        requireActivity().runOnUiThread {
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
                    array.add(CreateRideData.LocationData(address = prediction.getFullText(null).toString(), placeId = prediction.placeId))
                }
                requireActivity().runOnUiThread {
                    adapter.submitList(array)
                    binding.progress.isVisible = false
                }
            }.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    showSnackBar(exception.localizedMessage.orEmpty())
                }
                requireActivity().runOnUiThread {
                    adapter.submitList(emptyList())
                    binding.progress.isVisible = false
                }
            }
    }catch (e:Exception){
        e.printStackTrace()
    }


    private fun getLatLngFromPlaces(model: CreateRideData.LocationData){
        try {
            val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
            val request = FetchPlaceRequest.newInstance(model.placeId.orEmpty(), placeFields)
            placesClient.fetchPlace(request)
                .addOnSuccessListener { response: FetchPlaceResponse ->
                    val place = response.place
                    model.latitude = place.latLng?.latitude.toString()
                    model.longitude = place.latLng?.longitude.toString()
                    googleMap?.clear()
                    val latLng = LatLng(model.latitude.convertDouble(), model.longitude.convertDouble())
                    googleMap?.clear()
                    googleMap?.addMarker(MarkerOptions().position(latLng).title(model.address))
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                    locationData = model
                    Handler(Looper.getMainLooper()).postDelayed({
                        isSearchEnable = true
                    }, 2000)
                }.addOnFailureListener { exception: Exception ->
                    if (exception is ApiException) {
                        showSnackBar(exception.localizedMessage.orEmpty())
                    }
                }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

}