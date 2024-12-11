package com.ujeff_customer.view.activity.walk_though.ui.home

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.tabs.TabLayoutMediator
import com.ujeff_customer.R
import com.ujeff_customer.VenusApp
import com.ujeff_customer.customClasses.LocationResultHandler
import com.ujeff_customer.customClasses.SingleFusedLocation
import com.ujeff_customer.customClasses.singleClick.setOnSingleClickListener
import com.ujeff_customer.customClasses.trackingData.vectorToBitmap
import com.ujeff_customer.databinding.FragmentMainHomeBinding
import com.ujeff_customer.databinding.ItemAppTypesBinding
import com.ujeff_customer.databinding.ItemBannersBinding
import com.ujeff_customer.model.api.observeData
import com.ujeff_customer.model.dataClass.base.ClientConfig
import com.ujeff_customer.model.dataClass.userData.UserDataDC
import com.ujeff_customer.util.SharedPreferencesManager
import com.ujeff_customer.util.safeCall
import com.ujeff_customer.view.activity.CreateProfile
import com.ujeff_customer.view.activity.walk_though.Home
import com.ujeff_customer.view.base.BaseFragment
import com.ujeff_customer.viewmodel.HomeVM
import com.ujeff_customer.viewmodel.rideVM.RideVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

@AndroidEntryPoint
class MainHomeFragment : BaseFragment<FragmentMainHomeBinding>() {
    private lateinit var binding: FragmentMainHomeBinding
    private lateinit var appTypeAdapter: AppTypeAdapter
    private val bannerArrayList = ArrayList<UserDataDC.Login.Banners>()
    private lateinit var bannerAdapter: BannerAdapter
    private val appTypeArrayList = ArrayList<ClientConfig.OperatorAvailablity>()
    private var nearByDriverMap: GoogleMap? = null
    private var currentPage = 0
    private val handler = Handler(Looper.getMainLooper())
    private val homeViewModel by viewModels<HomeVM>()
    private val rideVM by activityViewModels<RideVM>()
    private val slideRunnable = object : Runnable {
        override fun run() {
            currentPage = (currentPage + 1) % bannerArrayList.size
            binding.bannerViewPager.setCurrentItem(currentPage, true)
            handler.postDelayed(this, 3000) // 3 seconds
        }
    }

    companion object {
        var onGoingRideType = 0
    }


    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(slideRunnable) // Stop sliding when fragment is destroyed
    }

    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_main_home
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.loginViaToken2()
        rideVM.fetchOngoingTrip()
    }

    private fun observeProfileData() = homeViewModel.loginViaToken.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        SharedPreferencesManager.putModel(SharedPreferencesManager.Keys.USER_DATA, this)
        if (activity != null) {
            binding.tvHi.text = "Hi ${this?.login?.userName.orEmpty() ?: ""},"
            Glide.with(requireActivity()).load(this?.login?.userImage.orEmpty())
                .error(R.drawable.circleimage).into(binding.ivProfileImage)
            bannerArrayList.clear()
            bannerArrayList.addAll(this?.login?.banner.orEmpty())
            handler.removeCallbacks(slideRunnable)
            if (bannerArrayList.isNotEmpty()) {
                bannerAdapter.notifyDataSetChanged()
                // Start auto-sliding
                handler.post(slideRunnable)
            } else {
                binding.bannerViewPager.isVisible = false
                binding.tabLayoutDots.isVisible = false
            }
        }
    }, onError = {
        hideProgressDialog()
        showToastShort(this)
    })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        observeFetchOngoingTrip()
        lifecycleScope.launch {
            getLocationDataFromLatLng(VenusApp.latLng)
        }
        observeProfileData()
        SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
            ?.let {
                binding.tvHi.text = "Hi ${it.login?.userName.orEmpty()},"
                Glide.with(view.context).load(it.login?.userImage.orEmpty())
                    .error(R.drawable.circleimage).into(binding.ivProfileImage)
            }

        setMapFragment()
        appTypeAdapter = AppTypeAdapter()
        SharedPreferencesManager.getModel<ClientConfig>(SharedPreferencesManager.Keys.CLIENT_CONFIG)
            ?.let {
                appTypeArrayList.clear()
                appTypeArrayList.addAll(it.operatorAvailablity.orEmpty())
                if (appTypeArrayList.isNotEmpty()) {
                    SharedPreferencesManager.put(
                        SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID,
                        appTypeArrayList[0].id ?: 0
                    )
                }
                VenusApp.isServiceTypeDefault = true
                binding.rvAppTypes.adapter = appTypeAdapter
            }

//        bannerArrayList.clear()
//        bannerArrayList.add(
//            ContextCompat.getDrawable(
//                requireActivity(),
//                R.drawable.ic_banner_image_new
//            )
//        )
//        bannerArrayList.add(
//            ContextCompat.getDrawable(
//                requireActivity(),
//                R.drawable.ic_banner_image_new
//            )
//        )
//        bannerArrayList.add(
//            ContextCompat.getDrawable(
//                requireActivity(),
//                R.drawable.ic_banner_image_new
//            )
//        )


        bannerAdapter = BannerAdapter()
        binding.bannerViewPager.adapter = bannerAdapter
        binding.tabLayoutDots.setSelectedTabIndicatorColor(Color.WHITE)
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


//        binding.cvWhereTo.setOnSingleClickListener {
//            val intent = Intent(requireActivity(), Home::class.java)
//            startActivity(intent)
//        }
        binding.ivProfileImage.setOnSingleClickListener {
            startActivity(Intent(requireContext(), CreateProfile::class.java).apply {
                putExtra("isEditProfile", true)
            })
        }
        binding.tvRefer.setOnClickListener {
            findNavController().navigate(R.id.navigate_referral)
        }

        binding.tvView.setOnSingleClickListener {
            SharedPreferencesManager.put(
                SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID,
                onGoingRideType
            )
            VenusApp.isServiceTypeDefault = false
            startActivity(Intent(requireActivity(), Home::class.java))
        }
    }

    inner class AppTypeAdapter : RecyclerView.Adapter<AppTypeAdapter.AppTypeViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): AppTypeAdapter.AppTypeViewHolder {
            return AppTypeViewHolder(
                ItemAppTypesBinding.inflate(
                    LayoutInflater.from(requireActivity()),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: AppTypeAdapter.AppTypeViewHolder, position: Int) {
            holder.bind(appTypeArrayList[position])
        }

        override fun getItemCount(): Int {
            return appTypeArrayList.size
        }

        inner class AppTypeViewHolder(private val binding: ItemAppTypesBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(data: ClientConfig.OperatorAvailablity) {
                if (SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID) == data.id) {
                    binding.cardViewRoot.strokeWidth =
                        4 // Set stroke width to highlight the selected card
                    binding.cardViewRoot.strokeColor =
                        ContextCompat.getColor(requireActivity(), R.color.theme)
                } else {
                    binding.cardViewRoot.strokeWidth = 0 // Remove stroke for unselected cards
                }

                if (data.id == 1)
                    binding.clRoot.background =
                        ContextCompat.getDrawable(requireActivity(), R.drawable.bg_ride_gradient)
                else
                    binding.clRoot.background = ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.bg_delivery_gradient
                    )

                binding.tvTaxiBooking.text = data.name
                binding.tvTaxiBookingDesc.text = data.description
                Glide.with(requireActivity()).load(data.image)
                    .error(R.drawable.taxi_booking).into(binding.ivAppImage)

                binding.cardViewRoot.setOnSingleClickListener {
                    SharedPreferencesManager.put(
                        SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID,
                        if (onGoingRideType == 0) data.id ?: 0 else onGoingRideType
                    )
                    notifyDataSetChanged()
                    VenusApp.isServiceTypeDefault = false
                    startActivity(Intent(requireActivity(), Home::class.java))
                }
            }
        }
    }

    private fun setMapFragment() {
        val smallMapFragment =
            childFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
        smallMapFragment.getMapAsync {
            nearByDriverMap = it
            nearByDriverMap?.clear()
//            try {
//                val success = nearByDriverMap?.setMapStyle(
//                    MapStyleOptions.loadRawResourceStyle(
//                        requireContext(), R.raw.map_style
//                    )
//                )
//
//                if (!success!!) {
//                    Log.e("MapFragment", "Style parsing failed.")
//                }
//            } catch (e: Resources.NotFoundException) {
//                Log.e("MapFragment", "Can't find style. Error: ", e)
//            }
            if (VenusApp.latLng.latitude != 0.0) {
                nearByDriverMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            VenusApp.latLng.latitude, VenusApp.latLng.longitude
                        ), 12f
                    )
                )
                nearByDriverMap?.addMarker(MarkerOptions().position(VenusApp.latLng).apply {
                    icon(context?.vectorToBitmap(R.drawable.new_location_placeholder))
                    anchor(0.5f, 1f)
                })
            } else {
                var alreadyHittingApi = false
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
                            nearByDriverMap?.addMarker(
                                MarkerOptions().position(VenusApp.latLng).apply {
                                    icon(context?.vectorToBitmap(R.drawable.new_location_placeholder))
                                    anchor(0.5f, 1f)
                                })
                        }
                    }
                })
            }
        }

    }

    inner class BannerAdapter : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

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
            holder.bind(bannerArrayList[position])
        }

        override fun getItemCount(): Int = bannerArrayList.size
    }

    private suspend fun getLocationDataFromLatLng(latLng: LatLng) {
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
//                        rideVM.createRideData.pickUpLocation = CreateRideData.LocationData(
//                            address = address,
//                            latitude = latLng.latitude.toString(),
//                            longitude = latLng.longitude.toString(),
//                            placeId = placeId
//                        )
                        // Optionally update the UI with the new location data
//                    // Ensure to switch back to the main thread for UI updates
                        withContext(Dispatchers.Main) {
//                            rideVM.createRideData.pickUpLocation?.address?.let {
                            binding.tvLocation.text = address
//                            }
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

    private fun observeFetchOngoingTrip() =
        rideVM.fetchOngoingTripData.observeData(lifecycle = viewLifecycleOwner,
            onLoading = {
            },
            onError = {
                onGoingRideType = 0
                binding.clOnGoingRide.isVisible = false
            },
            onSuccess = {
                if (this?.trips?.isNotEmpty() == true) {
                    this.trips.firstOrNull()?.let { trip ->
                        binding.clOnGoingRide.isVisible = true
                        onGoingRideType = trip.serviceType ?: 0
                        if (trip.serviceType == 1) {
                            binding.clOnGoingRide.background = ContextCompat.getDrawable(
                                requireActivity(),
                                R.drawable.bg_ride_gradient
                            )
                            binding.ivRideType.setImageDrawable(
                                ContextCompat.getDrawable(
                                    requireActivity(),
                                    R.drawable.ic_ride_now
                                )
                            )
                            binding.tvOngoingRideType.text =
                                if (trip.status != 0) "Ongoing Taxi Ride" else "Ongoing Taxi Ride Request"
                            binding.tvOngoingRideDesc.text =
                                if (trip.status != 0) "You have on going ride" else "You have on going ride request"
                        } else {
                            binding.clOnGoingRide.background = ContextCompat.getDrawable(
                                requireActivity(),
                                R.drawable.bg_delivery_gradient
                            )

                            binding.ivRideType.setImageDrawable(
                                ContextCompat.getDrawable(
                                    requireActivity(),
                                    R.drawable.ic_delivery_now
                                )
                            )
                            binding.tvOngoingRideType.text =
                                if (trip.status != 0) "Ongoing Delivery Ride" else "Ongoing Delivery Ride Request"
                            binding.tvOngoingRideDesc.text =
                                if (trip.status != 0) "You have on going delivery" else "You have on going delivery request"
                        }
                    }
                } else {
                    onGoingRideType = 0
                    binding.clOnGoingRide.isVisible = false
                }
            })
}