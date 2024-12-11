package com.ujeff_customer.view.fragment.notification

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.ujeff_customer.R
import com.ujeff_customer.customClasses.PaginationScrollListener
import com.ujeff_customer.databinding.FragmentNotificationBinding
import com.ujeff_customer.model.api.observeData
import com.ujeff_customer.view.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationFragment : BaseFragment<FragmentNotificationBinding>() {

    lateinit var binding: FragmentNotificationBinding
    private val adapter by lazy { NotificationAdapter() }
    private val viewModel by viewModels<NotificationsVM>()
    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_notification
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        binding.rvNotifications.adapter = adapter
        observeNotification()

        binding.ivBack.setOnClickListener {
            requireView().findNavController().popBackStack()
        }

        binding.rvNotifications.addOnScrollListener(object : PaginationScrollListener() {
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
        viewModel.currentPage = 1
        viewModel.isLastPage = false
        viewModel.getNotifications()
    }


    override fun onResume() {
        super.onResume()



    }


    /**
     * Observe Notification
     * */
    private fun observeNotification() =
        viewModel.notificationData.observeData(viewLifecycleOwner, onLoading = {
            if (viewModel.currentPage == 1) {
                showProgressDialog()
            }
        }, onSuccess = {
            hideProgressDialog()
            viewModel.isLastPage = this.isNullOrEmpty() == true
            viewModel.isLoading = false
            if (viewModel.currentPage == 1) {
                adapter.submitList(this ?: emptyList())
            } else {
                adapter.addMoreItems(this ?: emptyList())
            }
            binding.tvNoNotificationFound.isVisible = adapter.itemCount == 0
        }, onError = {
            hideProgressDialog()
            showToastLong(this)
        })


}