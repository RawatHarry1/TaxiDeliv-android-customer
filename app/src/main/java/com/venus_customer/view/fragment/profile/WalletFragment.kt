package com.venus_customer.view.fragment.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.venus_customer.R
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.FragmentWalletBinding
import com.venus_customer.dialogs.DialogUtils
import com.venus_customer.view.activity.CreateProfile
import com.venus_customer.view.adapter.WalletAdapter
import com.venus_customer.view.base.BaseFragment

class WalletFragment : BaseFragment<FragmentWalletBinding>() {

    lateinit var adapter : WalletAdapter
    lateinit var binding: FragmentWalletBinding

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

        adapter = WalletAdapter(requireContext())
        binding.rvWallet.adapter = adapter
    }

    override fun onResume() {
        super.onResume()

        setClicks()
    }

    private fun setClicks() {
        binding.ivBack.setOnSingleClickListener {
            findNavController().popBackStack()
        }

        binding.tvTopUp.setOnSingleClickListener {
            DialogUtils.getAddTopUpDialog(requireActivity(),onClickDialogLambda)
        }
    }

    private val onClickDialogLambda = { type : String ->

    }

}