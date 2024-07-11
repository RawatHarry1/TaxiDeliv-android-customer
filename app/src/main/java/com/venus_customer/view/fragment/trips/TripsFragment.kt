package com.venus_customer.view.fragment.trips

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.venus_customer.R
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.FragmentTripsBinding
import com.venus_customer.model.api.observeData
import com.venus_customer.model.dataClass.ScheduleList
import com.venus_customer.model.dataClass.tripsDC.TripListDC
import com.venus_customer.util.showSnackBar
import com.venus_customer.view.base.BaseFragment
import com.venus_customer.viewmodel.rideVM.RideVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TripsFragment : BaseFragment<FragmentTripsBinding>() {

    lateinit var binding: FragmentTripsBinding
    private var isRemoveSchedule = false
    private val tripsAdapter by lazy { TripsAdapter(onClickAdapterLambda) }
    private val scheduleAdapter by lazy {
        ScheduleAdapter(
            requireActivity(),
            onScheduleCancelClickAdapterLambda
        )
    }


    private val viewModel by viewModels<RideVM>()


    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_trips
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        observeTripData()
        observeScheduleData()
        observeRemoveSchedule()
        setUi()
    }

    private fun setUi() {
        binding.rvTrips.adapter = tripsAdapter
        binding.rvSchedule.adapter = scheduleAdapter
        binding.tvTrip.setOnSingleClickListener { setSaveAsUI(binding.tvTrip) }
        binding.tvSchedule.setOnSingleClickListener { setSaveAsUI(binding.tvSchedule) }
        setSaveAsUI(binding.tvTrip)
    }

    private val onClickAdapterLambda = { tripData: TripListDC ->
        findNavController().navigate(
            R.id.navigation_ride_details, bundleOf(
                "tripId" to tripData.engagementId.orEmpty(),
                "driverId" to tripData.driverId.orEmpty(),
                "fromTrip" to true
            )
        )
    }

    private val onScheduleCancelClickAdapterLambda = { scheduleData: ScheduleList ->
        isRemoveSchedule = true
        viewModel.removeSchedule(scheduleData.pickup_id.toString())
    }


    private fun observeTripData() = viewModel.tripListData.observeData(
        lifecycle = viewLifecycleOwner,
        onLoading = {
            binding.shimmerLayout.shimmerLayout.isVisible = true
//            showProgressDialog()
        }, onError = {
//            hideProgressDialog()
            binding.shimmerLayout.shimmerLayout.isVisible = false
            showSnackBar(this)
        }, onSuccess = {
//            hideProgressDialog
            binding.shimmerLayout.shimmerLayout.isVisible = false
            binding.rvTrips.isVisible = this?.isNotEmpty() ?: false
            binding.rvSchedule.isVisible = false
            binding.tvNoData.isVisible = this?.isEmpty() ?: false
            tripsAdapter.submitList(this ?: emptyList())
        }
    )

    private fun observeScheduleData() = viewModel.scheduleListData.observeData(
        lifecycle = viewLifecycleOwner,
        onLoading = {
            if (!isRemoveSchedule)
                binding.shimmerLayout.shimmerLayout.isVisible = true
//            showProgressDialog()
        }, onError = {
            if (!isRemoveSchedule)
                binding.shimmerLayout.shimmerLayout.isVisible = false
//            hideProgressDialog()
            showSnackBar(this)
        }, onSuccess = {
            if (!isRemoveSchedule)
                binding.shimmerLayout.shimmerLayout.isVisible = false
            else
                hideProgressDialog()
            binding.rvTrips.isVisible = false
            binding.rvSchedule.isVisible = this?.isNotEmpty() ?: false
            binding.tvNoData.isVisible = this?.isEmpty() ?: false
            scheduleAdapter.submitList(this ?: emptyList())
        }
    )


    private fun observeRemoveSchedule() = viewModel.removeScheduleData.observeData(
        lifecycle = viewLifecycleOwner,
        onLoading = {
//            showProgressDialog()
        }, onError = {
//            hideProgressDialog()
            showSnackBar(this)
        }, onSuccess = {
//            hideProgressDialog()
            showSnackBar(this?.message ?: "")
            viewModel.allScheduleList()
        }
    )

    private fun setSaveAsUI(textView: View) {
        if (textView !is TextView)
            return
        clearSaveAsUI()
        when (textView.id) {
            R.id.tvTrip -> {
                binding.tvTitle.text = getString(R.string.trip_history)
                viewModel.allTripList()
                binding.rvSchedule.isVisible = false
            }

            else -> {
                binding.tvTitle.text = getString(R.string.schedule_history)
                isRemoveSchedule = false
                viewModel.allScheduleList()
                binding.rvTrips.isVisible = false
            }

        }
        textView.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.bg_filled_gray_4dp)
    }

    private fun clearSaveAsUI() {
        binding.tvTrip.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.bg_outline_gray_4dp)
        binding.tvSchedule.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.bg_outline_gray_4dp)
    }

}