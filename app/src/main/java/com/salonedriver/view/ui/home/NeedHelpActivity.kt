package com.salonedriver.view.ui.home


import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.salonedriver.R
import com.salonedriver.customClasses.singleClick.setOnSingleClickListener
import com.salonedriver.databinding.ActivityNeedHelpBinding
import com.salonedriver.firebaseSetup.NewRideNotificationDC
import com.salonedriver.model.api.observeData
import com.salonedriver.view.base.BaseActivity
import com.salonedriver.view.ui.home_drawer.HomeActivity
import com.salonedriver.viewmodel.RideViewModel
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