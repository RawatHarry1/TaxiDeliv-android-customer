package com.salonedriver.view.fragment

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.salonedriver.R
import com.salonedriver.databinding.FragmentSupportBinding
import com.salonedriver.view.base.BaseFragment
import com.salonedriver.view.ui.home_drawer.HomeActivity

class SupportFragment : BaseFragment<FragmentSupportBinding>() {

    lateinit var binding: FragmentSupportBinding
    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_support
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

        binding.tvContactUs.setOnClickListener {
            findNavController().navigate(R.id.nav_contact_us)
        }
    }

}