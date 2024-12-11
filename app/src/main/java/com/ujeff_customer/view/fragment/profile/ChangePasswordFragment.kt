package com.ujeff_customer.view.fragment.profile

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.ujeff_customer.R
import com.ujeff_customer.customClasses.singleClick.setOnSingleClickListener
import com.ujeff_customer.databinding.FragmentChangePasswordBinding
import com.ujeff_customer.view.base.BaseFragment


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