package com.salonedriver.view.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.mukesh.photopicker.utils.pickerDialog
import com.salonedriver.R
import com.salonedriver.databinding.ActivityCreateProfileBinding
import com.salonedriver.model.api.getJsonRequestBody
import com.salonedriver.model.api.observeData
import com.salonedriver.model.dataclassses.updateDriverInfo.UpdateDriverInfo
import com.salonedriver.model.dataclassses.userData.UserDataDC
import com.salonedriver.util.SharedPreferencesManager
import com.salonedriver.util.ValidationUtils
import com.salonedriver.util.getValue
import com.salonedriver.view.base.BaseActivity
import com.salonedriver.viewmodel.OnBoardingVM
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.RequestBody

@AndroidEntryPoint
class CreateProfile : BaseActivity<ActivityCreateProfileBinding>() {


    lateinit var binding: ActivityCreateProfileBinding
    private val viewModel by viewModels<OnBoardingVM>()
    private val isEditProfile by lazy { intent.getBooleanExtra("isEditProfile", false) }
    override fun getLayoutId(): Int {
        return R.layout.activity_create_profile
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()

        fetchUserData()
        binding.tvSubmit.setOnClickListener {
            if (checkValidation()) {
                viewModel.updateDriverInfo(HashMap<String, RequestBody?>().apply {
                    put("firstName", binding.etFirstName.getValue().getJsonRequestBody())
                    put("lastName", binding.etLastName.getValue().getJsonRequestBody())
                    put("userName", binding.etDesignation.getValue().getJsonRequestBody())
                    put("updatedUserEmail", binding.etEmailCreate.getValue().getJsonRequestBody())
                    if (binding.etEmergency.getValue().isEmpty()) {
                        put(
                            "emergencyPhoneNumber",
                            binding.etEmergency.getValue().getJsonRequestBody()
                        )
                    }
                    put(
                        "address",
                        binding.etStreet.getValue().getJsonRequestBody()
                    )
                })
            }
        }
        binding.ivCloseCreate.setOnClickListener {
            finish()
        }


        binding.ivProfile.setOnClickListener {
            pickerDialog().setPickerCloseListener { _, uris ->
                Glide.with(this).load(uris).into(binding.ivProfile)
                viewModel.profilePic = uris
            }.show()
        }
        observerDriverInfoUpdate()

    }


    private fun fetchUserData() {
        try {
            SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
                ?.let {
                    binding.etPhone.setText(it.login?.phoneNo.orEmpty())
                    if (isEditProfile) {
                        binding.etFirstName.setText(it.login?.firstName.orEmpty())
                        binding.etLastName.setText(it.login?.lastName.orEmpty())
                        binding.etDesignation.setText(it.login?.userName.orEmpty())
                        binding.etEmailCreate.setText(it.login?.email.orEmpty())
                        binding.etStreet.setText(it.login?.address.orEmpty())
                        Glide.with(binding.ivProfile).load(it.login?.driverImage.orEmpty())
                            .error(R.drawable.ic_profile_user).into(binding.ivProfile)
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun checkValidation(): Boolean {
        return when {

            !isEditProfile && viewModel.profilePic.isNullOrEmpty() -> {
                showErrorMessage(getString(R.string.please_select_profile_pic))
                false
            }

            binding.etFirstName.getValue().isEmpty() -> {
                showErrorMessage(getString(R.string.please_enter_first_name))
                false
            }

            binding.etLastName.getValue().isEmpty() -> {
                showErrorMessage(getString(R.string.please_enter_last_name))
                false
            }

            binding.etDesignation.getValue().isEmpty() -> {
                showErrorMessage(getString(R.string.please_enter_designation))
                false
            }

            binding.etEmailCreate.getValue().isEmpty() -> {
                showErrorMessage(getString(R.string.please_enter_email_address))
                false
            }

            !ValidationUtils.isEmailValid(binding.etEmailCreate.getValue()) -> {
                showErrorMessage(getString(R.string.please_enter_valid_email_address))
                false
            }

            else -> {
                true
            }
        }
    }


    private fun observerDriverInfoUpdate() =
        viewModel.updateDriverInfo.observeData(this, onLoading = {
            showProgressDialog()
        }, onSuccess = {
            hideProgressDialog()
            storePreferenceData(this)
        }, onError = {
            hideProgressDialog()
            showErrorMessage(this)
        })


    private fun storePreferenceData(updateDriverInfo: UpdateDriverInfo?) {
        SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
            ?.let {
                it.login?.let { userData ->
                    userData.address = updateDriverInfo?.address
                    userData.firstName = updateDriverInfo?.firstName
                    userData.lastName = updateDriverInfo?.lastName
                    userData.userName = updateDriverInfo?.userName
                    userData.driverId = updateDriverInfo?.driverId
                    userData.email = updateDriverInfo?.email
                    userData.emergencyPhoneNumber = updateDriverInfo?.emergencyPhoneNumber
                    userData.profileImage = updateDriverInfo?.profileImage
                }
                SharedPreferencesManager.putModel(SharedPreferencesManager.Keys.USER_DATA, it)
                if (isEditProfile) finish()
                else {
                    startActivity(Intent(this@CreateProfile, Welcome::class.java))
                    finish()
                }
            }
    }

}