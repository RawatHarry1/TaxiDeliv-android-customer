package com.venus_customer.view.fragment.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.venus_customer.R
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.FragmentWalletBinding
import com.venus_customer.dialogs.DialogUtils
import com.venus_customer.model.api.observeData
import com.venus_customer.util.SharedPreferencesManager
import com.venus_customer.view.activity.walk_though.PaymentActivity
import com.venus_customer.view.adapter.WalletAdapter
import com.venus_customer.view.base.BaseFragment
import com.venus_customer.viewmodel.base.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WalletFragment : BaseFragment<FragmentWalletBinding>() {

    lateinit var adapter: WalletAdapter
    lateinit var binding: FragmentWalletBinding
    private val viewModel by viewModels<ProfileViewModel>()
    private var cardId = ""
    private var last4 = ""
    private var amount = ""
    private var cur = ""
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
        cur = SharedPreferencesManager.getCurrencySymbol()
        adapter = WalletAdapter(requireContext())
        binding.rvWallet.adapter = adapter
        observeData()
        observeTopUpData()
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

    private val onClickDialogLambda = { amount: String ->
        this.amount = amount
        activityResultLauncherForPayment.launch(
            Intent(
                requireActivity(),
                PaymentActivity::class.java
            ).putExtra("whileRide", true)
        )
    }

    private fun observeData() = viewModel.transactionHistoryData.observeData(this, onLoading = {
        binding.shimmerLayout.shimmerLayout.isVisible = true
        binding.llWallet.isVisible = false
//        showProgressDialog()
    }, onSuccess = {
        binding.shimmerLayout.shimmerLayout.isVisible = false
        binding.llWallet.isVisible = true
        this?.let {
            adapter.setCurrency(it.currency ?: "")
            adapter.submitList(it.transactions.orEmpty())
            cur = it.currency ?: ""
            binding.tvTotalBalance.text = "${it.currency ?: ""} ${it.balance ?: 0.0}"
            binding.tvUserName.text = "${it.user_name}"
        }
        binding.tvNoTransactions.isVisible = adapter.itemCount == 0
//        hideProgressDialog()
    }, onError = {
        binding.shimmerLayout.shimmerLayout.isVisible = false
        binding.llWallet.isVisible = true
//        hideProgressDialog()
        showToastShort(this)
    })

    private fun observeTopUpData() =
        viewModel.addMoneyData.observeData(
            requireActivity(),
            onLoading = {
                showProgressDialog()
            },
            onSuccess = {
                hideProgressDialog()
                viewModel.getTransactions()
            },
            onError = {
                hideProgressDialog()
            })

    private val activityResultLauncherForPayment = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                cardId = result.data?.getStringExtra("cardId") ?: ""
                last4 = result.data?.getStringExtra("last4") ?: ""
                DialogUtils.getNegativeDialog(
                    requireActivity(), "Use Card",
                    "Are you sure you want to use *** $last4 card for top up?"
                ) {
                    viewModel.addMoney(cardId = cardId, currency = cur, amount = amount)
                }
            } catch (_: Exception) {
            }
        }
    }
}