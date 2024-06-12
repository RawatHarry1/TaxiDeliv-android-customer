package com.salonedriver.view.fragment.notifications

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.salonedriver.R
import com.salonedriver.customClasses.PaginationScrollListener
import com.salonedriver.databinding.FragmentNotificationsListingBinding
import com.salonedriver.model.api.observeData
import com.salonedriver.view.adapter.NotificationsAdapter
import com.salonedriver.view.base.BaseFragment
import com.salonedriver.view.ui.home_drawer.HomeActivity
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