package com.venus_customer.view.fragment.trips

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.venus_customer.R
import com.venus_customer.databinding.FragmentTripsBinding
import com.venus_customer.model.api.observeData
import com.venus_customer.model.dataClass.tripsDC.TripListDC
import com.venus_customer.util.showSnackBar
import com.venus_customer.view.base.BaseFragment
import com.venus_customer.viewmodel.rideVM.RideVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TripsFragment : BaseFragment<FragmentTripsBinding>() {

    lateinit var binding: FragmentTripsBinding

    private val tripsAdapter by lazy { TripsAdapter(onClickAdapterLambda) }


    private val viewModel by viewModels<RideVM>()


    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_trips
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        setUi()
        observeTripData()
        viewModel.allTripList()
    }

    private fun setUi() {
        binding.rvTrips.adapter = tripsAdapter
    }

    private val onClickAdapterLambda = {tripData : TripListDC ->
        findNavController().navigate(R.id.navigation_ride_details, bundleOf(
            "tripId" to tripData.engagementId.orEmpty(),
            "driverId" to tripData.driverId.orEmpty()
        ))
    }


    private fun observeTripData() = viewModel.tripListData.observeData(
        lifecycle = viewLifecycleOwner,
        onLoading = {
            showProgressDialog()
        }, onError = {
            hideProgressDialog()
            showSnackBar(this)
        }, onSuccess = {
            hideProgressDialog()
            binding.rvTrips.isVisible = this?.isNotEmpty() ?: false
            binding.tvNoData.isVisible = this?.isEmpty() ?: false
            tripsAdapter.submitList(this ?: emptyList())
        }
    )

}