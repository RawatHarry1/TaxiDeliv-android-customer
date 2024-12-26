package com.ujeff_customer.view.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.ncorti.slidetoact.SlideToActView
import com.ujeff_customer.R
import com.ujeff_customer.VenusApp
import com.ujeff_customer.customClasses.singleClick.setOnSingleClickListener
import com.ujeff_customer.databinding.FragmentPackageReviewBinding
import com.ujeff_customer.databinding.ItemPackageListBinding
import com.ujeff_customer.model.api.observeData
import com.ujeff_customer.model.dataClass.AddPackage
import com.ujeff_customer.util.GenericAdapter
import com.ujeff_customer.util.SharedPreferencesManager
import com.ujeff_customer.util.safeCall
import com.ujeff_customer.util.showSnackBar
import com.ujeff_customer.view.activity.walk_though.PaymentActivity
import com.ujeff_customer.view.base.BaseFragment
import com.ujeff_customer.viewmodel.rideVM.CreateRideData
import com.ujeff_customer.viewmodel.rideVM.RideVM
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class PackageReviewDetailsFragment : BaseFragment<FragmentPackageReviewBinding>() {
    private lateinit var binding: FragmentPackageReviewBinding
    private lateinit var packagesAdapter: GenericAdapter<AddPackage>
    private val addedPackagesArrayList = ArrayList<AddPackage>()
    private val rideVM by activityViewModels<RideVM>()
    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_package_review
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        Log.i("PAYMENT", "IN create")
        binding.ivBack.setOnSingleClickListener { findNavController().popBackStack() }
        binding.tvPickUpAddress.text = rideVM.createRideData.pickUpLocation?.address
        binding.tvDropOffAddress.text = rideVM.createRideData.dropLocation?.address
        setAdapter()
        observePromoCode()
        observeRequestRide()
        observeRequestSchedule()
        observeInitializePayment()
        observePaymentStatus()
        binding.rlPackage.isVisible =
            SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID) != 1
        binding.rvAddedPackages.isVisible =
            SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID) != 1
        binding.cvReciever.isVisible =
            SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID) != 1
        binding.viewLinePackages.isVisible =
            SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID) != 1
        binding.cvNotes.isVisible =
            SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID) == 1

        rideVM.createRideData.vehicleData?.let {
            binding.tvVehicleName.text = it.name.orEmpty()
            binding.tvWaitingTime.text = it.eta.orEmpty().ifEmpty { "0" }.plus(" min away")

            binding.tvVehicleCapacity.setCompoundDrawablesWithIntrinsicBounds(
                if (
                    SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID) == 1
                ) R.drawable.ic_user_count else R.drawable.ic_kg_white,
                0,
                0,
                0
            )
            binding.tvVehicleCapacity.text = it.totalCapacity.orEmpty().ifEmpty { "0" }
            Glide.with(requireContext()).load(it.image.orEmpty()).error(R.mipmap.ic_launcher)
                .into(binding.ivVehicleImage)

            if ((it.discount ?: 0.0) > 0.0) {
                binding.tvOriginalPrice.isVisible = true
                binding.viewCross.isVisible = true
                binding.tvOriginalPrice.text = "${it.currency} ${it.original_fare}"
                binding.tvPrice.text = "${it.currency} ${it.fare}"
//                binding.tvConfirm.text = "Swipe to pay | ${it.currency} ${it.fare}"
                binding.tvOfferTitle.text = VenusApp.offerTitle
                binding.tvOfferTitle.isVisible = true
            } else {
                binding.tvOriginalPrice.isVisible = false
                binding.viewCross.isVisible = false
                binding.tvPrice.text = "${it.currency} ${it.fare}"
//                binding.tvConfirm.text = "Swipe to pay | ${it.currency} ${it.fare}"
                binding.tvOfferTitle.isVisible = false
            }
        }
        binding.rlDeliveryDetails.setOnSingleClickListener {
            if (binding.clDeliveryDetails.isVisible) {
                binding.ivArrow.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_drop_down_theme
                    )
                )
                binding.clDeliveryDetails.isVisible = false
            } else {
                binding.ivArrow.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_arrow_up_theme
                    )
                )
                binding.clDeliveryDetails.isVisible = true
            }
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

        binding.tvConfirm.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {
                override fun onSlideComplete(view: SlideToActView) {
                    safeCall {
                        binding.tvConfirm.setCompleted(completed = false, withAnimation = true)
                        val operatorId =
                            SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID)
                        if (operatorId == 2 && binding.etRecieverName.text.toString().trim()
                                .isEmpty()
                        )
                            showSnackBar("Please enter Receiver Name")
                        else if (operatorId == 2 && binding.etRecieverPhone.text.toString().trim()
                                .isEmpty()
                        )
                            showSnackBar("Please enter Receiver Phone Number")
                        else if (rideVM.paymentOption == 9 && rideVM.cardId.isEmpty())
                            showSnackBar("*Please select card for payment*")
                        else {
                            if (rideVM.paymentOption == 2) {
                                rideVM.initializeMobileMoney(jsonObject = JSONObject().apply {
                                    put(
                                        "amount",
                                        (rideVM.createRideData.vehicleData?.original_fare
                                            ?: 0.0).toString()
                                    )
                                    put("currency", rideVM.createRideData.currencyCode)
                                })
                            } else {
                                if (rideVM.schedule)
                                    rideVM.scheduleRideData(
                                        receiverName = binding.etRecieverName.text.toString(),
                                        receiverNumber = binding.etRecieverName.text.toString(),
                                        if (operatorId == 1) binding.etNotes.text.toString()
                                            .trim() else "",
                                        rideVM.selectedPickDateTimeForSchedule
                                    )
                                else
                                    rideVM.requestRideData(
                                        receiverName = binding.etRecieverName.text.toString(),
                                        receiverNumber = binding.etRecieverPhone.text.toString(),
                                        if (operatorId == 1) binding.etNotes.text.toString()
                                            .trim() else ""
                                    )
                            }
                        }
                    }
                }
            }
        changeSelection(1)
        binding.clPayByCash.setOnSingleClickListener {
            changeSelection(1)
        }
        binding.clMobilePay.setOnSingleClickListener {
            changeSelection(2)
        }
        binding.clPayByCard.setOnSingleClickListener {
            changeSelection(0)
            if (rideVM.cardId.isEmpty())
                activityResultLauncherForPayment.launch(
                    Intent(
                        requireActivity(),
                        PaymentActivity::class.java
                    ).putExtra("whileRide", true)
                )
        }
        binding.tvChangeCard.setOnSingleClickListener {
            activityResultLauncherForPayment.launch(
                Intent(
                    requireActivity(),
                    PaymentActivity::class.java
                ).putExtra("whileRide", true).putExtra("cardId", rideVM.cardId)
            )
        }

        binding.tvApply.setOnSingleClickListener {
            if (binding.tvApply.text == getString(R.string.remove_c)) {
                binding.etEnterCoupon.isFocusable = true
                binding.etEnterCoupon.isFocusableInTouchMode = true
                binding.tvApply.text = getString(R.string.apply)
                binding.tvApply.setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.theme
                    )
                )
                rideVM.couponToApply = 0
                binding.tvPromoOffer.isVisible = false
            } else {
                if (binding.etEnterCoupon.text.toString().trim().isEmpty()) {
                    showSnackBar("Please Enter Valid Promo Code")
                } else
                    rideVM.enterPromoCode(
                        binding.etEnterCoupon.text.toString().trim(),
                        regionId = rideVM.createRideData?.regionId ?: "",
                        vehicleType = rideVM.createRideData?.vehicleType ?: "",
                        fare = rideVM.createRideData?.vehicleData?.fare ?: "",
                        distance = rideVM.createRideData?.vehicleData?.distance ?: "",
                        currency = rideVM.createRideData?.vehicleData?.currency ?: ""
                    )
            }
        }
    }

    private val activityResultLauncherForPayment = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                rideVM.cardId = result.data?.getStringExtra("cardId") ?: ""
                rideVM.last4 = result.data?.getStringExtra("last4") ?: ""
                binding.tvSelectedCard.isVisible = true
                binding.tvChangeCard.isVisible = true
                binding.tvSelectedCard.text = "Selected card: **** ${rideVM.last4}"
            } catch (e: Exception) {
            }
        }
    }


    private fun setAdapter() {
        packagesAdapter = object : GenericAdapter<AddPackage>(R.layout.item_package_list) {
            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val binding = ItemPackageListBinding.bind(holder.itemView)
                val data = getItem(position)
                binding.tvPackageSize.text = data.packageSize
                binding.tvPackageType.text = data.packageType
                binding.tvPackageQuantity.text = data.quantity
                binding.ivEdit.isVisible = false
                binding.ivDelete.isVisible = false
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
    }

    private fun changeSelection(selection: Int) {
        binding.ivPayByCash.setImageDrawable(
            ContextCompat.getDrawable(
                requireActivity(), R.drawable.bg_circular_stroke_black
            )
        )
        binding.ivPayByCard.setImageDrawable(
            ContextCompat.getDrawable(
                requireActivity(), R.drawable.bg_circular_stroke_black
            )
        )
        binding.ivMobilePay.setImageDrawable(
            ContextCompat.getDrawable(
                requireActivity(), R.drawable.bg_circular_stroke_black
            )
        )
        if (selection == 1) {
            rideVM.paymentOption = 1
            binding.ivPayByCash.setImageDrawable(
                ContextCompat.getDrawable(
                    requireActivity(), R.drawable.ic_tick_circle
                )
            )
        } else if (selection == 2) {
            rideVM.paymentOption = 2
            binding.ivMobilePay.setImageDrawable(
                ContextCompat.getDrawable(
                    requireActivity(), R.drawable.ic_tick_circle
                )
            )
        } else {
            rideVM.paymentOption = 9
            binding.ivPayByCard.setImageDrawable(
                ContextCompat.getDrawable(
                    requireActivity(), R.drawable.ic_tick_circle
                )
            )
        }
    }

    private fun observePromoCode() =
        rideVM.enterPromoCode.observeData(lifecycle = viewLifecycleOwner, onLoading = {
            showProgressDialog()
        }, onError = {
            hideProgressDialog()
            showSnackBar(this)
        }, onSuccess = {
            hideProgressDialog()
            startConfettiAnimation()
            binding.etEnterCoupon.isFocusable = false
            binding.etEnterCoupon.isFocusableInTouchMode = false
            binding.tvApply.text = getString(R.string.remove_c)
            binding.tvApply.setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.theme_red
                )
            )
            rideVM.couponToApply = this?.codeId ?: 0
            binding.tvPromoOffer.isVisible = true
            binding.tvPromoOffer.text = this?.codeMessage ?: ""
            showSnackBar(this?.codeMessage ?: "")
        })

    private fun startConfettiAnimation() {
        binding.konfettiView.start(
            Party(
                speed = 10f,
                maxSpeed = 20f,
                damping = 0.95f,
                spread = 360,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x70e8f8, 0xfffff),
                shapes = listOf(Shape.Circle, Shape.Square),
                size = listOf(
                    nl.dionsegijn.konfetti.core.models.Size.SMALL,
                    nl.dionsegijn.konfetti.core.models.Size.LARGE
                ),
                position = Position.Relative(0.5, 0.3),
                emitter = Emitter(duration = 500, TimeUnit.MILLISECONDS).max(500)
            )
        )
    }

    private fun observeRequestRide() =
        rideVM.requestRideData.observeData(lifecycle = viewLifecycleOwner, onLoading = {
            showProgressDialog()
        }, onError = {
            hideProgressDialog()
            showToastShort(this)
//            rideVM.cardId = ""
//            rideVM.last4 = ""
//            rideVM.couponToApply = 0
//            rideVM.promoCode = ""
//            rideVM.hideHomeNav(false)
//            rideVM.updateUiState(RideVM.RideAlertUiState.HomeScreen)
        }, onSuccess = {
            hideProgressDialog()
            rideVM.createRideData.sessionId = this?.sessionId.orEmpty()
            rideVM.createRideData.referenceId = rideVM.payStackReferenceId
            rideVM.createRideData.status = 0
            rideVM.cardId = ""
            rideVM.last4 = ""
            rideVM.couponToApply = 0
            rideVM.promoCode = ""
            rideVM.updateUiState(RideVM.RideAlertUiState.FindDriverDialog)
            SharedPreferencesManager.clearKeyData(SharedPreferencesManager.Keys.ADD_PACKAGE)
            if (SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID) == 1) {
                findNavController().popBackStack(R.id.packageReviewDetailsFragment, true)
            } else {
                findNavController().popBackStack(R.id.addPackageFragment, true)
                findNavController().popBackStack(R.id.packageReviewDetailsFragment, true)

            }
            rideVM.delivery(true)
        })

    private fun observeRequestSchedule() =
        rideVM.scheduleRideData.observeData(lifecycle = viewLifecycleOwner, onLoading = {
            showProgressDialog()
        }, onError = {
            hideProgressDialog()
            showToastShort(this)
//            rideVM.createRideData.pickUpLocation = null
//            rideVM.createRideData.dropLocation = null
//            rideVM.cardId = ""
//            rideVM.last4 = ""
//            rideVM.couponToApply = 0
//            rideVM.promoCode = ""
//            rideVM.updateUiState(RideVM.RideAlertUiState.HomeScreen)
//            rideVM.hideHomeNav(false)
        }, onSuccess = {
            hideProgressDialog()
            rideVM.hideHomeNav(false)
            showSnackBar("Your ride has been scheduled successfully!!")
            rideVM.createRideData = CreateRideData()
            rideVM.cardId = ""
            rideVM.last4 = ""
            rideVM.couponToApply = 0
            rideVM.promoCode = ""
            rideVM.updateUiState(RideVM.RideAlertUiState.HomeScreen)
            SharedPreferencesManager.clearKeyData(SharedPreferencesManager.Keys.ADD_PACKAGE)
            if (SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID) == 1) {
                findNavController().popBackStack(R.id.packageReviewDetailsFragment, true)
            } else {
                findNavController().popBackStack(R.id.addPackageFragment, true)
                findNavController().popBackStack(R.id.packageReviewDetailsFragment, true)
            }
//            rideVM.delivery(true)
        })

    override fun onResume() {
        super.onResume()
        Log.i("PAYMENT", "IN RESUME")
        if (rideVM.isPaymentInitialized && rideVM.payStackReferenceId != "")
            rideVM.mobileMoneyStatus(jsonObject = JSONObject().apply {
                put("reference", rideVM.payStackReferenceId)
            })
    }


    private fun observeInitializePayment() =
        rideVM.initializeMobileMoney.observeData(lifecycle = viewLifecycleOwner, onLoading = {
            showProgressDialog()
        }, onError = {
            hideProgressDialog()
            showToastShort(this)

        }, onSuccess = {
            hideProgressDialog()
            rideVM.payStackReferenceId = this?.reference.toString()
            rideVM.isPaymentInitialized = true
            this?.authorization_url?.let { openWebBrowser(it) }
        })


    private fun observePaymentStatus() =
        rideVM.mobileMoneyStatus.observeData(lifecycle = viewLifecycleOwner, onLoading = {
            showProgressDialog()
        }, onError = {
            rideVM.isPaymentInitialized = false
            hideProgressDialog()
            showToastShort(this)
        }, onSuccess = {
            hideProgressDialog()
            rideVM.isPaymentInitialized = false
//            payment_status: {
//            PENDING : 0,
//            COMPLETED : 1,
//            FAILED : -1,
//            ABANDONED: 2,
//            ONGOING: 3,
//            QUEUED: 4,
//            REVERSED: 5
//        }
            when (this?.payment_status) {
                1 -> {
                    showSnackBar("Please wait confirming booking for you.")
                    val operatorId =
                        SharedPreferencesManager.getInt(SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID)
                    if (rideVM.schedule)
                        rideVM.scheduleRideData(
                            receiverName = binding.etRecieverName.text.toString(),
                            receiverNumber = binding.etRecieverName.text.toString(),
                            if (operatorId == 1) binding.etNotes.text.toString()
                                .trim() else "",
                            rideVM.selectedPickDateTimeForSchedule
                        )
                    else
                        rideVM.requestRideData(
                            receiverName = binding.etRecieverName.text.toString(),
                            receiverNumber = binding.etRecieverPhone.text.toString(),
                            if (operatorId == 1) binding.etNotes.text.toString()
                                .trim() else ""
                        )
                }

                -1 -> {
                    showSnackBar("Payment has been failed. Please try again")
                }

                2 -> {
                    showSnackBar("Payment has been cancelled")
                }

                3 -> {
                    showSnackBar("Please wait we are verifying your payment")
                    rideVM.mobileMoneyStatus(jsonObject = JSONObject().apply {
                        put("reference", rideVM.payStackReferenceId)
                    })
                }

                0 -> {
                    showSnackBar("We are not able to verify your payment. Please try again")
                }

            }

        })

    private fun openWebBrowser(link: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(link)
            requireActivity().startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }
}