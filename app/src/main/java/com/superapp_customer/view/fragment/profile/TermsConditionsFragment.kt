package com.superapp_customer.view.fragment.profile

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.superapp_customer.R
import com.superapp_customer.customClasses.singleClick.setOnSingleClickListener
import com.superapp_customer.databinding.FragmentTermsConditionsBinding
import com.superapp_customer.util.constants.AppConstants
import com.superapp_customer.view.base.BaseFragment


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