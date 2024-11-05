package com.superapp_customer.view.fragment.cancelRideFragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.superapp_customer.R
import com.superapp_customer.customClasses.singleClick.setOnSingleClickListener
import com.superapp_customer.databinding.FragmentCancelRideBinding
import com.superapp_customer.dialogs.DialogUtils
import com.superapp_customer.model.api.observeData
import com.superapp_customer.util.showSnackBar
import com.superapp_customer.view.activity.walk_though.Home
import com.superapp_customer.view.adapter.CancelRideAdapter
import com.superapp_customer.view.base.BaseFragment
import com.superapp_customer.viewmodel.rideVM.RideVM
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CancelRideFragment : BaseFragment<FragmentCancelRideBinding>() {

    lateinit var binding: FragmentCancelRideBinding
    private val rideVM by viewModels<RideVM>()

    private val cancelReasonList by lazy {
        listOf(
            getString(R.string.pickup_distance_is_more_than_expected),
            getString(R.string.wrong_customer_pickup_location),
            getString(R.string.more_customer_to_onboard_than_specified),
            getString(R.string.driver_taking_too_much_time_after_arrival)
        )
    }

    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_cancel_ride
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        arguments?.let {
            if (it.containsKey("sessionId")) {
                rideVM.createRideData.sessionId = it.getString("sessionId").orEmpty()
            }
        }
        observeCancelRide()
    }

    override fun onResume() {
        super.onResume()

        setClicks()
    }

    private fun setClicks() {
        binding.etReason.doOnTextChanged { text, start, before, count ->
            binding.tvCharCount.text = "$count/150"
        }

        binding.btnCancel.setOnSingleClickListener {
            if (CancelRideAdapter.selectedText.isNullOrEmpty() && binding.etReason.text?.trim().isNullOrEmpty()) {
                showSnackBar(getString(R.string.please_select_cancel_reason))
                return@setOnSingleClickListener
            }
            DialogUtils.getNegativeDialog(
                requireActivity(), getString(R.string.cancel_ride),
                getString(R.string.cancel_the_ride_request)
            ) {
                rideVM.cancelTrip(
                    rideVM.createRideData.sessionId.orEmpty(),
                    CancelRideAdapter.selectedText.orEmpty()
                )
            }
        }

        binding.btnNo.setOnSingleClickListener {
            findNavController().popBackStack()
        }
        binding.ivBack.setOnSingleClickListener {
            binding.btnNo.performClick()
        }
        binding.rvCancelReason.adapter = CancelRideAdapter(cancelReasonList)
    }


    private fun observeCancelRide() = rideVM.cancelTripData.observeData(
        lifecycle = viewLifecycleOwner,
        onLoading = {
            showProgressDialog()
        }, onSuccess = {
            hideProgressDialog()
//            findNavController().popBackStack(R.id.mobile_navigation, true)
//            findNavController().navigate(R.id.navigation_home)
            Intent(requireContext(), Home::class.java).apply {
                startActivity(this)
                requireActivity().finish()
            }
        }, onError = {
            hideProgressDialog()
            showToastShort(this)
        }
    )

}