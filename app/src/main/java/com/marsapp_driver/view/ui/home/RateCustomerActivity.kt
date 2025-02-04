package com.marsapp_driver.view.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.marsapp_driver.R
import com.marsapp_driver.customClasses.singleClick.setOnSingleClickListener
import com.marsapp_driver.databinding.ActivityRateCustomerBinding
import com.marsapp_driver.firebaseSetup.NewRideNotificationDC
import com.marsapp_driver.model.api.observeData
import com.marsapp_driver.view.base.BaseActivity
import com.marsapp_driver.view.ui.home_drawer.HomeActivity
import com.marsapp_driver.viewmodel.RideViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import kotlin.math.roundToInt

@AndroidEntryPoint
class RateCustomerActivity : BaseActivity<ActivityRateCustomerBinding>() {
    lateinit var binding: ActivityRateCustomerBinding
    private val viewModel by viewModels<RideViewModel>()
    private val rideData by lazy { intent.getParcelableExtra<NewRideNotificationDC>("rideData") }

    override fun getLayoutId(): Int {
        return R.layout.activity_rate_customer
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        rideData?.let {
            binding.tvRideFareValue.text = "${it.currency} ${it.estimatedDriverFare}"
            binding.tvPaidByWallet.text = "${it.currency} ${it.paidUsingWallet}"
            binding.tvDriverName.text = it.customerName ?: ""
        }

        binding.tvSubmit.setOnSingleClickListener {
            if (rideData != null)
                viewModel.rateCustomer(JSONObject().apply {
                    put("customer_id", rideData?.customerId)
                    put("engagement_id", rideData?.tripId)
                    put("given_rating", binding.ratingBar.rating.toInt().toString())
                })
        }
        binding.ivClose.setOnSingleClickListener { finish() }
        binding.tvNeedHelp.setOnSingleClickListener {
            if (rideData != null)
                startActivity(
                    Intent(
                        this@RateCustomerActivity,
                        NeedHelpActivity::class.java
                    ).putExtra("rideData", rideData)
                )
        }
        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            val text = when (rating.roundToInt()) {
                1 -> "Worst"
                2 -> "Bad"
                3 -> "Good"
                4 -> "Better"
                5 -> "Best"
                else -> ""
            }
            if (rating.roundToInt() == 0) {
                binding.ratingBar.rating = 1f
                return@setOnRatingBarChangeListener
            }
            binding.tvRating.text = text
        }
        viewModel.rateCustomer.observeData(this, onLoading = {
            showProgressDialog()
        }, onError = {
            hideProgressDialog()
            showErrorMessage(this)
        }, onSuccess = {
            hideProgressDialog()
            showErrorMessage("Successfully rated customer")
            val intent = Intent(this@RateCustomerActivity, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        })
    }
}