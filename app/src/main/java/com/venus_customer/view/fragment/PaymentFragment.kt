package com.venus_customer.view.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.ncorti.slidetoact.SlideToActView
import com.venus_customer.R
import com.venus_customer.VenusApp
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.FragmentPaymentBinding
import com.venus_customer.model.api.observeData
import com.venus_customer.util.safeCall
import com.venus_customer.util.showSnackBar
import com.venus_customer.view.activity.walk_though.PaymentActivity
import com.venus_customer.view.base.BaseFragment
import com.venus_customer.viewmodel.rideVM.RideVM
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import java.util.concurrent.TimeUnit

class PaymentFragment : BaseFragment<FragmentPaymentBinding>() {
    private lateinit var binding: FragmentPaymentBinding
    private val rideVM by activityViewModels<RideVM>()
    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_payment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        binding.ivBack.setOnSingleClickListener { findNavController().popBackStack() }
        binding.tvPickUpAddress.text = rideVM.createRideData.pickUpLocation?.address
        binding.tvDropOffAddress.text = rideVM.createRideData.dropLocation?.address
        observePromoCode()
        observeRequestRide()
        observeRequestSchedule()
        rideVM.createRideData.vehicleData?.let {
            binding.tvVehicleName.text = it.name.orEmpty()
            binding.tvWaitingTime.text = it.eta.orEmpty().ifEmpty { "0" }.plus(" min away")
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


        binding.tvConfirm.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {
                override fun onSlideComplete(view: SlideToActView) {
                    safeCall {
                        binding.tvConfirm.setCompleted(completed = false, withAnimation = true)
                        if (rideVM.paymentOption == 9 && rideVM.cardId.isEmpty())
                            showSnackBar("*Please select card for payment*")
                        else {
                            if (rideVM.schedule)
                                rideVM.scheduleRideData(
                                    binding.etNotes.text.toString().trim(),
                                    rideVM.selectedPickDateTimeForSchedule
                                )
                            else
                                rideVM.requestRideData( binding.etNotes.text.toString().trim())
                        }
                    }
                }
            }
        changeSelection(1)
        binding.clPayByCash.setOnSingleClickListener {
            changeSelection(1)
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
        if (selection == 1) {
            rideVM.paymentOption = 1
            binding.ivPayByCash.setImageDrawable(
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
                    R.color.cancel_btn_red_color
                )
            )
            rideVM.couponToApply = this?.codeId ?: 0
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
            rideVM.createRideData.status = 0
            rideVM.cardId = ""
            rideVM.last4 = ""
            rideVM.couponToApply = 0
            rideVM.promoCode = ""
            rideVM.updateUiState(RideVM.RideAlertUiState.FindDriverDialog)
            findNavController().popBackStack(R.id.paymentFragment, true)
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
            rideVM.createRideData.pickUpLocation = null
            rideVM.createRideData.dropLocation = null
            rideVM.cardId = ""
            rideVM.last4 = ""
            rideVM.couponToApply = 0
            rideVM.promoCode = ""
            rideVM.updateUiState(RideVM.RideAlertUiState.HomeScreen)
            findNavController().popBackStack(R.id.paymentFragment, true)
//            rideVM.delivery(true)
        })

}