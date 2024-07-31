package com.venus_customer.view.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.venus_customer.R
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.FragmentFAQBinding
import com.venus_customer.model.api.observeData
import com.venus_customer.model.dataClass.FaqX
import com.venus_customer.view.adapter.FAQAdapter
import com.venus_customer.view.base.BaseFragment
import com.venus_customer.viewmodel.base.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FAQFragment : BaseFragment<FragmentFAQBinding>() {

    lateinit var binding: FragmentFAQBinding
    lateinit var faqAdapter: FAQAdapter
    private val faqArrayList = ArrayList<FaqX>()
    private val viewModel by viewModels<ProfileViewModel>()
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
        observeData()
        viewModel.type = 1
        viewModel.aboutApp()
    }

    private fun setUI() {
        faqAdapter = FAQAdapter(requireActivity(), faqArrayList)
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

    private fun observeData() = viewModel.aboutApp.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        faqArrayList.clear()
        this?.faq?.let { faqArrayList.addAll(it) }
        faqAdapter.notifyDataSetChanged()
    }, onError = {
        hideProgressDialog()
        showToastShort(this)
        requireView().findNavController().popBackStack()
    })

}