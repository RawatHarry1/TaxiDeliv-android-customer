package com.salonedriver.view.ui

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
import com.bumptech.glide.Glide
import com.mukesh.photopicker.utils.pickerDialog
import com.salonedriver.R
import com.salonedriver.customClasses.singleClick.setOnSingleClickListener
import com.salonedriver.databinding.ActivityCreateProfileBinding
import com.salonedriver.model.api.getJsonRequestBody
import com.salonedriver.model.api.observeData
import com.salonedriver.model.dataclassses.updateDriverInfo.UpdateDriverInfo
import com.salonedriver.model.dataclassses.userData.UserDataDC
import com.salonedriver.util.NoSpaceInputFilter
import com.salonedriver.util.ResourceUtils
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
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>


    override fun getLayoutId(): Int {
        return R.layout.activity_create_profile
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        binding.etDesignation.filters = arrayOf(NoSpaceInputFilter())
        fetchUserData()
        binding.tvSubmit.setOnSingleClickListener {
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
        observerDriverInfoUpdate()
        // Setup the permission launcher
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                handlePermissionResult(permissions)
            }
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

            binding.etFirstName.getValue().trim().isEmpty() -> {
                showErrorMessage(getString(R.string.please_enter_first_name))
                false
            }

            binding.etFirstName.getValue().trim().length < 3 -> {
                showErrorMessage(getString(R.string.please_enter_first_name_length))
                false
            }

            binding.etLastName.getValue().trim().isEmpty() -> {
                showErrorMessage(getString(R.string.please_enter_last_name))
                false
            }

            binding.etLastName.getValue().trim().length < 3 -> {
                showErrorMessage(getString(R.string.please_enter_last_name_length))
                false
            }

            binding.etDesignation.getValue().trim().isEmpty() -> {
                showErrorMessage(getString(R.string.please_enter_designation))
                false
            }

            binding.etDesignation.getValue().trim().length < 3 -> {
                showErrorMessage(getString(R.string.please_enter_designation_length))
                false
            }

            binding.etEmailCreate.getValue().trim().isEmpty() -> {
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

    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (permissions[Manifest.permission.CAMERA] == true
                && permissions[Manifest.permission.READ_MEDIA_IMAGES] == true
            ) {
                pickerDialog().setPickerCloseListener { _, uris ->
                    Glide.with(this).load(uris).into(binding.ivProfile)
                    viewModel.profilePic = uris
                }.show()
            } else {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) || shouldShowRequestPermissionRationale(
                        Manifest.permission.CAMERA
                    )
                ) {
                    showPermissionRationaleDialog(this)
                } else
                    showSettingsDialog(this)
            }
        } else {
            if (permissions[Manifest.permission.CAMERA] == true
                && permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
                && permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
            ) {
                pickerDialog().setPickerCloseListener { _, uris ->
                    Glide.with(this).load(uris).into(binding.ivProfile)
                    viewModel.profilePic = uris
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
                    showPermissionRationaleDialog(this)
                } else {
                    showSettingsDialog(this)
                }
            }
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
                    Glide.with(this).load(uris).into(binding.ivProfile)
                    viewModel.profilePic = uris
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
                    Glide.with(this).load(uris).into(binding.ivProfile)
                    viewModel.profilePic = uris
                }.show()
            }
        }
    }
}