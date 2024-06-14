package com.venus_customer.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.venus_customer.R
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.FragmentRideCompleteBinding
import com.venus_customer.view.base.BaseFragment


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