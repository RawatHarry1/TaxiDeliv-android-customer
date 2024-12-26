package com.vyba_dri.view.ui.home.cancelTrip

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.vyba_dri.R
import com.vyba_dri.databinding.CancelRideBinding
import com.vyba_dri.model.api.observeData
import com.vyba_dri.model.dataclassses.userData.UserDataDC
import com.vyba_dri.util.SharedPreferencesManager
import com.vyba_dri.view.base.BaseActivity
import com.vyba_dri.view.ui.home_drawer.HomeActivity
import com.vyba_dri.viewmodel.RideViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject

@AndroidEntryPoint
class CancelTrip: BaseActivity<CancelRideBinding>() {

    private lateinit var binding: CancelRideBinding
    private val cancelRideAdapter by lazy { CancelTripAdapter() }
    private val viewModel by viewModels<RideViewModel>()
    private val tripId by lazy { intent.getStringExtra("tripId").orEmpty() }
    private val customerId by lazy { intent.getStringExtra("customerId").orEmpty() }


    override fun getLayoutId(): Int {
        return R.layout.cancel_ride
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        clickHandler()
        observeCancelTrip()
    }

    /**
     * Click Handler
     * */
    private fun clickHandler() {
        binding.rvReason.adapter = cancelRideAdapter
        cancelRideAdapter.submitList(SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)?.login?.cancellationReasons ?: emptyList())
        binding.tvNo.setOnClickListener {
            finish()
        }
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.tvCancelRide.setOnClickListener {
            val reason = cancelRideAdapter.getSelectedItemName() ?: binding.etReason.text.toString().trim()
            if (reason.isEmpty()){
                showToastShort("*Please select or enter any reason.")
            } else {
                viewModel.cancelTrip(jsonObject = JSONObject().apply {
                    put("engagementId",tripId)
                    put("customerId",customerId)
                    put("cancellationReason",reason)
                    put("by_operator",0)
                })
            }
        }
    }


    /**
     * Observe Cancel Trip
     * */
    private fun observeCancelTrip() = viewModel.cancelTrip.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        showToastLong(getString(R.string.ride_has_been_cancelled_by_you))
        Intent(this@CancelTrip, HomeActivity::class.java).apply {
            startActivity(this)
            finishAffinity()
        }
    }, onError = {
        hideProgressDialog()
        showErrorMessage(this)
    })

}