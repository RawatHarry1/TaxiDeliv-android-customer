package com.salonedriver.view.fragment.aboutApp

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.salonedriver.R
import com.salonedriver.databinding.AboutAppBinding
import com.salonedriver.model.api.observeData
import com.salonedriver.util.composeEmail
import com.salonedriver.util.showSessionExpire
import com.salonedriver.view.base.BaseFragment
import com.salonedriver.view.ui.home_drawer.HomeActivity
import com.salonedriver.viewmodel.AboutAppVM
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