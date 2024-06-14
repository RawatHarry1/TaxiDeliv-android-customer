package com.venus_customer.view.fragment.trips

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.venus_customer.R
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.FragmentRideDetailsBinding
import com.venus_customer.model.api.observeData
import com.venus_customer.model.dataClass.tripsDC.RideSummaryDC
import com.venus_customer.util.convertDouble
import com.venus_customer.util.formatString
import com.venus_customer.util.getTime
import com.venus_customer.util.showSnackBar
import com.venus_customer.view.activity.chat.ChatActivity
import com.venus_customer.view.base.BaseFragment
import com.venus_customer.viewmodel.rideVM.RideVM
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RideDetailsFragment : BaseFragment<FragmentRideDetailsBinding>() {

    lateinit var binding: FragmentRideDetailsBinding
    private val navArgs by navArgs<RideDetailsFragmentArgs>()
    private val viewModel by viewModels<RideVM>()


    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_ride_details
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        observeRideSummary()
        viewModel.rideSummary(
            tripId = navArgs.tripId,
            driverId = navArgs.driverId
        )
    }

    override fun onResume() {
        super.onResume()

        setClicks()
    }

    private fun setClicks() {
        binding.ivBack.setOnSingleClickListener {
            findNavController().popBackStack()
        }

        binding.tvRateDriver.setOnSingleClickListener {
            findNavController().navigate(R.id.navigation_rate_driver, bundleOf("engagementId" to viewModel.createRideData.sessionId, "driverName" to binding.tvDriverName.text.toString()))
        }

        binding.ivCustomerSupport.setOnSingleClickListener {
            startActivity(Intent(requireContext(), ChatActivity::class.java))
        }
    }


    private fun observeRideSummary() = viewModel.rideSummaryData.observeData(
        lifecycle = viewLifecycleOwner,
        onLoading = {
            showProgressDialog()
            binding.nsvScrollView.isVisible = false
        }, onError = {
            hideProgressDialog()
            showSnackBar(this)
        }, onSuccess = {
            hideProgressDialog()
            setUpUI(this)
        }
    )


    private fun setUpUI(rideSummaryDC: RideSummaryDC?) {
        try {
            viewModel.createRideData.sessionId = rideSummaryDC?.engagementId.orEmpty()
            binding.tvTitle.text = rideSummaryDC?.autosStatusText.orEmpty()
            binding.tvStartTime.text = rideSummaryDC?.pickupTime.getTime(output = "HH:mm", applyTimeZone = true)
            binding.tvEndTime.text = rideSummaryDC?.dropTime.getTime(output = "HH:mm", applyTimeZone = true)
            binding.tvStartAdress.text = rideSummaryDC?.pickupAddress.orEmpty()
            binding.tvEndAddress.text = rideSummaryDC?.dropAddress.orEmpty()
            binding.tvDriverName.text = rideSummaryDC?.driverName.orEmpty()
            if ((rideSummaryDC?.driverRating ?: -1) >= 0){
                binding.tvRatings.text = rideSummaryDC?.driverRating.toString()
            }else {
                binding.tvRatings.text = "0"
            }
            binding.tvCarNo.text = rideSummaryDC?.driverCarNo.orEmpty()
            binding.tvCarBrand.text = rideSummaryDC?.modelName.orEmpty()

            binding.tvHowTrip.text = "How was your trip with ${rideSummaryDC?.driverName.orEmpty()}"
            binding.tvHowTrip.isVisible = (rideSummaryDC?.driverRating ?: -1) <= -1
            binding.tvRateDriver.isVisible = (rideSummaryDC?.driverRating ?: -1) <= -1

            binding.tvPaymentCash.text = "${rideSummaryDC?.currency.orEmpty()} ${rideSummaryDC?.fare.orEmpty().formatString()}"
            binding.tvSubTotal.text = "${rideSummaryDC?.currency.orEmpty()} ${rideSummaryDC?.fare.orEmpty().formatString()}"
            binding.tvVat.text = "${rideSummaryDC?.currency.orEmpty()} ${rideSummaryDC?.netCustomerTax.orEmpty().formatString()}"
            binding.tvDiscount.text = "${rideSummaryDC?.currency.orEmpty()} ${rideSummaryDC?.fareDiscount.orEmpty().formatString()}"
            binding.tvTotalCharge.text = "${rideSummaryDC?.currency.orEmpty()} ${rideSummaryDC?.tripTotal.orEmpty().formatString()}"

            Glide.with(this).load(rideSummaryDC?.driverImage.orEmpty()).error(R.drawable.circleimage).into(binding.ivDriverImage)
            Glide.with(this).load(rideSummaryDC?.trackingImage.orEmpty()).into(binding.mapImage)
            binding.nsvScrollView.isVisible = true
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

}