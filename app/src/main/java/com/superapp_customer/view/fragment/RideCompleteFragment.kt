package com.superapp_customer.view.fragment

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.superapp_customer.R
import com.superapp_customer.customClasses.singleClick.setOnSingleClickListener
import com.superapp_customer.databinding.FragmentRideCompleteBinding
import com.superapp_customer.view.base.BaseFragment


class RideCompleteFragment : BaseFragment<FragmentRideCompleteBinding>() {
    lateinit var binding: FragmentRideCompleteBinding
    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_ride_complete
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
    }

    override fun onResume() {
        super.onResume()

        setClicks()
    }

    private fun setClicks() {
        binding.ivClose.setOnSingleClickListener {
            findNavController().popBackStack()
        }

        binding.tvRateDriver.setOnSingleClickListener {
            findNavController().navigate(R.id.navigation_rate_driver)
        }
    }
}