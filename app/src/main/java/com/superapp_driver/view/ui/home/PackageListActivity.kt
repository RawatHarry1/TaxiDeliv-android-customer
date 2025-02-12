package com.superapp_driver.view.ui.home

import android.Manifest
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mukesh.photopicker.utils.getMediaFilePathFor
import com.superapp_driver.R
import com.superapp_driver.SaloneDriver
import com.superapp_driver.customClasses.singleClick.setOnSingleClickListener
import com.superapp_driver.databinding.ActivityPackageListBinding
import com.superapp_driver.databinding.ItemPackageImagesBinding
import com.superapp_driver.databinding.ItemPackageListBinding
import com.superapp_driver.databinding.ItemUploadPackageImageBinding
import com.superapp_driver.dialogs.CustomProgressDialog
import com.superapp_driver.dialogs.DialogUtils
import com.superapp_driver.model.api.getJsonRequestBody
import com.superapp_driver.model.api.getPartMap
import com.superapp_driver.model.api.observeData
import com.superapp_driver.model.dataclassses.clientConfig.ClientConfigDC
import com.superapp_driver.model.dataclassses.rideModels.OngoingPackages
import com.superapp_driver.model.dataclassses.userData.UserDataDC
import com.superapp_driver.util.GenericAdapter
import com.superapp_driver.util.SharedPreferencesManager
import com.superapp_driver.view.base.BaseActivity
import com.superapp_driver.viewmodel.RideViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import java.io.File

@AndroidEntryPoint
class PackageListActivity : BaseActivity<ActivityPackageListBinding>() {
    private lateinit var packagesAdapter: GenericAdapter<OngoingPackages>
    private val packagesArrayList = ArrayList<OngoingPackages>()
    private lateinit var packagesImageAdapter: GenericAdapter<Any>
    private val packImageArrayList = ArrayList<String>()
    lateinit var binding: ActivityPackageListBinding
    private var isEndTrip = false
    private val viewModel by viewModels<RideViewModel>()
    private val rejectionReasonAdapter by lazy { RejectionReasonAdapter() }
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private val progressBar by lazy { CustomProgressDialog() }
    private var rejectionReason = ""
    private var isRestrictionEnabled = 0
    private var distance = ""
    override fun getLayoutId(): Int {
        return R.layout.activity_package_list
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        isEndTrip = intent?.getBooleanExtra("isEndTrip", false) ?: false
        setAdapter()
        observeData()
        observeOngoingTrip()
        observeEndTrip()
        observeStartTrip()
        observeUpdatePackageStatus()
        observeCancelTrip()
        getDistance()
        observeTripOtp()

        viewModel.ongoingTrip()
        binding.ivBack.setOnClickListener { finish() }
        binding.tvSubmit.setOnClickListener {
            if (isEndTrip) {
                SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
                    ?.let {
                        if (it.login?.servicesConfig?.isNotEmpty() == true) {
                            it.login?.servicesConfig?.firstOrNull()?.let { config ->
                                if (config.config != null && config.config.authentication_with_otp == 1) {
                                    viewModel.rideOtp(
                                        customerId = viewModel.newRideNotificationData.customerId.orEmpty(),
                                        tripId = viewModel.newRideNotificationData.tripId.orEmpty()
                                    )
                                } else {
                                    viewModel.endTrip(
                                        customerId = viewModel.newRideNotificationData.customerId.orEmpty(),
                                        tripId = viewModel.newRideNotificationData.tripId.orEmpty(),
                                        dropLatitude = viewModel.newRideNotificationData.dropLatitude.orEmpty(),
                                        dropLongitude = viewModel.newRideNotificationData.dropLongitude.orEmpty(),
                                        distanceTravelled = viewModel.newRideNotificationData.distanceTravelled.orEmpty(),
                                        rideTime = viewModel.newRideNotificationData.rideTime.orEmpty(),
                                        waitTime = viewModel.newRideNotificationData.waitTime.orEmpty()
                                    )
                                }
                            } ?: run {
                                viewModel.endTrip(
                                    customerId = viewModel.newRideNotificationData.customerId.orEmpty(),
                                    tripId = viewModel.newRideNotificationData.tripId.orEmpty(),
                                    dropLatitude = viewModel.newRideNotificationData.dropLatitude.orEmpty(),
                                    dropLongitude = viewModel.newRideNotificationData.dropLongitude.orEmpty(),
                                    distanceTravelled = viewModel.newRideNotificationData.distanceTravelled.orEmpty(),
                                    rideTime = viewModel.newRideNotificationData.rideTime.orEmpty(),
                                    waitTime = viewModel.newRideNotificationData.waitTime.orEmpty()
                                )
                            }
                        } else {
                            viewModel.endTrip(
                                customerId = viewModel.newRideNotificationData.customerId.orEmpty(),
                                tripId = viewModel.newRideNotificationData.tripId.orEmpty(),
                                dropLatitude = viewModel.newRideNotificationData.dropLatitude.orEmpty(),
                                dropLongitude = viewModel.newRideNotificationData.dropLongitude.orEmpty(),
                                distanceTravelled = viewModel.newRideNotificationData.distanceTravelled.orEmpty(),
                                rideTime = viewModel.newRideNotificationData.rideTime.orEmpty(),
                                waitTime = viewModel.newRideNotificationData.waitTime.orEmpty()
                            )
                        }
                    }
            } else
                viewModel.startTrip(
                    customerId = viewModel.newRideNotificationData.customerId.orEmpty(),
                    tripId = viewModel.newRideNotificationData.tripId.orEmpty()
                )
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
//                pickerDialog().setPickerCloseListener { _, uris ->
//                    Log.i("UPLOADIMAGE", "checkPermissions if ")
//                    viewModel.uploadPackageImage(
//                        part = File(uris).getPartMap("image"), hashMap = hashMapOf(
//                            "trip_id" to viewModel.newRideNotificationData.tripId?.getJsonRequestBody()
//                        )
//                    )
//                }.show()
                openCamera()
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
                } else
                    DialogUtils.getPermissionDeniedDialog(
                        this,
                        1,
                        getString(R.string.allow_camera_and_gallery),
                        ::onDialogPermissionAllowClick
                    )
            }
        } else {
            if (permissions[Manifest.permission.CAMERA] == true
                && permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
                && permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
            ) {
                openCamera()
//                pickerDialog().setPickerCloseListener { _, uris ->
//                    Log.i("UPLOADIMAGE", "Handle else ")
//                    viewModel.uploadPackageImage(
//                        part = File(uris).getPartMap("image"), hashMap = hashMapOf(
//                            "trip_id" to viewModel.newRideNotificationData.tripId?.getJsonRequestBody()
//                        )
//                    )
//                }.show()
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

    private lateinit var imageUri: Uri
    private val takePictureResultContract =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) {
                imageUri.let { uri ->
                    getMediaFilePathFor(uri).let { path ->
                        viewModel.uploadPackageImage(
                            part = File(path).getPartMap("image"), hashMap = hashMapOf(
                                "trip_id" to viewModel.newRideNotificationData.tripId?.getJsonRequestBody()
                            )
                        )
                    }
                }
            }
        }


    private fun openCamera() {
        val fileName = "${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, fileName)
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TENET KYC")
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?.let {
                imageUri = it
                takePictureResultContract.launch(it)
            } ?: openCamera()
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
//                pickerDialog().setPickerCloseListener { _, uris ->
//                    Log.i("UPLOADIMAGE", "checkPermissions if ")
//                    viewModel.uploadPackageImage(
//                        part = File(uris).getPartMap("image"), hashMap = hashMapOf(
//                            "trip_id" to viewModel.newRideNotificationData.tripId?.getJsonRequestBody()
//                        )
//                    )
//                }.show()
                openCamera()
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
//                pickerDialog().setPickerCloseListener { _, uris ->
//                    Log.i("UPLOADIMAGE", "checkPermissions else ")
//                    viewModel.uploadPackageImage(
//                        part = File(uris).getPartMap("image"), hashMap = hashMapOf(
//                            "trip_id" to viewModel.newRideNotificationData.tripId?.getJsonRequestBody()
//                        )
//                    )
//                }.show()
                openCamera()
            }
        }
    }

    private fun setAdapter() {
        packagesAdapter = object : GenericAdapter<OngoingPackages>(R.layout.item_package_list) {
            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val binding = ItemPackageListBinding.bind(holder.itemView)
                val data = getItem(position)
                binding.tvPackageSize.text = data.package_size ?: ""
                binding.tvPackageType.text = data.package_type
                binding.tvPackageQuantity.text = data.package_quantity.toString()
                binding.llAcceptAndReject.isVisible = true
                binding.tvLabel.text = "Pickup Images"


//                "REQUESTED", 0
//                "PACKAGE_PICKED", 1
//                "STARTED", 2
//                "PACKAGE_DELIVERY_FAILED", 3
//                "PACKAGE_DELIVERED_SUCCESSFULLY", 4
//                "PACKAGE_PICKUP_FAILED", 5

                if (isEndTrip) {
                    if (data.delivery_status == 3 || data.delivery_status == 4 || data.delivery_status == 5) {
                        binding.tvAccept.alpha = .5f
                        binding.tvReject.alpha = .5f
                        binding.tvAccept.isEnabled = false
                        binding.tvReject.isEnabled = false
                        binding.rlStatus.isVisible = data.delivery_status == 5
                        binding.statusViewLine.isVisible = data.delivery_status == 5
                    } else {
                        binding.tvAccept.alpha = 1f
                        binding.tvReject.alpha = 1f
                        binding.tvAccept.isEnabled = true
                        binding.tvReject.isEnabled = true
                    }
                } else {
                    if (data.delivery_status == 1 || data.delivery_status == 2 || data.delivery_status == 5) {
                        binding.tvAccept.alpha = .5f
                        binding.tvReject.alpha = .5f
                        binding.tvAccept.isEnabled = false
                        binding.tvReject.isEnabled = false
                    } else {
                        binding.tvAccept.alpha = 1f
                        binding.tvReject.alpha = 1f
                        binding.tvAccept.isEnabled = true
                        binding.tvReject.isEnabled = true
                    }
                }


                val adapterPickup = object : GenericAdapter<String>(R.layout.item_package_images) {
                    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                        val bindingM = ItemPackageImagesBinding.bind(holder.itemView)
                        Glide.with(this@PackageListActivity).load(getItem(position).toString())
                            .into(bindingM.ivUploadedImage)
                        bindingM.root.setOnClickListener {
                            fullImagesDialog(getItem(position))
                        }
                    }
                }
                val adapter = object : GenericAdapter<String>(R.layout.item_package_images) {
                    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                        val bindingM = ItemPackageImagesBinding.bind(holder.itemView)
                        Glide.with(this@PackageListActivity).load(getItem(position).toString())
                            .into(bindingM.ivUploadedImage)
                        bindingM.root.setOnClickListener {
                            fullImagesDialog(getItem(position))
                        }
                    }
                }
                adapter.submitList(data.package_image_while_drop_off)
                adapterPickup.submitList(data.package_image_while_pickup)
                binding.rvPickupImages.adapter = adapterPickup
                binding.rvDropImages.adapter = adapter
                binding.llPickupImages.isVisible = adapterPickup.itemCount > 0
                binding.llDropImages.isVisible = adapter.itemCount > 0
                if (isEndTrip) {
                    binding.tvAccept.text = getString(R.string.delivered_c)
                    binding.tvReject.text = getString(R.string.not_delivered_c)
                } else {
                    binding.tvAccept.text = getString(R.string.accept_c)
                    binding.tvReject.text = getString(R.string.reject)
                }
                binding.tvAccept.setOnClickListener {
                    SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
                        ?.let {
                            if (it.login?.servicesConfig?.isNotEmpty() == true) {
                                it.login?.servicesConfig?.firstOrNull()?.let { config ->
                                    if (config.config != null && config.config.driver_package_images == 1) {
                                        uploadImagesDialog(
                                            data.package_id.toString(), if (isEndTrip)
                                                data.package_image_while_drop_off
                                            else
                                                data.package_image_while_pickup, false
                                        )
                                    }
                                    else{
                                        val driverId =
                                            SharedPreferencesManager.getModel<UserDataDC>(
                                                SharedPreferencesManager.Keys.USER_DATA
                                            )
                                                ?.let {
                                                    it.login?.userId ?: ""
                                                }
                                        viewModel.updatePackage(
                                            viewModel.newRideNotificationData.tripId.toString(),
                                            driverId ?: "",
                                            data.package_id.toString(),
                                            rejectionReason,
                                            null, isEndTrip
                                        )
                                    }
                                } ?: run {
                                    val driverId =
                                        SharedPreferencesManager.getModel<UserDataDC>(
                                            SharedPreferencesManager.Keys.USER_DATA
                                        )
                                            ?.let {
                                                it.login?.userId ?: ""
                                            }
                                    viewModel.updatePackage(
                                        viewModel.newRideNotificationData.tripId.toString(),
                                        driverId ?: "",
                                        data.package_id.toString(),
                                        rejectionReason,
                                        null, isEndTrip
                                    )
                                }
                            } else {
                                val driverId =
                                    SharedPreferencesManager.getModel<UserDataDC>(
                                        SharedPreferencesManager.Keys.USER_DATA
                                    )
                                        ?.let {
                                            it.login?.userId ?: ""
                                        }
                                viewModel.updatePackage(
                                    viewModel.newRideNotificationData.tripId.toString(),
                                    driverId ?: "",
                                    data.package_id.toString(),
                                    rejectionReason,
                                    null, isEndTrip
                                )
                            }
                        }

                }
                binding.tvReject.setOnClickListener {
                    uploadImagesDialog(
                        data.package_id.toString(), if (isEndTrip)
                            data.package_image_while_drop_off
                        else
                            data.package_image_while_pickup, true
                    )
                }
            }
        }
        packagesArrayList.clear()
        packagesAdapter.submitList(
            packagesArrayList
        )
        binding.rvAddedPackages.adapter = packagesAdapter
    }

    fun uploadImagesDialog(
        packageId: String, packageList: List<String>, isReject: Boolean
    ): Dialog {
        val dialogView = Dialog(this)
        with(dialogView) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(R.layout.dialog_upload_package_images)
            setCancelable(false)
            val rvAddImages = findViewById<RecyclerView>(R.id.rvAddImages)
            val rvReason = findViewById<RecyclerView>(R.id.rvReason)
            val clReason = findViewById<ConstraintLayout>(R.id.clReason)
            val rlDismiss = findViewById<RelativeLayout>(R.id.rlDismiss)
            val tvConfirm = findViewById<TextView>(R.id.tvConfirm)
            val tvRejectReason = findViewById<TextView>(R.id.tvRejectReason)
            if (isReject) {
                tvConfirm.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.red_text_color)
                if (isEndTrip) {
                    tvConfirm.text = getString(R.string.not_delivered_c)
                } else {
                    tvConfirm.text = getString(R.string.reject)
                }
            }
            rlDismiss.setOnClickListener { dismiss() }
            tvConfirm.setOnSingleClickListener {
                if (isReject) {
                    rejectionReason = rejectionReasonAdapter.getSelectedItemName() ?: ""
                    if (rejectionReason.isNotEmpty()) {
                        dismiss()
                        val driverId =
                            SharedPreferencesManager.getModel<UserDataDC>(
                                SharedPreferencesManager.Keys.USER_DATA
                            )
                                ?.let {
                                    it.login?.userId ?: ""
                                }
                        viewModel.updatePackage(
                            viewModel.newRideNotificationData.tripId.toString(),
                            driverId ?: "",
                            packageId,
                            rejectionReason,
                            packImageArrayList.filter { it.isNotEmpty() }, isEndTrip
                        )
                    } else
                        showSnackBar("Please select cancellation reason.", tvConfirm)
                } else {
                    if (packImageArrayList.size > 1) {
                        dismiss()
                        val cityId =
                            SharedPreferencesManager.getModel<UserDataDC>(
                                SharedPreferencesManager.Keys.USER_DATA
                            )?.login?.city
                        val driverId =
                            SharedPreferencesManager.getModel<UserDataDC>(
                                SharedPreferencesManager.Keys.USER_DATA
                            )
                                ?.let {
                                    it.login?.userId ?: ""
                                }
                        viewModel.updatePackage(
                            viewModel.newRideNotificationData.tripId.toString(),
                            driverId ?: "",
                            packageId,
                            null,
                            packImageArrayList.filter { it.isNotEmpty() }, isEndTrip,
                            SaloneDriver.latLng?.latitude.toString(),
                            SaloneDriver.latLng?.longitude.toString(),
                            viewModel.newRideNotificationData.dropLatitude,
                            viewModel.newRideNotificationData.dropLongitude,
                            cityId.toString(), isRestrictionEnabled, distance
                        )
                    } else
                        showSnackBar("Please upload package image.", tvConfirm)
                }
            }

            packagesImageAdapter =
                object : GenericAdapter<Any>(R.layout.item_upload_package_image) {
                    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                        val binding = ItemUploadPackageImageBinding.bind(holder.itemView)
                        val data = getItem(position)
                        if (data.toString().isEmpty()) {
                            binding.ivUploadedImage.isVisible = false
                            binding.ivAddImages.isVisible = true
                            binding.ivDelete.isVisible = false
                        } else {
                            Glide.with(this@PackageListActivity).load(data.toString())
                                .into(binding.ivUploadedImage)
                            binding.ivUploadedImage.isVisible = true
                            binding.ivAddImages.isVisible = false
                            binding.ivDelete.isVisible = true
                        }
                        binding.ivDelete.setOnClickListener {
                            packImageArrayList.removeAt(position)
                            packagesImageAdapter.submitList(packImageArrayList)
                            packagesImageAdapter.refreshAdapter()
                        }
                        binding.root.setOnClickListener {
                            if (data.toString().isEmpty()) {
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
                            } else
                                fullImagesDialog(data.toString())
                        }
                    }
                }
            packImageArrayList.clear()
            packImageArrayList.addAll(packageList)
            packImageArrayList.add("")
            packagesImageAdapter.submitList(packImageArrayList)
            rvAddImages.adapter = packagesImageAdapter
            rvReason.adapter = rejectionReasonAdapter
            rejectionReasonAdapter.submitList(
                SharedPreferencesManager.getModel<UserDataDC>(
                    SharedPreferencesManager.Keys.USER_DATA
                )?.login?.deliveryCancellationReasons ?: emptyList()
            )
            rvReason.isVisible = isReject
            clReason.isVisible = isReject
            tvRejectReason.isVisible = isReject


            // Set width to full screen
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            show()
        }
        return dialogView
    }

    private fun getDistance() {
        try {
            val cityList =
                SharedPreferencesManager.getModel<ClientConfigDC>(SharedPreferencesManager.Keys.CLIENT_CONFIG)?.cityList
                    ?: ArrayList()
            isRestrictionEnabled = cityList.find {
                it.cityId == SharedPreferencesManager.getModel<UserDataDC>(
                    SharedPreferencesManager.Keys.USER_DATA
                )?.login?.city.orEmpty()
            }?.packageDeliveryRestrictionEnabled ?: 0
            distance = cityList.find {
                it.cityId == SharedPreferencesManager.getModel<UserDataDC>(
                    SharedPreferencesManager.Keys.USER_DATA
                )?.login?.city.orEmpty()
            }?.maximumDistance ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun observeData() = viewModel.uploadPackage.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        this?.file_path?.let { packImageArrayList.add(0, it) }
        packagesImageAdapter.submitList(packImageArrayList)
        packagesImageAdapter.refreshAdapter()
    }, onError = {
        hideProgressDialog()
        showToastShort(this)
    })


    private fun onReject(type: Int) {
        viewModel.cancelTrip(jsonObject = JSONObject().apply {
            put("engagementId", viewModel.newRideNotificationData.tripId)
            put("customerId", viewModel.newRideNotificationData.customerId)
            put("cancellationReason", rejectionReason)
            put("by_operator", 0)
        })
    }

    private fun observeOngoingTrip() =
        viewModel.ongoingTrip.observeData(this, onSuccess = {
            hideProgressDialog()
//            progressBar.show(this@PackageListActivity)
            if (this?.trips?.isNotEmpty() == true) {
                this.trips.firstOrNull()?.let {
                    viewModel.newRideNotificationData = it
                }
            }
            if (this?.deliveryPackages?.isNotEmpty() == true) {
                packagesAdapter.submitList(
                    this.deliveryPackages
                )
                packagesAdapter.refreshAdapter()
            }
            if (isEndTrip) {
                if (this?.canEnd != 1) {
                    binding.tvSubmit.alpha = 0.5f
                    binding.tvSubmit.isEnabled = false
                } else {
                    binding.tvSubmit.alpha = 1f
                    binding.tvSubmit.isEnabled = true
                }

            } else {
                if (this?.canStart != 1) {
                    binding.tvSubmit.alpha = 0.5f
                    binding.tvSubmit.isEnabled = false
                } else {
                    binding.tvSubmit.alpha = 1f
                    binding.tvSubmit.isEnabled = true
                }
            }
        }, onError = {
            hideProgressDialog()
            showToastLong(this)
        })

    /**
     * Observe End Trip
     * */
    private fun observeEndTrip() =
        viewModel.endTrip.observeData(this, onLoading = {
//            showProgressDialog()
            progressBar.show(this)
        }, onSuccess = {
//            hideProgressDialog()
            progressBar.dismiss()
            otpDialog?.dismiss()
            otpDialog = null

            viewModel.newRideNotificationData.also {
                it.estimatedDriverFare = this?.estimatedDriverFare
                it.customerName = this?.customerName
                it.customerImage = this?.customerImage
                it.rideTime = this?.date
                it.paidUsingWallet = this?.paidUsingWallet
            }
            startActivity(
                Intent(this@PackageListActivity, AcceptTripActivity::class.java).putExtra(
                    "screenType", "RideCompleted"
                ).putExtra("rideData", viewModel.newRideNotificationData)
            )
            finish()
        }, onError = {
//            hideProgressDialog()
            progressBar.dismiss()
            showToastLong(this)
        })

    private fun observeUpdatePackageStatus() =
        viewModel.updatePackage.observeData(this, onLoading = {
            progressBar.show(this)
        }, onSuccess = {
            progressBar.dismiss()
            if (isEndTrip) {
                if (this?.can_end != 1) {
                    binding.tvSubmit.alpha = 0.5f
                    binding.tvSubmit.isEnabled = false
                } else {
                    binding.tvSubmit.alpha = 1f
                    binding.tvSubmit.isEnabled = true
                }
            } else {
                if (this?.can_start != 1) {
                    binding.tvSubmit.alpha = 0.5f
                    binding.tvSubmit.isEnabled = false
                } else {
                    binding.tvSubmit.alpha = 1f
                    binding.tvSubmit.isEnabled = true
                }
                if (this?.message != null) {
                    DialogUtils.getNegativeDialog(
                        this@PackageListActivity,
                        "Reject/Cancel Delivery",
                        this.message,
                        ::onReject
                    )
                }

            }
            viewModel.ongoingTrip()
        }, onError = {
            progressBar.dismiss()
            showToastLong(this)
        })

    /**
     * Observe Start Trip
     * */
    private fun observeStartTrip() = viewModel.startTrip.observeData(this, onLoading = {
//        showProgressDialog()
        progressBar.show(this@PackageListActivity)
    }, onError = {
//        hideProgressDialog()
        progressBar.dismiss()
        showToastLong(this)
    }, onSuccess = {
//        hideProgressDialog()
        progressBar.dismiss()
        finish()
    })

    /**
     * Observe Cancel Trip
     * */
    private fun observeCancelTrip() = viewModel.cancelTrip.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        showToastLong(getString(R.string.ride_has_been_cancelled_by_you))
        finish()
    }, onError = {
        hideProgressDialog()
        showErrorMessage(this)
    })

    data class ReasonItem(
        val itemName: String? = null,
        var isSelected: Boolean = false
    )

    fun fullImagesDialog(
        string: String
    ): Dialog {
        val dialogView = Dialog(this)
        with(dialogView) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(R.layout.dialog_full_image)
            setCancelable(false)
            val rlDismiss = findViewById<RelativeLayout>(R.id.rlDismiss)
            rlDismiss.setOnClickListener { dismiss() }
            val ivPackageImage = findViewById<ImageView>(R.id.ivPackageImage)
            Glide.with(this@PackageListActivity).load(string)
                .into(ivPackageImage)
            // Set width to full screen
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            show()
        }
        return dialogView
    }

    /**
     * Observe Trip OTP
     * */
    private var otpDialog: Dialog? = null
    private fun observeTripOtp() =
        viewModel.rideOtp.observeData(this, onLoading = {
            showProgressDialog()
        }, onSuccess = {
            hideProgressDialog()
            otpDialog?.let {
                return@let
            } ?: run {
                otpDialog = DialogUtils.verifyOtpDialog(
                    this@PackageListActivity,
                    dismissDialog = { dialog ->
                        dialog.dismiss()
                        otpDialog = null
                    },
                    verify = { otp, dialog ->
                        otpDialog = dialog
                        viewModel.endTrip(
                            customerId = viewModel.newRideNotificationData.customerId.orEmpty(),
                            tripId = viewModel.newRideNotificationData.tripId.orEmpty(),
                            dropLatitude = viewModel.newRideNotificationData.dropLatitude.orEmpty(),
                            dropLongitude = viewModel.newRideNotificationData.dropLongitude.orEmpty(),
                            distanceTravelled = viewModel.newRideNotificationData.distanceTravelled.orEmpty(),
                            rideTime = viewModel.newRideNotificationData.rideTime.orEmpty(),
                            waitTime = viewModel.newRideNotificationData.waitTime.orEmpty(),
                            otp
                        )
                    },
                    resend = {
                        viewModel.rideOtp(
                            customerId = viewModel.newRideNotificationData.customerId.orEmpty(),
                            tripId = viewModel.newRideNotificationData.tripId.orEmpty()
                        )
                    }
                )
            }
        }, onError = {
            hideProgressDialog()
            showToastLong(this)
        })

}