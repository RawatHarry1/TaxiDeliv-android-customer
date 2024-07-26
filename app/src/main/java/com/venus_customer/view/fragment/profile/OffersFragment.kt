package com.venus_customer.view.fragment.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.venus_customer.R
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.FragmentOffersBinding
import com.venus_customer.model.api.observeData
import com.venus_customer.view.adapter.OffersAdapter
import com.venus_customer.view.base.BaseFragment
import com.venus_customer.viewmodel.base.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OffersFragment : BaseFragment<FragmentOffersBinding>() {
    lateinit var binding: FragmentOffersBinding
    lateinit var adapter : OffersAdapter
    private val viewModel by viewModels<ProfileViewModel>()
    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_offers
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        adapter = OffersAdapter(requireActivity())
        binding.rvPromo.adapter = adapter
        observeData()
        viewModel.getCouponAndPromotions()

    }

    override fun onResume() {
        super.onResume()
        setClicks()
    }

    private fun setClicks() {
        binding.ivBack.setOnSingleClickListener {
            findNavController().popBackStack()
        }
    }
    private fun observeData() = viewModel.promoData.observeData(this, onLoading = {
        binding.shimmerLayout.shimmerLayout.isVisible = true
//        showProgressDialog()
    }, onSuccess = {
        binding.shimmerLayout.shimmerLayout.isVisible = false
        binding.rvPromo.isVisible = true
        this?.let {
          adapter.submitList(it.promotions)
        }
        binding.tvNoData.isVisible = adapter.itemCount == 0
//        hideProgressDialog()
    }, onError = {
        binding.shimmerLayout.shimmerLayout.isVisible = false
//        hideProgressDialog()
        showToastShort(this)
    })
}