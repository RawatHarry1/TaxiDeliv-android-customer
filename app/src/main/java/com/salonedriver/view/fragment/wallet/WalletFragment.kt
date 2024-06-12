package com.salonedriver.view.fragment.wallet

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.salonedriver.R
import com.salonedriver.databinding.FragmentWalletBinding
import com.salonedriver.dialogs.DialogUtils
import com.salonedriver.firebaseSetup.NotificationInterface
import com.salonedriver.model.api.observeData
import com.salonedriver.model.dataclassses.transactionHistory.TransactionHistoryDC
import com.salonedriver.model.dataclassses.userData.UserDataDC
import com.salonedriver.util.SharedPreferencesManager
import com.salonedriver.util.formatAmount
import com.salonedriver.view.adapter.WalletAdapter
import com.salonedriver.view.base.BaseFragment
import com.salonedriver.view.ui.home_drawer.HomeActivity
import com.salonedriver.view.ui.home_drawer.ui.home.CheckOnGoingBooking
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception

@AndroidEntryPoint
class WalletFragment : BaseFragment<FragmentWalletBinding>(), NotificationInterface {

    lateinit var binding: FragmentWalletBinding
    private val viewModel by viewModels<WalletVM>()
    private val adapter: WalletAdapter by lazy { WalletAdapter() }

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
        binding.rvWallet.adapter = adapter
        viewModel.getWalletTransactions()
        observeWalletData()
    }

    override fun onResume() {
        super.onResume()
        notificationInterface =  this
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

    private val onClickDialogLambda = { type: String ->

    }


    @SuppressLint("SetTextI18n")
    private fun observeWalletData() =
        viewModel.walletData.observeData(viewLifecycleOwner, onLoading = {
            showProgressDialog()
        }, onSuccess = {
            hideProgressDialog()
            setUpUi(this)
        }, onError = {
            hideProgressDialog()
        })


    @SuppressLint("SetTextI18n")
    private fun setUpUi(transactionHistoryDC: TransactionHistoryDC?) {
        try {
            binding.tvTotalBalance.text = "${SharedPreferencesManager.getCurrencySymbol()} ${
                transactionHistoryDC?.balance.orEmpty().ifEmpty { "0.0" }.formatAmount()
            }"
            if ((transactionHistoryDC?.balance.orEmpty().ifEmpty { "0.0" }.toDoubleOrNull() ?: 0.0) < (SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)?.login?.minDriverBalance.orEmpty().ifEmpty { "0.0" }.toDoubleOrNull() ?: 0.0)){
                binding.tvTotalBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_text_color))
                binding.tvLowWalletAmount.isVisible = true
            } else {
                binding.tvTotalBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                binding.tvLowWalletAmount.isVisible = false
            }
                binding.tvUserName.text = transactionHistoryDC?.userName.orEmpty()
            adapter.submitList(transactionHistoryDC?.transactions ?: emptyList())
            binding.rvWallet.isVisible = (transactionHistoryDC?.transactions ?: emptyList()).isNotEmpty()
            binding.tvNoData.isVisible = (transactionHistoryDC?.transactions ?: emptyList()).isEmpty()
        }catch (e:Exception){
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