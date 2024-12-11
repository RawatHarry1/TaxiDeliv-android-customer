package com.ujeff_driver.view.fragment.aboutApp

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.viewModels
import com.ujeff_driver.R
import com.ujeff_driver.databinding.AboutAppBinding
import com.ujeff_driver.model.api.observeData
import com.ujeff_driver.util.composeEmail
import com.ujeff_driver.view.base.BaseFragment
import com.ujeff_driver.view.ui.home_drawer.HomeActivity
import com.ujeff_driver.viewmodel.AboutAppVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutApp: BaseFragment<AboutAppBinding>() {

    private lateinit var binding: AboutAppBinding
    private val viewModel by viewModels<AboutAppVM>()

    override fun getLayoutId(): Int {
        return R.layout.about_app
    }

    override fun initialiseFragmentBaseViewModel() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        binding.vm = viewModel
        if (viewModel.data == null) viewModel.getAboutUsData()

        binding.ivMenuBurg.setOnClickListener {
            (activity as HomeActivity).openDrawer()
        }

        binding.rlPrivacy.setOnClickListener {
            viewModel.data?.privacyPolicy?.let {
                safeCall {
                    if (it.isNotEmpty())
                        CustomTabsIntent.Builder().build().launchUrl(requireContext(), Uri.parse(it))
                }
            }
        }

        binding.rlLikeUs.setOnClickListener {
            viewModel.data?.facebookUrl?.let {
                safeCall {
                    if (it.isNotEmpty())
                        CustomTabsIntent.Builder().build().launchUrl(requireContext(), Uri.parse(it))
                }
            }
        }

        binding.rlWhoAreWe.setOnClickListener {
            viewModel.data?.whoWeAre?.let {
                safeCall {
                    if (it.isNotEmpty())
                        CustomTabsIntent.Builder().build().launchUrl(requireContext(), Uri.parse(it))
                }
            }
        }

        binding.rlEmailSupport.setOnClickListener {
            viewModel.data?.supportEmail?.let {
                safeCall {
                    if (it.isNotEmpty())
                        requireContext().composeEmail(it)
                }
            }
        }

        binding.rlLegal.setOnClickListener {
            viewModel.data?.legalUrl?.let {
                safeCall {
                    if (it.isNotEmpty())
                        CustomTabsIntent.Builder().build().launchUrl(requireContext(), Uri.parse(it))
                }
            }
        }

        observeData()
    }


    private fun observeData() = viewModel.aboutUsLinkData.observeData(viewLifecycleOwner, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        viewModel.data = this
    }, onError = {
        hideProgressDialog()
        activity?.onBackPressed()
        showToastLong(this)
    })



    private fun safeCall(block: () -> Unit){
        try {
            block()
        }catch (e:Exception){
            showToastShort(getString(R.string.invalid_url))
            e.printStackTrace()
        }
    }

}