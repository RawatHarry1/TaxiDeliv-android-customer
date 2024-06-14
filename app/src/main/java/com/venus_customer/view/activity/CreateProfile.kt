package com.venus_customer.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.matter.companion.util.ValidationUtils
import com.mukesh.photopicker.utils.pickerDialog
import com.venus_customer.R
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.ActivityCreateProfileBinding
import com.venus_customer.model.api.getJsonRequestBody
import com.venus_customer.model.api.observeData
import com.venus_customer.util.SharedPreferencesManager
import com.venus_customer.util.getValue
import com.venus_customer.view.base.BaseActivity
import com.venus_customer.viewmodel.HomeVM
import com.venus_customer.viewmodel.base.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.RequestBody

@AndroidEntryPoint
class CreateProfile : BaseActivity<ActivityCreateProfileBinding>() {

    lateinit var binding: ActivityCreateProfileBinding
    private val viewModel by viewModels<ProfileViewModel>()
    private val homeViewModel by viewModels<HomeVM>()
    private val isEditProfile by lazy { intent.getBooleanExtra("isEditProfile", false) }


    override fun getLayoutId(): Int {
        return R.layout.activity_create_profile
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        clickListener()
        observeData()
        observeProfileData()
        if (isEditProfile) {
            homeViewModel.loginViaToken()
        }
    }


    private fun clickListener() {
        viewModel.needToUploadImage = !isEditProfile
        binding.tvCreateProfile.text = if (isEditProfile) getString(R.string.edit_your_nprofile) else getString(R.string.create_your_nprofile)
        binding.ivCamera.setOnSingleClickListener {
            pickerDialog().setPickerCloseListener { _, uris ->
                viewModel.needToUploadImage = true
                Glide.with(this).load(uris).into(binding.ivProfile)
                viewModel.imagePath = uris
            }.show()
        }

        binding.tvUploadPhoto.setOnSingleClickListener {
            binding.ivCamera.performClick()
        }

        binding.ivBack.setOnSingleClickListener {
            finish()
        }

        binding.tvSubmitBtn.setOnSingleClickListener {
            if (checkValidation()) {
                viewModel.updateProfile(HashMap<String, RequestBody?>().apply {
                    put("firstName", binding.etFirstName.text.toString().getJsonRequestBody())
                    put("lastName", binding.etLastName.text.toString().getJsonRequestBody())
                    put("userName", binding.etUserName.text.toString().getJsonRequestBody())
                    put(
                        "updatedUserEmail",
                        binding.etEmailAddress.text.toString().getJsonRequestBody()
                    )
                    put("address", binding.etStreetName.text.toString().getJsonRequestBody())
                })
            }
        }
    }


    private fun checkValidation(): Boolean {
        return when {

            viewModel.imagePath.isNullOrEmpty() -> {
                showToastShort(getString(R.string.please_select_profile_pic))
                false
            }

            binding.etFirstName.getValue().isEmpty() -> {
                showToastShort(getString(R.string.please_enter_first_name))
                false
            }

            binding.etLastName.getValue().isEmpty() -> {
                showToastShort(getString(R.string.please_enter_last_name))
                false
            }

            binding.etUserName.getValue().isEmpty() -> {
                showToastShort(getString(R.string.please_enter_username))
                false
            }

            binding.etEmailAddress.getValue().isEmpty() -> {
                showToastShort(getString(R.string.please_enter_email_address))
                false
            }

            !ValidationUtils.isEmailValid(binding.etEmailAddress.getValue()) -> {
                showToastShort(getString(R.string.please_enter_valid_email_address))
                false
            }

            else -> {
                true
            }
        }
    }


    private fun observeData() = viewModel.updateProfile.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        if (isEditProfile){
            finish()
        }else{
            startActivity(Intent(this@CreateProfile, Welcome::class.java))
            finish()
        }
    }, onError = {
        hideProgressDialog()
        showToastShort(this)
    })


    private fun observeProfileData() = homeViewModel.loginViaToken.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        SharedPreferencesManager.putModel(SharedPreferencesManager.Keys.USER_DATA, this)
        viewModel.imagePath = this?.login?.userImage.orEmpty()
        Glide.with(this@CreateProfile).load(viewModel.imagePath).error(R.drawable.circleimage).into(binding.ivProfile)
        binding.etFirstName.setText(this?.login?.firstName.orEmpty())
        binding.etLastName.setText(this?.login?.lastName.orEmpty())
        binding.etUserName.setText(this?.login?.userName.orEmpty())
        binding.etEmailAddress.setText(this?.login?.userEmail.orEmpty())
        binding.etStreetName.setText(this?.login?.address.orEmpty())
    }, onError = {
        hideProgressDialog()
        showToastShort(this)
    })

}