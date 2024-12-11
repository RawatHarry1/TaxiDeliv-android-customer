package com.superapp_driver.view.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.superapp_driver.BuildConfig
import com.superapp_driver.R
import com.superapp_driver.customClasses.singleClick.setOnSingleClickListener
import com.superapp_driver.databinding.ActivityBookingDetailsBinding
import com.superapp_driver.databinding.ItemPackageImagesBinding
import com.superapp_driver.databinding.ItemPackageListBinding
import com.superapp_driver.dialogs.DialogUtils
import com.superapp_driver.model.api.observeData
import com.superapp_driver.model.dataclassses.bookingHistory.RideSummaryDC
import com.superapp_driver.model.dataclassses.rideModels.OngoingPackages
import com.superapp_driver.util.GenericAdapter
import com.superapp_driver.util.SharedPreferencesManager
import com.superapp_driver.util.formatAmount
import com.superapp_driver.util.getTime
import com.superapp_driver.view.base.BaseActivity
import com.superapp_driver.view.fragment.RaiseATicketActivity
import com.superapp_driver.viewmodel.BookingVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookingDetailsActivity : BaseActivity<ActivityBookingDetailsBinding>() {
    private lateinit var packagesAdapter: GenericAdapter<OngoingPackages>
    lateinit var binding: ActivityBookingDetailsBinding
    private val bookingId by lazy { intent.getStringExtra("bookingId").orEmpty() }
    private val viewModel by viewModels<BookingVM>()
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var downloadID: Long = 0

    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadID) {
                showToastShort("Invoice downloaded successfully")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(onDownloadComplete)
        } catch (e: Exception) {
        }
    }

    /**
     * Get Layout Id
     * */
    override fun getLayoutId(): Int {
        return R.layout.activity_booking_details
    }


    /**
     * On Create
     * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        setAdapter()
        clickHandler()
        observeRideSummary()
        viewModel.rideSummary(tripId = bookingId)
        // Setup the permission launcher
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                handlePermissionResult(permissions)
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                onDownloadComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED
            )
        } else {
            registerReceiver(
                onDownloadComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }
    }

    private fun onDialogDownloadPermissionAllowClick(type: Int) {
        if (type == 0) {
            checkPermissions()
        } else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }
    }

    /**
     * Click Handler
     * */
    private fun clickHandler() {
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.tvDownloadInvoice.setOnSingleClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                requestPermissionLauncher.launch(
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
                )
            else
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
        }

        binding.rlPackage.setOnSingleClickListener {
            if (binding.rvAddedPackages.isVisible) {
                binding.ivArrowPackage.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@BookingDetailsActivity,
                        R.drawable.ic_drop_down_theme
                    )
                )
                binding.rvAddedPackages.isVisible = false
            } else {
                binding.ivArrowPackage.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@BookingDetailsActivity,
                        R.drawable.ic_arrow_up_theme
                    )
                )
                binding.rvAddedPackages.isVisible = true
            }
        }

        binding.tvRaiseTicket.setOnClickListener {
            startActivity(
                Intent(this, RaiseATicketActivity::class.java).putExtra(
                    "tripId",
                    bookingId
                )
            )
        }
    }

    private fun setAdapter() {
        packagesAdapter = object : GenericAdapter<OngoingPackages>(R.layout.item_package_list) {
            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val binding = ItemPackageListBinding.bind(holder.itemView)
                val data = getItem(position)
                binding.tvPackageSize.text = data.package_size
                binding.tvPackageType.text = data.package_type
                binding.tvPackageQuantity.text = data.package_quantity.toString()
                binding.llCustomerImages.isVisible = true
                binding.llPickupImages.isVisible = true
                binding.llDropImages.isVisible = true
                when (data.delivery_status) {
                    5 -> {
                        binding.rlStatus.isVisible = true
                        binding.statusViewLine.isVisible = true
                        binding.llPickupImages.isVisible = false
                        binding.llDropImages.isVisible = false
                    }

                    3 -> {
                        binding.rlStatus.isVisible = true
                        binding.statusViewLine.isVisible = true
                        binding.tvStatus.text = "Not Delivered"
                        binding.llDropImages.isVisible = false
                    }
                }
                val adapterCustomer =
                    object : GenericAdapter<String>(R.layout.item_package_images) {
                        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                            val bindingM = ItemPackageImagesBinding.bind(holder.itemView)
                            Glide.with(this@BookingDetailsActivity)
                                .load(getItem(position).toString())
                                .into(bindingM.ivUploadedImage)
                            bindingM.root.setOnClickListener {
                                fullImagesDialog(getItem(position))
                            }
                        }
                    }

                val adapterPick = object : GenericAdapter<String>(R.layout.item_package_images) {
                    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                        val bindingM = ItemPackageImagesBinding.bind(holder.itemView)
                        Glide.with(this@BookingDetailsActivity).load(getItem(position).toString())
                            .into(bindingM.ivUploadedImage)
                        bindingM.root.setOnClickListener {
                            fullImagesDialog(getItem(position))
                        }
                    }
                }
                val adapterDrop = object : GenericAdapter<String>(R.layout.item_package_images) {
                    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                        val bindingM = ItemPackageImagesBinding.bind(holder.itemView)
                        Glide.with(this@BookingDetailsActivity).load(getItem(position).toString())
                            .into(bindingM.ivUploadedImage)
                        bindingM.root.setOnClickListener {
                            fullImagesDialog(getItem(position))
                        }
                    }
                }
                adapterCustomer.submitList(data.package_images_by_customer)
                binding.rvCustomerImages.adapter = adapterCustomer

                adapterPick.submitList(data.package_image_while_pickup)
                binding.rvPickupImages.adapter = adapterPick

                adapterDrop.submitList(data.package_image_while_drop_off)
                binding.rvDropImages.adapter = adapterDrop
            }
        }
        binding.rvAddedPackages.adapter = packagesAdapter
    }

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
            Glide.with(this@BookingDetailsActivity).load(string)
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
     * Set Up UI
     * */
    @SuppressLint("SetTextI18n")
    private fun setUpUi(dataClass: RideSummaryDC) = try {
        binding.tvCustomerName.text = dataClass.customerName.orEmpty()
        binding.tvAmountReceived.text = getString(
            R.string.your_received_amount_rs_60,
            "${SharedPreferencesManager.getCurrencySymbol()} ${
                dataClass.totalFare.orEmpty().formatAmount()
            }"
        )
        binding.tvRideFare.text = "${SharedPreferencesManager.getCurrencySymbol()} ${
            dataClass.rideFare.orEmpty().ifEmpty { "0.0" }.formatAmount()
        }"
        binding.tvVatValue.text = "${SharedPreferencesManager.getCurrencySymbol()} ${
            dataClass.netCustomerTax.orEmpty().ifEmpty { "0.0" }.formatAmount()
        }"
        binding.tvCommissionValue.text = "${SharedPreferencesManager.getCurrencySymbol()} ${
            dataClass.venusCommission.orEmpty().ifEmpty { "0.0" }.formatAmount()
        }"
        binding.tvSubtotal.text = "${SharedPreferencesManager.getCurrencySymbol()} ${
            dataClass.subTotalRideFare.orEmpty().ifEmpty { "0.0" }.formatAmount()
        }"
        binding.tvTotalFare.text = "${SharedPreferencesManager.getCurrencySymbol()} ${
            dataClass.totalFare.orEmpty().ifEmpty { "0.0" }.formatAmount()
        }"
        binding.tvWaitingTime.text = dataClass.waitTime.orEmpty().ifEmpty { "0" }.plus(" min")
        binding.tvDateTime.text = dataClass.createdAt.getTime(
            input = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            output = "dd/MM/yyyy, hh:mm a",
            applyTimeZone = true
        )

        Glide.with(binding.ivMap).load(dataClass.trackingImage).into(binding.ivMap)
        if (dataClass.serviceType == 2) {
            binding.ivArrowPackage.setImageDrawable(
                ContextCompat.getDrawable(
                    this@BookingDetailsActivity,
                    R.drawable.ic_drop_down_theme
                )
            )
            binding.rlPackage.isVisible = true
            binding.rvAddedPackages.isVisible = true
            binding.viewLine1.isVisible = true
        }
        packagesAdapter.submitList(
            dataClass.deliveryPackages.orEmpty()
        )
        packagesAdapter.refreshAdapter()
    } catch (e: Exception) {
        e.printStackTrace()
    }


    private fun observeRideSummary() = viewModel.rideSummaryData.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        this?.let { setUpUi(it) }
    }, onError = {
        hideProgressDialog()
        showToastShort(this)
    })

    private fun downloadPdf(context: Context, url: String, title: String, description: String) {
        showToastShort("Downloading invoice please wait!!")
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(title) // Title of the Download Notification
            .setDescription(description) // Description of the Download Notification
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // Visibility of the download Notification
            .setAllowedOverMetered(true) // Set if download is allowed on Mobile network
            .setAllowedOverRoaming(true) // Set if download is allowed on roaming network
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "$title.pdf"
            ) // Destination of the file
            .addRequestHeader(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36"
            ) // Add a user-agent header
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadID = downloadManager.enqueue(request) // Enqueue the download
    }

    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (permissions[Manifest.permission.READ_MEDIA_IMAGES] == true
            ) {
                val url = "${BuildConfig.BASE_URL}ride/invoice?ride_id=${bookingId}"
                downloadPdf(
                    this,
                    url,
                    "RideInvoice",
                    "Downloading Your File"
                )
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
                        getString(R.string.allow_download_permission),
                        ::onDialogDownloadPermissionAllowClick
                    )
                } else
                    DialogUtils.getPermissionDeniedDialog(
                        this,
                        1,
                        getString(R.string.allow_download_permission),
                        ::onDialogDownloadPermissionAllowClick
                    )
            }
        } else {
            if (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
                && permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
            ) {
                val url = "${BuildConfig.BASE_URL}ride/invoice?ride_id=${bookingId}"
                downloadPdf(
                    this,
                    url,
                    "RideInvoice",
                    "Downloading Your File"
                )
            } else {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) || shouldShowRequestPermissionRationale(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    DialogUtils.getPermissionDeniedDialog(
                        this,
                        0,
                        getString(R.string.allow_download_permission),
                        ::onDialogDownloadPermissionAllowClick
                    )
                } else {
                    DialogUtils.getPermissionDeniedDialog(
                        this,
                        1,
                        getString(R.string.allow_download_permission),
                        ::onDialogDownloadPermissionAllowClick
                    )
                }
            }
        }
    }

    private fun showSettingsDialog(context: Context) {
        AlertDialog.Builder(context).apply {
            setTitle("Download Invoice")
            setMessage("Please allow permissions to download invoice.")
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
            setTitle("Download Invoice")
            setMessage("Please allow permissions to download invoice.")
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
            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                )
            } else {
                val url = "${BuildConfig.BASE_URL}ride/invoice?ride_id=${bookingId}"
                downloadPdf(
                    this,
                    url,
                    "RideInvoice",
                    "Downloading Your File"
                )
            }
        } else {
            if (ContextCompat.checkSelfPermission(
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
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            } else {
                val url = "${BuildConfig.BASE_URL}ride/invoice?ride_id=${bookingId}"
                downloadPdf(
                    this,
                    url,
                    "RideInvoice",
                    "Downloading Your File"
                )
            }
        }
    }
}