package com.marsapp_driver.view.fragment

import android.os.Bundle
import android.view.View
import com.marsapp_driver.R
import com.marsapp_driver.databinding.FragmentContactUsBinding
import com.marsapp_driver.view.base.BaseFragment
import com.marsapp_driver.view.ui.home_drawer.HomeActivity


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