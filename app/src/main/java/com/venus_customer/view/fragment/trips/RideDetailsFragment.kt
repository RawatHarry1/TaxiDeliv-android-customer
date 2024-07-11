package com.venus_customer.view.fragment.trips

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.venus_customer.R
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.FragmentRideDetailsBinding
import com.venus_customer.model.api.observeData
import com.venus_customer.model.dataClass.tripsDC.RideSummaryDC
import com.venus_customer.util.formatString
import com.venus_customer.util.getTime
import com.venus_customer.util.showSnackBar
import com.venus_customer.view.activity.chat.ChatActivity
import com.venus_customer.view.base.BaseFragment
import com.venus_customer.viewmodel.rideVM.RideVM
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RideDetailsFragment : BaseFragment<FragmentRideDetailsBinding>() {

    lateinit var binding: FragmentRideDetailsBinding
    private val navArgs by navArgs<RideDetailsFragmentArgs>()
    private val viewModel by viewModels<RideVM>()
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_ride_details
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        observeRideSummary()
        viewModel.rideSummary(
            tripId = navArgs.tripId,
            driverId = navArgs.driverId
        )
        // Setup the permission launcher
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                handlePermissionResult(permissions)
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

    }

    // Register back press callback
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (navArgs.fromTrip)
                findNavController().popBackStack()
            else
                findNavController().popBackStack(R.id.navigation_home, false)
        }
    }

    override fun onResume() {
        super.onResume()
        setClicks()
    }

    private fun setClicks() {
        binding.ivBack.setOnSingleClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.tvRateDriver.setOnSingleClickListener {
            findNavController().navigate(
                R.id.navigation_rate_driver,
                bundleOf(
                    "engagementId" to viewModel.createRideData.sessionId,
                    "driverName" to binding.tvDriverName.text.toString()
                )
            )
        }

        binding.ivCustomerSupport.setOnSingleClickListener {
            startActivity(Intent(requireContext(), ChatActivity::class.java))
        }

        binding.tvDownloadInvoice.setOnSingleClickListener {
//            if (checkAndRequestPermissions()) {
//                val url = "https://dev-rides.venustaxi.in/ride/invoice?ride_id=${navArgs.tripId}"
//                downloadPdf(requireActivity(), url, "RideInvoice", "Downloading Invoice")
//            }
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
        downloadManager.enqueue(request) // Enqueue the download
    }

    private fun observeRideSummary() = viewModel.rideSummaryData.observeData(
        lifecycle = viewLifecycleOwner,
        onLoading = {
            showProgressDialog()
            binding.nsvScrollView.isVisible = false
        }, onError = {
            hideProgressDialog()
            showSnackBar(this)
        }, onSuccess = {
            hideProgressDialog()
            setUpUI(this)
        }
    )


    private fun setUpUI(rideSummaryDC: RideSummaryDC?) {
        try {
            viewModel.createRideData.sessionId = rideSummaryDC?.engagementId.orEmpty()
            binding.tvTitle.text = rideSummaryDC?.autosStatusText.orEmpty()
            binding.tvStartTime.text =
                rideSummaryDC?.pickupTime.getTime(output = "HH:mm", applyTimeZone = true)
            binding.tvEndTime.text =
                rideSummaryDC?.dropTime.getTime(output = "HH:mm", applyTimeZone = true)
            binding.tvStartAdress.text = rideSummaryDC?.pickupAddress.orEmpty()
            binding.tvEndAddress.text = rideSummaryDC?.dropAddress.orEmpty()
            binding.tvDriverName.text = rideSummaryDC?.driverName.orEmpty()
            if ((rideSummaryDC?.driverRating ?: -1) >= 0) {
                binding.tvRatings.text =
                    rideSummaryDC?.driverRating.toString().ifEmpty { "0.0" }.formatString(1)
            } else {
                binding.tvRatings.text = "0"
            }
            binding.tvCarNo.text = rideSummaryDC?.driverCarNo.orEmpty()
            binding.tvCarBrand.text = rideSummaryDC?.modelName.orEmpty()

            binding.tvHowTrip.text = "How was your trip with ${rideSummaryDC?.driverName.orEmpty()}"
            binding.tvHowTrip.isVisible = (rideSummaryDC?.driverRating ?: -1) <= -1
            binding.tvRateDriver.isVisible = (rideSummaryDC?.driverRating ?: -1) <= -1

            binding.tvPaymentCash.text = "${rideSummaryDC?.currency.orEmpty()} ${
                rideSummaryDC?.fare.orEmpty().formatString()
            }"
            binding.tvSubTotal.text = "${rideSummaryDC?.currency.orEmpty()} ${
                rideSummaryDC?.fare.orEmpty().formatString()
            }"
            binding.tvVat.text = "${rideSummaryDC?.currency.orEmpty()} ${
                rideSummaryDC?.netCustomerTax.orEmpty().formatString()
            }"
            binding.tvDiscount.text = "${rideSummaryDC?.currency.orEmpty()} ${
                rideSummaryDC?.fareDiscount.orEmpty().formatString()
            }"
            binding.tvTotalCharge.text = "${rideSummaryDC?.currency.orEmpty()} ${
                rideSummaryDC?.tripTotal.orEmpty().formatString()
            }"

            Glide.with(this).load(rideSummaryDC?.driverImage.orEmpty())
                .error(R.drawable.circleimage).into(binding.ivDriverImage)
            Glide.with(this).load(rideSummaryDC?.trackingImage.orEmpty()).into(binding.mapImage)
            binding.nsvScrollView.isVisible = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (permissions[Manifest.permission.READ_MEDIA_IMAGES] == true
            ) {
                val url = "https://dev-rides.venustaxi.in/ride/invoice?ride_id=${navArgs.tripId}"
                downloadPdf(
                    requireActivity(),
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
                    showPermissionRationaleDialog(requireActivity())
                } else
                    showSettingsDialog(requireActivity())
            }
        } else {
            if (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
                && permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
            ) {
                val url = "https://dev-rides.venustaxi.in/ride/invoice?ride_id=${navArgs.tripId}"
                downloadPdf(
                    requireActivity(),
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
                    showPermissionRationaleDialog(requireActivity())
                } else {
                    showSettingsDialog(requireActivity())
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
                    requireActivity(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                )
            } else {
                val url = "https://dev-rides.venustaxi.in/ride/invoice?ride_id=${navArgs.tripId}"
                downloadPdf(
                    requireActivity(),
                    url,
                    "RideInvoice",
                    "Downloading Your File"
                )
            }
        } else {
            if (ContextCompat.checkSelfPermission(
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
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            } else {
                val url = "https://dev-rides.venustaxi.in/ride/invoice?ride_id=${navArgs.tripId}"
                downloadPdf(
                    requireActivity(),
                    url,
                    "RideInvoice",
                    "Downloading Your File"
                )
            }
        }
    }


}