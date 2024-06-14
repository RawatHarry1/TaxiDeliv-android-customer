package com.venus_customer.view.fragment.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.venus_customer.R
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.FragmentChangePasswordBinding
import com.venus_customer.view.base.BaseFragment


class ChangePasswordFragment : BaseFragment<FragmentChangePasswordBinding>() {

    lateinit var binding: FragmentChangePasswordBinding
    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_change_password
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()

    }

    override fun onResume() {
        super.onResume()

        setClicks()
    }

    private fun setClicks() {
        binding.btnSave.setOnSingleClickListener {
            findNavController().popBackStack()
        }

        binding.ivBack.setOnSingleClickListener {
            findNavController().popBackStack()
        }

    }
}