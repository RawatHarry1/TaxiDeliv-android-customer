package com.ujeff_customer.view.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.matter.companion.util.ValidationUtils
import com.mukesh.photopicker.utils.pickerDialog
import com.ujeff_customer.R
import com.ujeff_customer.VenusApp
import com.ujeff_customer.customClasses.singleClick.setOnSingleClickListener
import com.ujeff_customer.databinding.ActivityCreateProfileBinding
import com.ujeff_customer.dialogs.DialogUtils
import com.ujeff_customer.model.api.getJsonRequestBody
import com.ujeff_customer.model.api.observeData
import com.ujeff_customer.util.NoSpaceInputFilter
import com.ujeff_customer.util.SharedPreferencesManager
import com.ujeff_customer.util.getValue
import com.ujeff_customer.view.base.BaseActivity
import com.ujeff_customer.viewmodel.HomeVM
import com.ujeff_customer.viewmodel.base.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.RequestBody

@AndroidEntryPoint
class CreateProfile : BaseActivity<ActivityCreateProfileBinding>() {

    lateinit var binding: ActivityCreateProfileBinding
    private val viewModel by viewModels<ProfileViewModel>()
    private val homeViewModel by viewModels<HomeVM>()
    private val isEditProfile by lazy { intent.getBooleanExtra("isEditProfile", false) }
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

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
            binding.tvReferral.isVisible = false
            binding.etReferral.isVisible = false
            homeViewModel.loginViaToken()
        }
        // Setup the permission launcher
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                handlePermissionResult(permissions)
            }
    }


    private fun clickListener() {
        viewModel.needToUploadImage = !isEditProfile
        binding.etUserName.filters = arrayOf(NoSpaceInputFilter())
        binding.etReferral.filters = arrayOf(NoSpaceInputFilter())
        binding.tvCreateProfile.text =
            if (isEditProfile) getString(R.string.edit_your_nprofile) else getString(R.string.create_your_nprofile)
        binding.ivCamera.setOnSingleClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                )
            else
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
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
                    if (!isEditProfile)
                        put(
                            "referral_code",
                            binding.etReferral.text.toString().getJsonRequestBody()
                        )
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

            binding.etFirstName.getValue().trim().length < 3 -> {
                showToastShort(getString(R.string.please_enter_first_name_length))
                false
            }

            binding.etLastName.getValue().isEmpty() -> {
                showToastShort(getString(R.string.please_enter_last_name))
                false
            }

            binding.etLastName.getValue().trim().length < 3 -> {
                showToastShort(getString(R.string.please_enter_last_name_length))
                false
            }


            binding.etUserName.getValue().isEmpty() -> {
                showToastShort(getString(R.string.please_enter_username))
                false
            }

            binding.etUserName.getValue().trim().length < 3 -> {
                showToastShort(getString(R.string.please_enter_user_name_length))
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
        if (isEditProfile) {
            finish()
        } else {
            VenusApp.isReferee = this?.is_referee ?: false
            VenusApp.referralMsg = this?.message ?: ""
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
        Glide.with(this@CreateProfile).load(viewModel.imagePath).error(R.drawable.circleimage)
            .into(binding.ivProfile)
        binding.etFirstName.setText(this?.login?.firstName.orEmpty())
        binding.etLastName.setText(this?.login?.lastName.orEmpty())
        binding.etUserName.setText(this?.login?.userName.orEmpty())
        binding.etEmailAddress.setText(this?.login?.userEmail.orEmpty())
        binding.etStreetName.setText(this?.login?.address.orEmpty())
    }, onError = {
        hideProgressDialog()
        showToastShort(this)
    })

    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (permissions[Manifest.permission.CAMERA] == true
                && permissions[Manifest.permission.READ_MEDIA_IMAGES] == true
            ) {
                pickerDialog().setPickerCloseListener { _, uris ->
                    viewModel.needToUploadImage = true
                    Glide.with(this).load(uris).into(binding.ivProfile)
                    viewModel.imagePath = uris
                }.show()
            } else {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) || shouldShowRequestPermissionRationale(
                        Manifest.permission.CAMERA
                    )
                ) {
                    DialogUtils.getPermissionDeniedDialog(
                        this,
                        0,
                        getString(R.string.allow_camera_and_gallery),
                        ::onDialogPermissionAllowClick
                    )
                } else {
                    DialogUtils.getPermissionDeniedDialog(
                        this,
                        1,
                        getString(R.string.allow_camera_and_gallery),
                        ::onDialogPermissionAllowClick
                    )
                }
            }
        } else {
            if (permissions[Manifest.permission.CAMERA] == true
                && permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
                && permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
            ) {
                pickerDialog().setPickerCloseListener { _, uris ->
                    viewModel.needToUploadImage = true
                    Glide.with(this).load(uris).into(binding.ivProfile)
                    viewModel.imagePath = uris
                }.show()
            } else {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.CAMERA
                    ) || shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) || shouldShowRequestPermissionRationale(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
//                    showPermissionRationaleDialog(this)
                    DialogUtils.getPermissionDeniedDialog(
                        this,
                        0,
                        getString(R.string.allow_camera_and_gallery),
                        ::onDialogPermissionAllowClick
                    )
                } else {
//                    showSettingsDialog(this)
                    DialogUtils.getPermissionDeniedDialog(
                        this,
                        1,
                        getString(R.string.allow_camera_and_gallery),
                        ::onDialogPermissionAllowClick
                    )
                }
            }
        }
    }

    private fun onDialogPermissionAllowClick(type: Int) {
        if (type == 0) {
            checkPermissions()
        } else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }
    }

    private fun showSettingsDialog(context: Context) {
        AlertDialog.Builder(context).apply {
            setTitle("Permissions")
            setMessage("Please turn on camera and gallery permissions")
            setPositiveButton("Settings") { _, _ ->
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null)
                )
                context.startActivity(intent)
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    private fun showPermissionRationaleDialog(context: Context) {
        AlertDialog.Builder(context).apply {
            setTitle("Permissions")
            setMessage("Please turn on camera and gallery permissions")
            setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                checkPermissions()
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            create()
            show()
        }
    }


    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                )
            } else {
                pickerDialog().setPickerCloseListener { _, uris ->
                    viewModel.needToUploadImage = true
                    Glide.with(this).load(uris).into(binding.ivProfile)
                    viewModel.imagePath = uris
                }.show()
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            } else {
                pickerDialog().setPickerCloseListener { _, uris ->
                    viewModel.needToUploadImage = true
                    Glide.with(this).load(uris).into(binding.ivProfile)
                    viewModel.imagePath = uris
                }.show()
            }
        }
    }
}