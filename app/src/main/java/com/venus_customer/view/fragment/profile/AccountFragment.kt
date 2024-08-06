package com.venus_customer.view.fragment.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.venus_customer.R
import com.venus_customer.VenusApp
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.FragmentAccountBinding
import com.venus_customer.dialogs.DialogUtils
import com.venus_customer.model.api.observeData
import com.venus_customer.model.dataClass.base.ClientConfig
import com.venus_customer.model.dataClass.userData.UserDataDC
import com.venus_customer.util.SharedPreferencesManager
import com.venus_customer.util.formatString
import com.venus_customer.util.safeCall
import com.venus_customer.view.activity.CreateProfile
import com.venus_customer.view.activity.sign_in.SignIn
import com.venus_customer.view.activity.walk_though.PaymentActivity
import com.venus_customer.view.base.BaseFragment
import com.venus_customer.viewmodel.HomeVM
import com.venus_customer.viewmodel.base.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountFragment : BaseFragment<FragmentAccountBinding>() {

    lateinit var binding: FragmentAccountBinding
    private val viewModel by viewModels<ProfileViewModel>()
    private val homeViewModel by viewModels<HomeVM>()

    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_account
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        observeData()
        observeDeleteData()
        observeProfileData()
        setData(view)
        homeViewModel.loginViaToken()
    }

    private fun setData(view: View) {
        SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
            ?.let {
                binding.tvUserName.text = it.login?.userName.orEmpty()
                binding.tvUserRating.text =
                    it.login?.userRating.orEmpty().ifEmpty { "0.0" }.formatString(1)
                Glide.with(view.context).load(it.login?.userImage.orEmpty())
                    .error(R.drawable.circleimage).into(binding.ivProfileImage)
            }

        binding.ivProfileImage.setOnSingleClickListener {
            startActivity(Intent(requireContext(), CreateProfile::class.java).apply {
                putExtra("isEditProfile", true)
            })
        }

        binding.tvUserName.setOnSingleClickListener {
            binding.ivProfileImage.performClick()
        }
        binding.llCards.setOnSingleClickListener {
//            val dialog =AddAndShowCardDialogFragment()
//            dialog.onCardSelected = {
//
//            }
//            dialog.show(parentFragmentManager, "AddCardDialog")
            startActivity(Intent(requireActivity(), PaymentActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        setClicks()
    }


    private fun setClicks() {
        binding.llFaq.setOnSingleClickListener {
            findNavController().navigate(R.id.navigation_faq)
        }


        binding.llChangePassword.setOnSingleClickListener {

            findNavController().navigate(R.id.navigation_change_password)
        }

        binding.llTerms.setOnSingleClickListener {
            val configData =
                SharedPreferencesManager.getModel<ClientConfig>(SharedPreferencesManager.Keys.CLIENT_CONFIG)
            configData?.termsOfUseUrl?.let {
                safeCall {
                    if (it.isNotEmpty())
                        CustomTabsIntent.Builder().build()
                            .launchUrl(requireContext(), Uri.parse(it))
                }
            }
        }

        binding.llAbout.setOnSingleClickListener {
            findNavController().navigate(R.id.navigation_about_us)
        }

        binding.llOffers.setOnSingleClickListener {
            findNavController().navigate(R.id.navigation_offers)
        }

        binding.llWallet.setOnSingleClickListener {
            findNavController().navigate(R.id.navigation_wallet)
        }
        binding.llReferral.setOnSingleClickListener {
            findNavController().navigate(R.id.navigate_referral)
        }

        binding.llLogout.setOnSingleClickListener {
            DialogUtils.getNegativeDialog(
                requireActivity(),
                "Yes",
                getString(R.string.are_you_sure_you_want_to_log_out),
                ::onDialogClick
            )
        }

        binding.llDelete.setOnSingleClickListener {
            DialogUtils.getNegativeDialog(
                requireActivity(),
                "Yes",
                getString(R.string.are_you_sure_you_want_to_delete_account),
                ::onDeleteAccountClick
            )
        }
    }


    private fun onDialogClick(position: Int) {
        viewModel.logout()
    }

    private fun onDeleteAccountClick(position: Int) {
        viewModel.deleteAccount()
    }


    private fun observeData() = viewModel.logout.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        VenusApp.offerApplied = 0
        VenusApp.offerTitle = ""
        SharedPreferencesManager.clearKeyData(SharedPreferencesManager.Keys.USER_DATA)
        startActivity(
            Intent(
                requireContext(),
                SignIn::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        requireActivity().finishAffinity()
    }, onError = {
        hideProgressDialog()
        VenusApp.offerApplied = 0
        VenusApp.offerTitle = ""
        SharedPreferencesManager.clearKeyData(SharedPreferencesManager.Keys.USER_DATA)
        startActivity(
            Intent(
                requireContext(),
                SignIn::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        requireActivity().finishAffinity()
    })

    private fun observeDeleteData() = viewModel.deleteAccount.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        VenusApp.offerApplied = 0
        VenusApp.offerTitle = ""
        SharedPreferencesManager.clearKeyData(SharedPreferencesManager.Keys.USER_DATA)
        startActivity(
            Intent(
                requireContext(),
                SignIn::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        requireActivity().finishAffinity()
    }, onError = {
        hideProgressDialog()
        VenusApp.offerApplied = 0
        VenusApp.offerTitle = ""
        SharedPreferencesManager.clearKeyData(SharedPreferencesManager.Keys.USER_DATA)
        startActivity(
            Intent(
                requireContext(),
                SignIn::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        requireActivity().finishAffinity()
    })

    private fun observeProfileData() = homeViewModel.loginViaToken.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        SharedPreferencesManager.putModel(SharedPreferencesManager.Keys.USER_DATA, this)
        if (activity != null)
            binding.tvUserRating.text =
                this?.login?.userRating.orEmpty().ifEmpty { "0.0" }.formatString(1)
    }, onError = {
        hideProgressDialog()
        showToastShort(this)
    })


    override fun onDestroyView() {
        super.onDestroyView()
        hideProgressDialog()
    }

}