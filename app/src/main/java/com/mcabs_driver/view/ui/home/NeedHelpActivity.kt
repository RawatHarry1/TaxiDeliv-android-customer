package com.mcabs_driver.view.ui.home


import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.mcabs_driver.R
import com.mcabs_driver.customClasses.singleClick.setOnSingleClickListener
import com.mcabs_driver.databinding.ActivityNeedHelpBinding
import com.mcabs_driver.firebaseSetup.NewRideNotificationDC
import com.mcabs_driver.model.api.observeData
import com.mcabs_driver.view.base.BaseActivity
import com.mcabs_driver.view.ui.home_drawer.HomeActivity
import com.mcabs_driver.viewmodel.RideViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject

@AndroidEntryPoint
class NeedHelpActivity : BaseActivity<ActivityNeedHelpBinding>() {
    lateinit var binding: ActivityNeedHelpBinding
    private val viewModel by viewModels<RideViewModel>()
    private val rideData by lazy { intent.getParcelableExtra<NewRideNotificationDC>("rideData") }

    override fun getLayoutId(): Int {
        return R.layout.activity_need_help
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        binding.ivBack.setOnSingleClickListener { finish() }
        binding.tvSubmit.setOnSingleClickListener {
            if (binding.etFeedback.text.toString().trim().isEmpty())
                showErrorMessage("Please enter your feedback")
            else {
                viewModel.generateSupportTicket(JSONObject().apply {
                    put("engagement_id", rideData?.tripId)
                    put("support_feedback_text", binding.etFeedback.text.toString())
                    put("ticket_type", "")
                    put("ride_date", "")
                })
            }
        }

        viewModel.generateTicket.observeData(this, onLoading = {
            showProgressDialog()
        }, onError = {
            hideProgressDialog()
            showErrorMessage(this)
        }, onSuccess = {
            hideProgressDialog()
            showErrorMessage("Issue has been submitted successfully")
            val intent = Intent(this@NeedHelpActivity, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        })
    }


}