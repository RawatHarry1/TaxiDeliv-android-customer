package com.venus_customer.view.fragment.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.venus_customer.R
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.FragmentAboutUsBinding
import com.venus_customer.model.api.observeData
import com.venus_customer.util.composeEmail
import com.venus_customer.util.safeCall
import com.venus_customer.view.base.BaseFragment
import com.venus_customer.viewmodel.base.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AboutUsFragment : BaseFragment<FragmentAboutUsBinding>() {
    lateinit var binding: FragmentAboutUsBinding
    private val viewModel by viewModels<ProfileViewModel>()

    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_about_us
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        viewModel.type = 2
        viewModel.aboutApp()
        observeData()
        clickHandler()

    }

    private fun clickHandler() {
        binding.ivBack.setOnSingleClickListener {
            findNavController().popBackStack()
        }

        binding.rlRateUs.setOnSingleClickListener {
            safeCall {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/")
                    )
                )
            }
        }

        binding.rlPrivacy.setOnSingleClickListener {
            viewModel.data?.privacyPolicy?.let {
                safeCall {
                    if (it.isNotEmpty())
                        CustomTabsIntent.Builder().build().launchUrl(requireContext(), Uri.parse(it))
                }
            }
        }

        binding.rlLikeUs.setOnSingleClickListener {
            viewModel.data?.facebookUrl?.let {
                safeCall {
                    if (it.isNotEmpty())
                        CustomTabsIntent.Builder().build().launchUrl(requireContext(), Uri.parse(it))
                }
            }
        }

        binding.rlWhoAreWe.setOnSingleClickListener {
            viewModel.data?.whoWeAre?.let {
                safeCall {
                    if (it.isNotEmpty())
                        CustomTabsIntent.Builder().build().launchUrl(requireContext(), Uri.parse(it))
                }
            }
        }

        binding.rlEmailSupport.setOnSingleClickListener {
            viewModel.data?.supportEmail?.let {
                safeCall {
                    if (it.isNotEmpty())
                        requireContext().composeEmail(it)
                }
            }
        }

        binding.rlLegal.setOnSingleClickListener {
            viewModel.data?.legalUrl?.let {
                safeCall {
                    if (it.isNotEmpty())
                        CustomTabsIntent.Builder().build().launchUrl(requireContext(), Uri.parse(it))
                }
            }
        }
    }


    private fun observeData() = viewModel.aboutApp.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        viewModel.data = this
    }, onError = {
        hideProgressDialog()
        showToastShort(this)
        requireView().findNavController().popBackStack()
    })
}