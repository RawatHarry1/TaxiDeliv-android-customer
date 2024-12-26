package com.vyba_dri.view.fragment.wallet

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.vyba_dri.R
import com.vyba_dri.databinding.FragmentWalletBinding
import com.vyba_dri.dialogs.CustomProgressDialog
import com.vyba_dri.dialogs.DialogUtils
import com.vyba_dri.firebaseSetup.NotificationInterface
import com.vyba_dri.model.api.observeData
import com.vyba_dri.model.dataclassses.transactionHistory.TransactionHistoryDC
import com.vyba_dri.model.dataclassses.userData.UserDataDC
import com.vyba_dri.util.SharedPreferencesManager
import com.vyba_dri.util.formatAmount
import com.vyba_dri.view.adapter.WalletAdapter
import com.vyba_dri.view.base.BaseFragment
import com.vyba_dri.view.ui.PaymentActivity
import com.vyba_dri.view.ui.home_drawer.HomeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WalletFragment : BaseFragment<FragmentWalletBinding>(), NotificationInterface {

    lateinit var binding: FragmentWalletBinding
    private val viewModel by viewModels<WalletVM>()
    private val adapter: WalletAdapter by lazy { WalletAdapter() }
    private var cardId = ""
    private var last4 = ""
    private var amount = ""
    private var cur = ""
    private val progressBar by lazy { CustomProgressDialog() }

    companion object {
        var notificationInterface: NotificationInterface? = null
    }

    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_wallet
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        cur = SharedPreferencesManager.getCurrencySymbol()
        binding.rvWallet.adapter = adapter
        viewModel.getWalletTransactions()
        observeWalletData()
        observeTopUpData()
    }

    override fun onResume() {
        super.onResume()
        notificationInterface = this
        setClicks()
    }

    private fun setClicks() {
        binding.ivBack.setOnClickListener {
            (activity as HomeActivity).openDrawer()
        }

        binding.tvTopUp.setOnClickListener {
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

    private fun observeTopUpData() =
        viewModel.addMoneyData.observeData(
            requireActivity(),
            onLoading = {
                progressBar.show(requireActivity())
            },
            onSuccess = {
                progressBar.dismiss()
                viewModel.getWalletTransactions()
            },
            onError = {
                progressBar.dismiss()
                showToastLong(this)
            })

    @SuppressLint("SetTextI18n")
    private fun observeWalletData() =
        viewModel.walletData.observeData(viewLifecycleOwner, onLoading = {
            binding.shimmerLayout.shimmerLayout.isVisible = true
            binding.llWalletLayout.isVisible = false
        }, onSuccess = {
            binding.shimmerLayout.shimmerLayout.isVisible = false
            binding.llWalletLayout.isVisible = true
            setUpUi(this)
        }, onError = {
            binding.shimmerLayout.shimmerLayout.isVisible = false
            binding.llWalletLayout.isVisible = true
        })


    @SuppressLint("SetTextI18n")
    private fun setUpUi(transactionHistoryDC: TransactionHistoryDC?) {
        try {
            binding.tvTotalBalance.text = "${SharedPreferencesManager.getCurrencySymbol()} ${
                transactionHistoryDC?.balance.orEmpty().ifEmpty { "0.0" }.formatAmount()
            }"
            if ((transactionHistoryDC?.balance.orEmpty().ifEmpty { "0.0" }.toDoubleOrNull()
                    ?: 0.0) < (SharedPreferencesManager.getModel<UserDataDC>(
                    SharedPreferencesManager.Keys.USER_DATA
                )?.login?.minDriverBalance.orEmpty().ifEmpty { "0.0" }.toDoubleOrNull() ?: 0.0)
            ) {
                binding.tvTotalBalance.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.red_text_color
                    )
                )
                binding.tvLowWalletAmount.isVisible = true
            } else {
                binding.tvTotalBalance.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                binding.tvLowWalletAmount.isVisible = false
            }
            binding.tvUserName.text = transactionHistoryDC?.userName.orEmpty()
            adapter.submitList(transactionHistoryDC?.transactions ?: emptyList())
            binding.rvWallet.isVisible =
                (transactionHistoryDC?.transactions ?: emptyList()).isNotEmpty()
            binding.tvNoData.isVisible =
                (transactionHistoryDC?.transactions ?: emptyList()).isEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        notificationInterface = null
    }


    override fun walletUpdate() {
        super.walletUpdate()
        requireActivity().runOnUiThread {
            viewModel.getWalletTransactions()
        }
    }

}