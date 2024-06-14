package com.venus_customer.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.venus_customer.R
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.ActivitySignUpInBinding
import com.venus_customer.databinding.FragmentFAQBinding
import com.venus_customer.view.adapter.FAQAdapter
import com.venus_customer.view.base.BaseFragment


class FAQFragment : BaseFragment<FragmentFAQBinding>() {

    lateinit var binding: FragmentFAQBinding
    lateinit var faqAdapter: FAQAdapter

    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
                return R.layout.fragment_f_a_q
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()

        setUI()

    }

    private fun setUI() {
        faqAdapter = FAQAdapter(requireContext())
        binding.rvFaqs.adapter = faqAdapter
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