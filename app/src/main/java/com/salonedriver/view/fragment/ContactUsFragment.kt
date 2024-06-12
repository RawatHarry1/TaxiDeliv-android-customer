package com.salonedriver.view.fragment

import android.os.Bundle
import android.view.View
import com.salonedriver.R
import com.salonedriver.databinding.FragmentContactUsBinding
import com.salonedriver.view.base.BaseFragment
import com.salonedriver.view.ui.home_drawer.HomeActivity


class ContactUsFragment : BaseFragment<FragmentContactUsBinding>() {

    lateinit var binding: FragmentContactUsBinding
    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_contact_us
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

        binding.ivMenuBurg.setOnClickListener {
            (activity as HomeActivity).openDrawer()
        }

    }

}