package com.ujeff_customer.view.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mukesh.photopicker.utils.pickerDialog
import com.ujeff_customer.R
import com.ujeff_customer.customClasses.singleClick.setOnSingleClickListener
import com.ujeff_customer.databinding.BottomSheetAddPackageBinding
import com.ujeff_customer.databinding.FragmentAddPackageBinding
import com.ujeff_customer.databinding.ItemPackageListBinding
import com.ujeff_customer.databinding.ItemPackageTypeBinding
import com.ujeff_customer.dialogs.DialogUtils
import com.ujeff_customer.model.api.getPartMap
import com.ujeff_customer.model.api.observeData
import com.ujeff_customer.model.dataClass.AddPackage
import com.ujeff_customer.model.dataClass.userData.UserDataDC
import com.ujeff_customer.util.GenericAdapter
import com.ujeff_customer.util.SharedPreferencesManager
import com.ujeff_customer.util.arrayAdapter
import com.ujeff_customer.util.showSnackBar
import com.ujeff_customer.view.base.BaseFragment
import com.ujeff_customer.viewmodel.rideVM.RideVM
import java.io.File

class AddPackageFragment : BaseFragment<FragmentAddPackageBinding>() {
    lateinit var binding: FragmentAddPackageBinding
    private lateinit var packagesAdapter: GenericAdapter<AddPackage>
    private val addedPackagesArrayList = ArrayList<AddPackage>()
    private val rideVM by activityViewModels<RideVM>()
    private var packageImageUrl = ""
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var isImageChanged = false
    override fun initialiseFragmentBaseViewModel() {
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_add_package
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        setAdapter()
        observeData()
        binding.tvAddPackage.setOnSingleClickListener {
            showAddPackageBottomSheet()
        }
        binding.llAddPackage.setOnSingleClickListener {
            showAddPackageBottomSheet()
        }
        binding.ivBack.setOnSingleClickListener { findNavController().popBackStack() }
        binding.tvContinue.setOnSingleClickListener {
            if (addedPackagesArrayList.isEmpty())
                showSnackBar("Please add package to continue.")
            else
                findNavController().navigate(R.id.packageReviewDetailsFragment)
        }
        // Setup the permission launcher
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                handlePermissionResult(permissions)
            }

    }


    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (permissions[Manifest.permission.CAMERA] == true
                && permissions[Manifest.permission.READ_MEDIA_IMAGES] == true
            ) {
                pickerDialog().setPickerCloseListener { _, uris ->
                    bindingM.ivPackageImage.isVisible = true
                    bindingM.llUploadImage.isVisible = false
                    bindingM.ivDelete.isVisible = true
                    Glide.with(this).load(uris).into(bindingM.ivPackageImage)
                    isImageChanged = true
                    packageImageUrl = uris
                }.show()
            } else {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) || shouldShowRequestPermissionRationale(
                        Manifest.permission.CAMERA
                    )
                ) {
                    DialogUtils.getPermissionDeniedDialog(
                        requireActivity(),
                        0,
                        getString(R.string.allow_camera_and_gallery),
                        ::onDialogPermissionAllowClick
                    )
                } else {
                    DialogUtils.getPermissionDeniedDialog(
                        requireActivity(),
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
                    bindingM.ivPackageImage.isVisible = true
                    bindingM.llUploadImage.isVisible = false
                    bindingM.ivDelete.isVisible = true
                    Glide.with(this).load(uris).into(bindingM.ivPackageImage)
                    isImageChanged = true
                    packageImageUrl = uris
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
                        requireActivity(),
                        0,
                        getString(R.string.allow_camera_and_gallery),
                        ::onDialogPermissionAllowClick
                    )
                } else {
//                    showSettingsDialog(this)
                    DialogUtils.getPermissionDeniedDialog(
                        requireActivity(),
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
                data = Uri.fromParts("package", requireActivity().packageName, null)
            }
            startActivity(intent)
        }
    }

    private fun setAdapter() {
        packagesAdapter = object : GenericAdapter<AddPackage>(R.layout.item_package_list) {
            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val bindingM = ItemPackageListBinding.bind(holder.itemView)
                val data = getItem(position)

                bindingM.tvPackageSize.text = data.packageSize
                bindingM.tvPackageType.text = data.packageType
                bindingM.tvPackageQuantity.text = data.quantity

                bindingM.ivDelete.setOnSingleClickListener {
                    getNegativeDialog(position)
                }
                bindingM.ivEdit.setOnSingleClickListener {
                    showAddPackageBottomSheet(true, position)
                }
            }
        }
        addedPackagesArrayList.clear()
        SharedPreferencesManager.getAddPackageList(
            SharedPreferencesManager.Keys.ADD_PACKAGE
        )?.let { addedPackagesArrayList.addAll(it) }
        packagesAdapter.submitList(
            addedPackagesArrayList
        )

        binding.rvAddedPackages.adapter = packagesAdapter
        if (packagesAdapter.itemCount == 0) {
            binding.llAddPackage.isVisible = true
            binding.rvAddedPackages.isVisible = false
            binding.tvContinue.isVisible = false
        } else {
            binding.llAddPackage.isVisible = false
            binding.rvAddedPackages.isVisible = true
            binding.tvContinue.isVisible = true
        }
    }

    private lateinit var packagesTypeAdapter: GenericAdapter<UserDataDC.Login.PackageDetail>
    var packageId = 0
    var packageSize = ""
    var isEditPackage = false
    var editPackagePosition = 0
    private lateinit var bindingM: BottomSheetAddPackageBinding
    private lateinit var dialog: BottomSheetDialog
    private fun showAddPackageBottomSheet(isEdit: Boolean = false, position: Int = 0) {
        dialog = BottomSheetDialog(requireActivity(), R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        bindingM =
            BottomSheetAddPackageBinding.inflate(LayoutInflater.from(context), null, false)
        dialog.setContentView(bindingM.root)
        bindingM.tvSelectPackageType.setOnSingleClickListener {
            SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
                ?.let {
                    it.login?.packageTypes
                }?.let { it ->
                    requireActivity().arrayAdapter(
                        autoCompleteTextView = bindingM.tvSelectPackageType,
                        list = it
                    ) { s ->
//                        bindingM.tvSelectPackageType.setText(s)
                    }
                }
        }
        isEditPackage = isEdit
        isImageChanged = false
        bindingM.ivDelete.setOnSingleClickListener {
            bindingM.ivPackageImage.isVisible = false
            bindingM.ivDelete.isVisible = false
            bindingM.llUploadImage.isVisible = true
            packageImageUrl = ""
        }
        if (isEdit) {
            editPackagePosition = position
            bindingM.tvAddPackage.text = getString(R.string.update_package)
            bindingM.ivDelete.isVisible = true

            addedPackagesArrayList[position].let {
                packageId = it.id
                bindingM.ivPackageImage.isVisible = true
                bindingM.llUploadImage.isVisible = false
                Glide.with(requireActivity()).load(it.image).into(bindingM.ivPackageImage)
                packageImageUrl = it.image
                bindingM.tvSelectPackageType.setText(it.packageType)
                bindingM.etItemName.setText(it.itemDescription)
                bindingM.etQuantity.setText(it.quantity)
            }
        }
        packagesTypeAdapter =
            object : GenericAdapter<UserDataDC.Login.PackageDetail>(R.layout.item_package_type) {
                @SuppressLint("SetTextI18n")
                override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                    val binding = ItemPackageTypeBinding.bind(holder.itemView)
                    val data = getItem(position)
                    Glide.with(binding.root).load(data.package_3d_image.orEmpty())
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher).into(binding.ivVehicleImage)
                    binding.tvVehicleName.text = data.package_size
                    binding.tvSize.text = data.package_description
                    if (data.package_id == packageId) {
                        binding.clBackground.strokeWidth =
                            4 // Set stroke width to highlight the selected card
                        binding.clBackground.strokeColor =
                            ContextCompat.getColor(requireActivity(), R.color.theme)
                        packageSize = data.package_size
                        bindingM.etWidth.setText(data.package_width.toString() + " (${data.package_size_units})")
                        bindingM.etHeight.setText(data.package_height.toString() + " (${data.package_size_units})")
                        bindingM.etLength.setText(data.package_length.toString() + " (${data.package_size_units})")
                        bindingM.etWeight.setText(data.package_weight.toString())

                    } else {
                        binding.clBackground.strokeWidth = 0 // Remove stroke for unselected cards
                    }//
                    binding.clBackground.setOnSingleClickListener {
                        packageId = data.package_id
                        refreshAdapter()
                    }
                }
            }
        val vehicleSize = ArrayList<UserDataDC.Login.PackageDetail>()
        SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
            ?.let {
                it.login?.packageDetails?.let { it1 -> vehicleSize.addAll(it1) }
            }
        packagesTypeAdapter.submitList(vehicleSize)
        bindingM.rvPackageSize.adapter = packagesTypeAdapter
        bindingM.rlDismiss.setOnClickListener {
            dialog.dismiss()
            packageId = 0
            isImageChanged = false
            packageImageUrl = ""
        }

        bindingM.llUploadImage.setOnSingleClickListener {
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
        bindingM.tvAddPackage.setOnSingleClickListener {
            if (packageId == 0)
                showSnackBar("Please select package size.", bindingM.clRootView)
            else if (bindingM.tvSelectPackageType.text.toString().trim().isEmpty())
                showSnackBar("Please select package type.", bindingM.clRootView)
            else if (bindingM.etItemName.text.toString().trim().isEmpty())
                showSnackBar("Please enter item description.", bindingM.clRootView)
            else if (bindingM.etQuantity.text.toString().trim().isEmpty())
                showSnackBar("Please add item quantity.", bindingM.clRootView)
            else if (bindingM.etQuantity.text.toString().trim().toInt() > 100)
                showSnackBar("Quantity should be less than 100.", bindingM.clRootView)
            else if (packageImageUrl.isEmpty())
                showSnackBar("Please upload package image.", bindingM.clRootView)
            else {
                if (isEditPackage) {
                    if (isImageChanged)
                        rideVM.uploadDocument(part = File(packageImageUrl).getPartMap("image"))
                    else {
                        dialog.dismiss()
                        addedPackagesArrayList[editPackagePosition].apply {
                            this.id = packageId
                            this.packageSize = this@AddPackageFragment.packageSize
                            this.packageType = bindingM.tvSelectPackageType.text.toString()
                            this.itemDescription = bindingM.etItemName.text.toString()
                            this.quantity = bindingM.etQuantity.text.toString()
                            this.length = bindingM.etLength.text.toString()
                            this.width = bindingM.etWidth.text.toString()
                            this.height = bindingM.etHeight.text.toString()
                            this.weight = bindingM.etWeight.text.toString()
                            this.image = packageImageUrl
                        }
                        SharedPreferencesManager.putAddPackageList(
                            SharedPreferencesManager.Keys.ADD_PACKAGE,
                            addedPackagesArrayList
                        )
                        packagesAdapter.submitList(
                            addedPackagesArrayList
                        )
                        packagesAdapter.refreshAdapter()
                        packageId = 0
                        packageImageUrl = ""
                    }
                } else
                    rideVM.uploadDocument(part = File(packageImageUrl).getPartMap("image"))
            }
        }
        dialog.setCancelable(true)
        dialog.show()
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    requireActivity(),
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
                    bindingM.ivPackageImage.isVisible = true
                    bindingM.llUploadImage.isVisible = false
                    bindingM.ivDelete.isVisible = true
                    isImageChanged = true
                    Glide.with(this).load(uris).into(bindingM.ivPackageImage)
                    packageImageUrl = uris
                }.show()
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    requireActivity(),
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
                    bindingM.ivPackageImage.isVisible = true
                    bindingM.llUploadImage.isVisible = false
                    bindingM.ivDelete.isVisible = true
                    isImageChanged = true
                    Glide.with(this).load(uris).into(bindingM.ivPackageImage)
                    packageImageUrl = uris
                }.show()
            }

        }
    }

    private fun observeData() = rideVM.uploadPackage.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        dialog.dismiss()
        isImageChanged = false
        if (isEditPackage) {
            val file = this?.file_path.orEmpty()
            addedPackagesArrayList[editPackagePosition].apply {
                this.id = packageId
                this.packageSize = this@AddPackageFragment.packageSize
                this.packageType = bindingM.tvSelectPackageType.text.toString()
                this.itemDescription = bindingM.etItemName.text.toString()
                this.quantity = bindingM.etQuantity.text.toString()
                this.length = bindingM.etLength.text.toString()
                this.width = bindingM.etWidth.text.toString()
                this.height = bindingM.etHeight.text.toString()
                this.weight = bindingM.etWeight.text.toString()
                this.image = file
            }
            SharedPreferencesManager.putAddPackageList(
                SharedPreferencesManager.Keys.ADD_PACKAGE,
                addedPackagesArrayList
            )
            packagesAdapter.submitList(
                addedPackagesArrayList
            )
            packagesAdapter.refreshAdapter()
            packageId = 0
            packageImageUrl = ""
        } else
            addedPackagesArrayList.add(
                AddPackage(
                    packageId,
                    packageSize,
                    bindingM.tvSelectPackageType.text.toString(),
                    bindingM.etItemName.text.toString(),
                    bindingM.etQuantity.text.toString(),
                    bindingM.etLength.text.toString(),
                    bindingM.etWidth.text.toString(),
                    bindingM.etHeight.text.toString(),
                    bindingM.etWeight.text.toString(),
                    this?.file_path.orEmpty()
                )
            )
        SharedPreferencesManager.putAddPackageList(
            SharedPreferencesManager.Keys.ADD_PACKAGE,
            addedPackagesArrayList
        )
        packagesAdapter.submitList(
            addedPackagesArrayList
        )
        packagesAdapter.refreshAdapter()
        binding.llAddPackage.isVisible = false
        binding.rvAddedPackages.isVisible = true
        binding.tvContinue.isVisible = true
        packageId = 0
        packageImageUrl = ""
    }, onError = {
        hideProgressDialog()
        showToastShort(this)
    })

    fun getNegativeDialog(position: Int): Dialog {
        val dialogView = Dialog(requireActivity())
        with(dialogView) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(R.layout.dialog_negative_two_button)
            setCancelable(false)
            val tvCancel = findViewById<AppCompatTextView>(R.id.tvCancel)
            val tvTitle = findViewById<AppCompatTextView>(R.id.tvTitle)
            val tvConfirm = findViewById<AppCompatTextView>(R.id.tvConfirm)
            tvTitle.text = "Are you sure you want to remove package?"
            tvConfirm.text = "Remove"

            tvCancel.setOnSingleClickListener {
                dismiss()
            }

            tvConfirm.setOnSingleClickListener {
                addedPackagesArrayList.removeAt(position)
                SharedPreferencesManager.putAddPackageList(
                    SharedPreferencesManager.Keys.ADD_PACKAGE,
                    addedPackagesArrayList
                )
                packagesAdapter.submitList(addedPackagesArrayList)
                packagesAdapter.refreshAdapter()
                if (packagesAdapter.itemCount == 0) {
                    binding.llAddPackage.isVisible = true
                    binding.rvAddedPackages.isVisible = false
                    binding.tvContinue.isVisible = false
                } else {
                    binding.llAddPackage.isVisible = false
                    binding.rvAddedPackages.isVisible = true
                    binding.tvContinue.isVisible = true
                }
                dismiss()
            }
            show()
        }
        return dialogView
    }
}