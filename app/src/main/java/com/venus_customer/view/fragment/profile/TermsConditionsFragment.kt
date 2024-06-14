package com.venus_customer.view.fragment.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.venus_customer.R
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.FragmentTermsConditionsBinding
import com.venus_customer.util.constants.AppConstants
import com.venus_customer.view.base.BaseFragment


class TermsConditionsFragment : BaseFragment<FragmentTermsConditionsBinding>() {

    lateinit var binding: FragmentTermsConditionsBinding
    private val isTerms : Boolean by lazy { arguments?.getBoolean(AppConstants.IS_TERMS,false) ?: false}

    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_terms_conditions
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()

        setUi()
    }

    private fun setUi() {
        if(isTerms)
            binding.tvTitle.text = getString(R.string.terms_conditions)
        else
            binding.tvTitle.text =getString(R.string.privacy_policy)

    }

    override fun onResume() {
        super.onResume()

        setClicks()
    }

    private fun setClicks() {
        binding.ivBack.setOnSingleClickListener {
            findNavController().popBackStack()
        }
    }
}