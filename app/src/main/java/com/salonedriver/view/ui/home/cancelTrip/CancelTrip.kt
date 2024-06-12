package com.salonedriver.view.ui.home.cancelTrip

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.salonedriver.R
import com.salonedriver.databinding.CancelRideBinding
import com.salonedriver.model.api.observeData
import com.salonedriver.model.dataclassses.userData.UserDataDC
import com.salonedriver.util.SharedPreferencesManager
import com.salonedriver.view.base.BaseActivity
import com.salonedriver.view.base.BaseFragment
import com.salonedriver.view.ui.home_drawer.HomeActivity
import com.salonedriver.viewmodel.RideViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject

@AndroidEntryPoint
class CancelTrip: BaseActivity<CancelRideBinding>() {

    private lateinit var binding: CancelRideBinding
    private val cancelRideAdapter by lazy { CancelTripAdapter() }
    private val viewModel by viewModels<RideViewModel>()
    private val tripId by lazy { intent.getStringExtra("tripId").orEmpty() }


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
            val reason = cancelRideAdapter.getSelectedItemName() ?: binding.etReason.text.toString()
            if (reason.isEmpty()){
                showToastShort("*Please select or enter any reason.")
            } else {
                viewModel.cancelTrip(jsonObject = JSONObject().apply {
                    put("engagementId",tripId)
                    put("customerId",SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)?.login?.userId.orEmpty())
                    put("cancellationReason",reason)
                    put("by_operator",1)
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