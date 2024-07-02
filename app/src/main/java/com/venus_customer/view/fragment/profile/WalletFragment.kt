package com.venus_customer.view.fragment.profile

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.venus_customer.R
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.FragmentWalletBinding
import com.venus_customer.dialogs.DialogUtils
import com.venus_customer.model.api.observeData
import com.venus_customer.view.adapter.WalletAdapter
import com.venus_customer.view.base.BaseFragment
import com.venus_customer.viewmodel.base.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WalletFragment : BaseFragment<FragmentWalletBinding>() {

    lateinit var adapter: WalletAdapter
    lateinit var binding: FragmentWalletBinding
    private val viewModel by viewModels<ProfileViewModel>()
    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_wallet
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        setClicks()
        adapter = WalletAdapter(requireContext())
        binding.rvWallet.adapter = adapter
        observeData()
        viewModel.getTransactions()
    }

    override fun onResume() {
        super.onResume()

    }

    private fun setClicks() {
        binding.ivBack.setOnSingleClickListener {
            findNavController().popBackStack()
        }

        binding.tvTopUp.setOnSingleClickListener {
            DialogUtils.getAddTopUpDialog(requireActivity(), onClickDialogLambda)
        }
    }

    private val onClickDialogLambda = { type: String ->

    }

    private fun observeData() = viewModel.transactionHistoryData.observeData(this, onLoading = {
        binding.shimmerLayout.shimmerLayout.isVisible = true
//        showProgressDialog()
    }, onSuccess = {
        binding.shimmerLayout.shimmerLayout.isVisible = false
        binding.llWallet.isVisible = true
        this?.let {
            adapter.setCurrency(it.currency?: "")
            adapter.submitList(it.transactions.orEmpty())
            binding.tvTotalBalance.text = "${it.currency?: ""} ${it.balance?: 0.0}"
            binding.tvUserName.text = "${it.user_name}"
        }
        binding.tvNoTransactions.isVisible = adapter.itemCount == 0
//        hideProgressDialog()
    }, onError = {
        binding.shimmerLayout.shimmerLayout.isVisible = false
//        hideProgressDialog()
        showToastShort(this)
    })
}