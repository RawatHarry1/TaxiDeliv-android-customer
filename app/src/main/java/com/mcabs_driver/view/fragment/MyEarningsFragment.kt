package com.mcabs_driver.view.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.mcabs_driver.R
import com.mcabs_driver.databinding.FragmentMyEarningsBinding
import com.mcabs_driver.model.api.observeData
import com.mcabs_driver.util.SharedPreferencesManager
import com.mcabs_driver.util.formatAmount
import com.mcabs_driver.util.gone
import com.mcabs_driver.util.visible
import com.mcabs_driver.view.adapter.BookingsAdapter
import com.mcabs_driver.view.adapter.EarningsAdapter
import com.mcabs_driver.view.base.BaseFragment
import com.mcabs_driver.view.ui.home.BookingDetailsActivity
import com.mcabs_driver.view.ui.home_drawer.HomeActivity
import com.mcabs_driver.viewmodel.BookingVM
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MyEarningsFragment : BaseFragment<FragmentMyEarningsBinding>() {

    lateinit var binding: FragmentMyEarningsBinding
    private val bookingAdapter: BookingsAdapter by lazy { BookingsAdapter(::bookingAdapterClick) }
    private val earningsAdapter: EarningsAdapter by lazy { EarningsAdapter() }
    private val viewModel by viewModels<BookingVM>()

    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_my_earnings
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        setClicks()
        getArgumentData()
        setBookingsAdapter()
        setEarningsAdapter()
        observeBookingList()
        observeEarningData()
    }

    private fun getArgumentData() {
        if (arguments != null) {
            val isBookingTab = requireArguments().getBoolean("isBookingTab", false)
            if (isBookingTab) binding.tvBookings.performClick() else binding.tvEarnings.performClick()
        }
    }

    private fun setEarningsAdapter() {
        binding.rvEarnings.adapter = earningsAdapter
    }

    private fun setBookingsAdapter() {
        binding.rvBookings.adapter = bookingAdapter
    }

    private fun setClicks() {

        binding.tvEarnings.setOnClickListener {
            setEarningsUI()
        }

        binding.tvBookings.setOnClickListener {
            setBookingsUI()
        }

        binding.ivMenuBurg.setOnClickListener {
            (activity as HomeActivity).openDrawer()
        }
    }

    private fun setBookingsUI() {
        if (bookingAdapter.getItems().isEmpty()) viewModel.getBookingHistory()
        binding.tvEarnings.setBackgroundResource(R.drawable.bg_left_rounded_24dp_white)
        binding.tvBookings.setBackgroundResource(R.drawable.bg_right_rounded_24dp_theme)
        binding.tvBookings.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.tvEarnings.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        binding.clEarnings.gone()
        binding.clBookings.visible()
    }

    private fun setEarningsUI() {
        viewModel.getEarningData()
        binding.tvEarnings.setBackgroundResource(R.drawable.bg_left_rounded_24dp_theme)
        binding.tvBookings.setBackgroundResource(R.drawable.bg_right_rounded_24dp_white)
        binding.tvEarnings.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.tvBookings.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        binding.clEarnings.visible()
        binding.clBookings.gone()
    }


    private fun bookingAdapterClick(string: String) {
        startActivity(Intent(requireContext(), BookingDetailsActivity::class.java).also {
            it.putExtra("bookingId", string)
        })
    }

    private fun observeBookingList() = viewModel.bookingHistoryData.observeData(viewLifecycleOwner, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        bookingAdapter.submitList(this ?: emptyList())
        binding.rvBookings.isVisible = this?.isNotEmpty() == true
        binding.tvBookingNoData.isVisible = this?.isEmpty() == true
    }, onError = {
        hideProgressDialog()
        showToastShort(this)
    })


    @SuppressLint("SetTextI18n")
    private fun observeEarningData() = viewModel.earningData.observeData(viewLifecycleOwner,
        onLoading = {
                    showProgressDialog()
        }, onSuccess = {
            hideProgressDialog()
            binding.tvBalance.text = "${SharedPreferencesManager.getCurrencySymbol()} ${this?.totalEarnings.orEmpty().ifEmpty { "0.0" }.formatAmount()}"
            earningsAdapter.submitList(this?.rides ?: emptyList())
            binding.rvEarnings.isVisible = this?.rides?.isNotEmpty() == true
            binding.tvEarningNoData.isVisible = this?.rides?.isEmpty() == true
        }, onError = {
            hideProgressDialog()
            showToastLong(this)
        })


}