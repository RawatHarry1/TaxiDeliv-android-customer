package com.mcabs_driver.view.ui

import android.content.Intent
import android.os.Bundle
import com.mcabs_driver.R
import com.mcabs_driver.databinding.ActivityCancelTripBinding
import com.mcabs_driver.dialogs.DialogUtils
import com.mcabs_driver.view.base.BaseActivity
import com.mcabs_driver.view.ui.home_drawer.HomeActivity

class CancelTripActivity : BaseActivity<ActivityCancelTripBinding>() {


    lateinit var binding: ActivityCancelTripBinding
    override fun getLayoutId(): Int {
        return R.layout.activity_cancel_trip
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.btnCancel.setOnClickListener {
            DialogUtils.getNegativeDialog(
                this, getString(R.string.cancel_ride),
                getString(R.string.cancel_the_ride_request), onClickDialog
            )
        }

        binding.btnNo.setOnClickListener {
            finish()
        }

    }

    private val onClickDialog = { _: Int ->
        startActivity(Intent(this, HomeActivity::class.java))
        finishAffinity()
    }

}