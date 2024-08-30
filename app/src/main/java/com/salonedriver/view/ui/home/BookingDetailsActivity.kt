package com.salonedriver.view.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.salonedriver.BuildConfig
import com.salonedriver.R
import com.salonedriver.customClasses.singleClick.setOnSingleClickListener
import com.salonedriver.databinding.ActivityBookingDetailsBinding
import com.salonedriver.model.api.observeData
import com.salonedriver.model.dataclassses.bookingHistory.RideSummaryDC
import com.salonedriver.util.SharedPreferencesManager
import com.salonedriver.util.formatAmount
import com.salonedriver.util.getTime
import com.salonedriver.view.base.BaseActivity
import com.salonedriver.viewmodel.BookingVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookingDetailsActivity : BaseActivity<ActivityBookingDetailsBinding>() {
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
        downloadID =  downloadManager.enqueue(request) // Enqueue the download
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
                    showPermissionRationaleDialog(this)
                } else
                    showSettingsDialog(this)
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