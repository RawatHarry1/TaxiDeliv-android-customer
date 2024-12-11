package com.ujeff_driver.view.fragment.notifications

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.ujeff_driver.R
import com.ujeff_driver.customClasses.PaginationScrollListener
import com.ujeff_driver.databinding.FragmentNotificationsListingBinding
import com.ujeff_driver.model.api.observeData
import com.ujeff_driver.view.adapter.NotificationsAdapter
import com.ujeff_driver.view.base.BaseFragment
import com.ujeff_driver.view.ui.home_drawer.HomeActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class NotificationsListingFragment : BaseFragment<FragmentNotificationsListingBinding>() {

    lateinit var binding: FragmentNotificationsListingBinding
    private val adapter by lazy { NotificationsAdapter() }
    private val viewModel by viewModels<NotificationsVM>()
    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_notifications_listing
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        binding.rvNotifications.adapter = adapter
        observeNotification()
    }

    override fun onResume() {
        super.onResume()

        binding.ivMenuBurg.setOnClickListener {
            (activity as HomeActivity).openDrawer()
        }


        binding.rvNotifications.addOnScrollListener(object : PaginationScrollListener(){
            override fun loadMoreItems() {
                if (!viewModel.isLoading) {
                    viewModel.isLoading = true
                    viewModel.currentPage = viewModel.currentPage.plus(1)
                    viewModel.getNotifications()
                }
            }

            override val isLastPage: Boolean
                get() = viewModel.isLastPage

            override val isLoading: Boolean
                get() = viewModel.isLoading

        })

    }


    private fun observeNotification() = viewModel.notificationData.observeData(viewLifecycleOwner, onLoading = {
        if (viewModel.currentPage == 1){
            showProgressDialog()
        }
    }, onSuccess = {
        hideProgressDialog()
        viewModel.isLastPage = this.isNullOrEmpty() == true
        if (viewModel.currentPage == 1){
            adapter.submitList(this ?: emptyList())
        } else {
            adapter.addMoreItems(this ?: emptyList())
        }
    }, onError = {
        hideProgressDialog()
        showToastLong(this)
    })

}