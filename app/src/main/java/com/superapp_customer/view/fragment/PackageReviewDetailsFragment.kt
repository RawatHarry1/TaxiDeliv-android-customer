package com.superapp_customer.view.fragment

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.superapp_customer.R
import com.superapp_customer.customClasses.singleClick.setOnSingleClickListener
import com.superapp_customer.databinding.FragmentPackageReviewBinding
import com.superapp_customer.databinding.ItemPackageListBinding
import com.superapp_customer.model.dataClass.AddPackage
import com.superapp_customer.util.GenericAdapter
import com.superapp_customer.util.SharedPreferencesManager
import com.superapp_customer.view.base.BaseFragment
import com.superapp_customer.viewmodel.rideVM.RideVM

class PackageReviewDetailsFragment : BaseFragment<FragmentPackageReviewBinding>() {
    private lateinit var binding: FragmentPackageReviewBinding
    private lateinit var packagesAdapter: GenericAdapter<AddPackage>
    private val addedPackagesArrayList = ArrayList<AddPackage>()
    private val rideVM by activityViewModels<RideVM>()
    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_package_review
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        binding.ivBack.setOnSingleClickListener { findNavController().popBackStack() }
        binding.tvPickUpAddress.text = rideVM.createRideData.pickUpLocation?.address
        binding.tvDropOffAddress.text = rideVM.createRideData.dropLocation?.address
        setAdapter()
        binding.rlDeliveryDetails.setOnSingleClickListener {
            if (binding.clDeliveryDetails.isVisible) {
                binding.ivArrow.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_drop_down_theme
                    )
                )
                binding.clDeliveryDetails.isVisible = false
            } else {
                binding.ivArrow.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_arrow_up_theme
                    )
                )
                binding.clDeliveryDetails.isVisible = true
            }
        }

        binding.rlPackage.setOnSingleClickListener {
            if (binding.rvAddedPackages.isVisible) {
                binding.ivArrowPackage.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_drop_down_theme
                    )
                )
                binding.rvAddedPackages.isVisible = false
            } else {
                binding.ivArrowPackage.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_arrow_up_theme
                    )
                )
                binding.rvAddedPackages.isVisible = true
            }
        }

        binding.rlVehicle.setOnSingleClickListener {
            if (binding.cvVehicleDetails.isVisible) {
                binding.ivArrowVehicle.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_drop_down_theme
                    )
                )
                binding.cvVehicleDetails.isVisible = false
            } else {
                binding.ivArrowVehicle.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_arrow_up_theme
                    )
                )
                binding.cvVehicleDetails.isVisible = true
            }
        }
        binding.tvConfirm.setOnSingleClickListener {
            findNavController().popBackStack(R.id.addPackageFragment, true)
            findNavController().popBackStack(R.id.packageReviewDetailsFragment, true)
            rideVM.delivery(true)
        }
    }

    private fun setAdapter() {
        packagesAdapter = object : GenericAdapter<AddPackage>(R.layout.item_package_list) {
            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val binding = ItemPackageListBinding.bind(holder.itemView)
                val data = getItem(position)
                binding.tvPackageSize.text = data.packageSize
                binding.tvPackageType.text = data.packageType
                binding.tvPackageQuantity.text = data.quantity
                binding.ivEdit.isVisible = false
                binding.ivDelete.isVisible = false
            }
        }
        addedPackagesArrayList.clear()
        SharedPreferencesManager.getAddPackageList(
            SharedPreferencesManager.Keys.ADD_PACKAGE
        )?.let { addedPackagesArrayList.addAll(it) }
        packagesAdapter.submitList(
            addedPackagesArrayList
        )
        binding.rvAddedPackages.adapter = packagesAdapter
    }
}