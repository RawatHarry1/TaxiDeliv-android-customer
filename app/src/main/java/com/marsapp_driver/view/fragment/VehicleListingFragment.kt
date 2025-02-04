package com.marsapp_driver.view.fragment

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.marsapp_driver.R
import com.marsapp_driver.databinding.FragmentVehicleListingBinding
import com.marsapp_driver.model.dataclassses.VehicleListDC
import com.marsapp_driver.view.adapter.VehicleListAdapter
import com.marsapp_driver.view.base.BaseFragment
import com.marsapp_driver.view.ui.home_drawer.HomeActivity
import com.marsapp_driver.viewmodel.UserAccountVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VehicleListingFragment : BaseFragment<FragmentVehicleListingBinding>() {

    lateinit var binding: FragmentVehicleListingBinding
    private val viewModel by viewModels<UserAccountVM>()
    private val adapter by lazy { VehicleListAdapter() }

    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_vehicle_listing
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        binding.rvVehicles.adapter = adapter
        observeVehicleData()
        viewModel.vehicleListData()
    }

    override fun onResume() {
        super.onResume()

        binding.ivMenuBurg.setOnClickListener {
            (activity as HomeActivity).openDrawer()
        }
    }



    private fun observeVehicleData() {
        arguments?.let {
            if (arguments?.containsKey("vehicleData") == true){
                val data = arguments?.getParcelable<VehicleListDC>("vehicleData")
                adapter.submitList(data?.vehicleArray ?: emptyList())
                binding.tvNoData.isVisible = data?.vehicleArray?.isEmpty() == true
                binding.rvVehicles.isVisible = data?.vehicleArray?.isNotEmpty() == true
            }
        }
    }

}