package com.vyba_dri.view.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.vyba_dri.R
import com.vyba_dri.databinding.FragmentAccountBinding
import com.vyba_dri.model.api.observeData
import com.vyba_dri.model.dataclassses.userData.Login
import com.vyba_dri.model.dataclassses.userData.UserDataDC
import com.vyba_dri.util.SharedPreferencesManager
import com.vyba_dri.view.base.BaseFragment
import com.vyba_dri.view.ui.CreateProfile
import com.vyba_dri.view.ui.home_drawer.HomeActivity
import com.vyba_dri.viewmodel.UserAccountVM
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AccountFragment : BaseFragment<FragmentAccountBinding>() {

    lateinit var binding: FragmentAccountBinding
    private val viewModel by viewModels<UserAccountVM>()

    override fun initialiseFragmentBaseViewModel() {

    }


    override fun getLayoutId(): Int {
        return R.layout.fragment_account
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        setClicks()
        observeGetProfile()
    }


    override fun onResume() {
        super.onResume()
        viewModel.getProfile()
    }

    private fun setClicks() {
        binding.ivMenuBurg.setOnClickListener {
            (activity as HomeActivity).openDrawer()
        }

        binding.tvEdit.setOnClickListener {
            startActivity(Intent(requireContext(), CreateProfile::class.java).also {
                it.putExtra("isEditProfile", true)
            })
        }
    }


    private fun observeGetProfile() =
        viewModel.getProfile.observeData(viewLifecycleOwner, onLoading = {
            showProgressDialog()
        }, onSuccess = {
            hideProgressDialog()
            setUpUi(this)
        }, onError = {
            hideProgressDialog()
            hideKeyboard()
            showToastShort(this)
        })


    /**
     * Set Up UI
     * */
    @SuppressLint("SetTextI18n")
    private fun setUpUi(login: Login?) = try {
        binding.tvName.text = login?.userName.orEmpty()
        binding.tvFirstName.text = login?.firstName.orEmpty()
        binding.tvLastName.text = login?.lastName.orEmpty()
        binding.tvEmailAddress.text = login?.email.orEmpty()
        binding.tvPhoneNo.text = "${login?.countryCode.orEmpty()} ${login?.phoneNo.orEmpty()}"
        binding.rlAddress.isVisible = login?.address?.isNotEmpty() == true
        binding.tvAddress.text = login?.address.orEmpty()
        val type =
            SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID)
        binding.tvServiceType.text = if (type == 1) "Ride" else "Delivery"
        Glide.with(binding.ivProfileImage).load(login?.driverImage.orEmpty())
            .error(R.drawable.ic_profile_user).into(binding.ivProfileImage)
        SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
            ?.let {
                it.login?.let { loginData ->
                    loginData.userName = login?.userName.orEmpty()
                    loginData.firstName = login?.firstName.orEmpty()
                    loginData.lastName = login?.lastName.orEmpty()
                    loginData.email = login?.email.orEmpty()
                    loginData.phoneNo = login?.phoneNo.orEmpty()
                    loginData.countryCode = login?.countryCode.orEmpty()
                    loginData.address = login?.address.orEmpty()
                }
                SharedPreferencesManager.putModel(SharedPreferencesManager.Keys.USER_DATA, it)
            }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}