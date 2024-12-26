package com.vyba_dri.view.fragment

import android.os.Bundle
import android.view.View
import com.vyba_dri.R
import com.vyba_dri.databinding.FragmentContactUsBinding
import com.vyba_dri.view.base.BaseFragment
import com.vyba_dri.view.ui.home_drawer.HomeActivity


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