package com.salonedriver.view.fragment

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.salonedriver.R
import com.salonedriver.databinding.FragmentVehicleListingBinding
import com.salonedriver.model.api.observeData
import com.salonedriver.model.dataclassses.VehicleListDC
import com.salonedriver.model.dataclassses.userData.UserDataDC
import com.salonedriver.util.SharedPreferencesManager
import com.salonedriver.view.adapter.VehicleListAdapter
import com.salonedriver.view.base.BaseFragment
import com.salonedriver.view.ui.home_drawer.HomeActivity
import com.salonedriver.viewmodel.UserAccountVM
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