package com.ujeff_customer.view.fragment.trips

import android.Manifest
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
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
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
import com.ujeff_customer.BuildConfig
import com.ujeff_customer.R
import com.ujeff_customer.customClasses.singleClick.setOnSingleClickListener
import com.ujeff_customer.databinding.FragmentRideDetailsBinding
import com.ujeff_customer.databinding.ItemPackageImagesBinding
import com.ujeff_customer.databinding.ItemPackageListBinding
import com.ujeff_customer.dialogs.DialogUtils
import com.ujeff_customer.model.api.observeData
import com.ujeff_customer.model.dataClass.tripsDC.RideSummaryDC
import com.ujeff_customer.util.GenericAdapter
import com.ujeff_customer.util.formatString
import com.ujeff_customer.util.getTime
import com.ujeff_customer.util.showSnackBar
import com.ujeff_customer.view.base.BaseFragment
import com.ujeff_customer.viewmodel.rideVM.RideVM
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RideDetailsFragment : BaseFragment<FragmentRideDetailsBinding>() {
    private lateinit var packagesAdapter: GenericAdapter<RideSummaryDC.OngoingPackages>
    lateinit var binding: FragmentRideDetailsBinding
    private val navArgs by navArgs<RideDetailsFragmentArgs>()
    private val viewModel by viewModels<RideVM>()
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var supportNumber = ""
    private lateinit var requestPermissionLauncherForCall: ActivityResultLauncher<String>
    private var downloadID: Long = 0

    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            Log.i("onDownloadComplete", "in receiver  ${id}")
            if (id == downloadID) {
                showToastShort("Invoice downloaded successfully")
            }
        }
    }

    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_ride_details
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        setAdapter()
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
        requestPermissionLauncherForCall = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                if (supportNumber.isEmpty())
                    showSnackBar("There is some issue in call. Please try after sometimes.")
                else
                    makePhoneCall(supportNumber) // Replace with the phone number you want to call
            } else {
                DialogUtils.getPermissionDeniedDialog(
                    requireActivity(),
                    1,
                    getString(R.string.allow_call_permission),
                    ::onDialogCallPermissionAllowClick
                )
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireActivity().registerReceiver(
                onDownloadComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED
            )
        } else {
            requireActivity().registerReceiver(
                onDownloadComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }

        binding.rlPackage.setOnSingleClickListener {
            if (binding.rvAddedPackages.isVisible) {
                binding.ivArrowPackage.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_drop_down_theme
                    )
                )
                binding.rvAddedPackages.isVisible = false
            } else {
                binding.ivArrowPackage.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_arrow_up_theme
                    )
                )
                binding.rvAddedPackages.isVisible = true
            }
        }

        binding.tvRaiseTicket.setOnClickListener {
            findNavController().navigate(
                R.id.raiseATicketFragment, bundleOf(
                    "tripId" to navArgs.tripId
                )
            )
        }
    }

    private fun onDialogCallPermissionAllowClick(type: Int) {
        if (type == 0) {
            requestPermissionLauncherForCall.launch(Manifest.permission.CALL_PHONE)
        } else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", requireActivity().packageName, null)
            }
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            requireActivity().unregisterReceiver(onDownloadComplete)
        } catch (e: Exception) {
        }
    }

    private fun checkPermissionAndMakeCall(phoneNumber: String) {
        when {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted
                makePhoneCall(phoneNumber)
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE) -> {
                // Show rationale and request permission
                // You can show a dialog explaining why you need this permission
                DialogUtils.getPermissionDeniedDialog(
                    requireActivity(),
                    0,
                    getString(R.string.allow_call_permission),
                    ::onDialogCallPermissionAllowClick
                )
            }

            else -> {
                // Directly request the permission
                DialogUtils.getPermissionDeniedDialog(
                    requireActivity(),
                    1,
                    getString(R.string.allow_call_permission),
                    ::onDialogCallPermissionAllowClick
                )
            }
        }
    }

    private fun makePhoneCall(phoneNumber: String) {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$phoneNumber")
        startActivity(callIntent)
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
            if (supportNumber.isEmpty())
                showSnackBar("There is some issue in call. Please try after sometime.")
            else
                checkPermissionAndMakeCall(supportNumber)
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

    private fun downloadPdf(context: Context, url: String, title: String, description: String) {
        showToastShort("Downloading invoice please wait!!")
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(title) // Title of the Download Notification
            .setDescription(description) // Description of the Download Notification
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED or DownloadManager.Request.VISIBILITY_VISIBLE) // Visibility of the download Notification
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
        Log.i("onDownloadComplete", "download  ${downloadID}")
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

    private fun onDialogDownloadPermissionAllowClick(type: Int) {
        if (type == 0) {
            checkPermissions()
        } else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", requireActivity().packageName, null)
            }
            startActivity(intent)
        }
    }

    private fun setUpUI(rideSummaryDC: RideSummaryDC?) {
        try {
            supportNumber = rideSummaryDC?.supportNumber ?: ""
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

            if (rideSummaryDC?.toPay.orEmpty().ifEmpty { "0" }.toInt() > 0) {
                binding.rlPaymentCash.isVisible = true
                binding.tvPaymentCash.text = "${rideSummaryDC?.currency.orEmpty()} ${
                    rideSummaryDC?.toPay.orEmpty().formatString()
                }"
            } else
                binding.rlPaymentCash.isVisible = false

            binding.tvSubTotal.text = "${rideSummaryDC?.currency.orEmpty()} ${
                rideSummaryDC?.fare.orEmpty().formatString()
            }"
            binding.tvVat.text = "${rideSummaryDC?.currency.orEmpty()} ${
                rideSummaryDC?.netCustomerTax.orEmpty().formatString()
            }"

            if (rideSummaryDC?.discountValue.orEmpty().ifEmpty { "0" }.toInt() > 0) {
                binding.tvDiscount.text = "${rideSummaryDC?.currency.orEmpty()} ${
                    rideSummaryDC?.discountValue.orEmpty().formatString()
                }"
            } else {
                binding.rlDiscount.isVisible = false
            }

            binding.tvTotalCharge.text = "${rideSummaryDC?.currency.orEmpty()} ${
                rideSummaryDC?.tripTotal.orEmpty().formatString()
            }"

            if (rideSummaryDC?.paidUsingStripe.orEmpty().ifEmpty { "0" }.toInt() > 0) {
                binding.rlPaidUsingStripe.isVisible = true
                binding.tvPaidUsingStripe.text = "${rideSummaryDC?.currency.orEmpty()} ${
                    rideSummaryDC?.paidUsingStripe.orEmpty().formatString()
                }"
            }

            if (rideSummaryDC?.payUsingPaystack.orEmpty().ifEmpty { "0" }.toInt() > 0) {
                binding.rlPaidUsingStripe.isVisible = true
                binding.tvUsingStripe.text = "Paid Using Mobile Money"
                binding.tvPaidUsingStripe.text = "${rideSummaryDC?.currency.orEmpty()} ${
                    rideSummaryDC?.payUsingPaystack.orEmpty().formatString()
                }"
            }

            if (rideSummaryDC?.paidUsingWallet.orEmpty().ifEmpty { "0" }.toInt() > 0) {
                binding.rlPaidUsingWallet.isVisible = true
                binding.tvPaidUsingWallet.text = "${rideSummaryDC?.currency.orEmpty()} ${
                    rideSummaryDC?.paidUsingWallet.orEmpty().formatString()
                }"
            }

            if (rideSummaryDC?.paidUsingPaytm.orEmpty().ifEmpty { "0" }.toInt() > 0) {
                binding.rlPaidUsingPaytm.isVisible = true
                binding.tvPaidUsingPaytm.text = "${rideSummaryDC?.currency.orEmpty()} ${
                    rideSummaryDC?.paidUsingPaytm.orEmpty().formatString()
                }"
            }

            Glide.with(this).load(rideSummaryDC?.driverImage.orEmpty())
                .error(R.drawable.circleimage).into(binding.ivDriverImage)
            Glide.with(this).load(rideSummaryDC?.trackingImage.orEmpty()).into(binding.mapImage)
            binding.nsvScrollView.isVisible = true

            if (rideSummaryDC?.serviceType == 2) {
                binding.ivArrowPackage.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_drop_down_theme
                    )
                )
                binding.rlPackage.isVisible = true
                binding.rvAddedPackages.isVisible = true
                binding.viewLine1.isVisible = true
                binding.viewLine2.isVisible = true
            }
            packagesAdapter.submitList(
                rideSummaryDC?.deliveryPackages.orEmpty()
            )
            packagesAdapter.refreshAdapter()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (permissions[Manifest.permission.READ_MEDIA_IMAGES] == true
            ) {
                val url = "${BuildConfig.BASE_URL}ride/invoice?ride_id=${navArgs.tripId}"
                downloadPdf(
                    requireActivity(),
                    url,
                    "RideInvoice",
                    "Downloading Your File"
                )
            } else {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                ) {
                    DialogUtils.getPermissionDeniedDialog(
                        requireActivity(),
                        0,
                        getString(R.string.allow_download_permission),
                        ::onDialogDownloadPermissionAllowClick
                    )
                } else
                    DialogUtils.getPermissionDeniedDialog(
                        requireActivity(),
                        1,
                        getString(R.string.allow_download_permission),
                        ::onDialogDownloadPermissionAllowClick
                    )
            }
        } else {
            if (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
                && permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
            ) {
                val url = "${BuildConfig.BASE_URL}ride/invoice?ride_id=${navArgs.tripId}"
                downloadPdf(
                    requireActivity(),
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
                        requireActivity(),
                        0,
                        getString(R.string.allow_download_permission),
                        ::onDialogDownloadPermissionAllowClick
                    )
                } else {
                    DialogUtils.getPermissionDeniedDialog(
                        requireActivity(),
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
                val url = "${BuildConfig.BASE_URL}ride/invoice?ride_id=${navArgs.tripId}"
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
                val url = "${BuildConfig.BASE_URL}ride/invoice?ride_id=${navArgs.tripId}"
                downloadPdf(
                    requireActivity(),
                    url,
                    "RideInvoice",
                    "Downloading Your File"
                )
            }
        }
    }

    private fun setAdapter() {
        packagesAdapter =
            object : GenericAdapter<RideSummaryDC.OngoingPackages>(R.layout.item_package_list) {
                override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                    val binding = ItemPackageListBinding.bind(holder.itemView)
                    val data = getItem(position)
                    binding.tvPackageSize.text = data.package_size
                    binding.tvPackageType.text = data.package_type
                    binding.tvPackageQuantity.text = data.package_quantity.toString()
                    binding.llCustomerImages.isVisible = true
                    binding.llPickupImages.isVisible = true
                    binding.llDropImages.isVisible = true
                    binding.ivEdit.isVisible = false
                    binding.ivDelete.isVisible = false
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
                                Glide.with(requireActivity())
                                    .load(getItem(position).toString())
                                    .into(bindingM.ivUploadedImage)
                                bindingM.root.setOnClickListener {
                                    fullImagesDialog(getItem(position))
                                }
                            }
                        }

                    val adapterPick =
                        object : GenericAdapter<String>(R.layout.item_package_images) {
                            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                                val bindingM = ItemPackageImagesBinding.bind(holder.itemView)
                                Glide.with(requireActivity()).load(getItem(position).toString())
                                    .into(bindingM.ivUploadedImage)
                                bindingM.root.setOnClickListener {
                                    fullImagesDialog(getItem(position))
                                }
                            }
                        }
                    val adapterDrop =
                        object : GenericAdapter<String>(R.layout.item_package_images) {
                            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                                val bindingM = ItemPackageImagesBinding.bind(holder.itemView)
                                Glide.with(requireActivity()).load(getItem(position).toString())
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
        val dialogView = Dialog(requireActivity())
        with(dialogView) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(R.layout.dialog_full_image)
            setCancelable(false)
            val rlDismiss = findViewById<RelativeLayout>(R.id.rlDismiss)
            rlDismiss.setOnClickListener { dismiss() }
            val ivPackageImage = findViewById<ImageView>(R.id.ivPackageImage)
            Glide.with(requireActivity()).load(string)
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
}