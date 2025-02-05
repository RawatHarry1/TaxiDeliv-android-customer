package com.mb_driver.view.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.mb_driver.R
import com.mb_driver.databinding.FragmentAccountBinding
import com.mb_driver.model.api.observeData
import com.mb_driver.model.dataclassses.userData.Login
import com.mb_driver.model.dataclassses.userData.UserDataDC
import com.mb_driver.util.SharedPreferencesManager
import com.mb_driver.view.base.BaseFragment
import com.mb_driver.view.ui.CreateProfile
import com.mb_driver.view.ui.home_drawer.HomeActivity
import com.mb_driver.viewmodel.UserAccountVM
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