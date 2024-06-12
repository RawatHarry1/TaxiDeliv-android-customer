package com.salonedriver.view.ui

import android.content.Intent
import android.location.Location
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.salonedriver.R
import com.salonedriver.customClasses.LocationResultHandler
import com.salonedriver.customClasses.SingleFusedLocation
import com.salonedriver.databinding.ActivitySplashBinding
import com.salonedriver.model.api.observeData
import com.salonedriver.model.dataclassses.userData.UserDataDC
import com.salonedriver.util.SharedPreferencesManager
import com.salonedriver.util.gone
import com.salonedriver.util.visible
import com.salonedriver.view.base.BaseActivity
import com.salonedriver.view.ui.home_drawer.HomeActivity
import com.salonedriver.viewmodel.SplashVM
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class Splash : BaseActivity<ActivitySplashBinding>() {

    private lateinit var binding: ActivitySplashBinding
    private val viewModel by viewModels<SplashVM>()

    override fun getLayoutId(): Int {
        return R.layout.activity_splash
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        observerClientConfig()

        binding.btRetry.setOnClickListener {
            viewModel.getClientConfig()
        }
    }


    private fun observerClientConfig() =
        viewModel.clientConfig.observeData(lifecycle = this, onLoading = {
            binding.pbProgress.visible()
            binding.btRetry.gone()
        }, onSuccess = {
            binding.pbProgress.gone()
            binding.btRetry.gone()
            SharedPreferencesManager.putModel(SharedPreferencesManager.Keys.CLIENT_CONFIG, this)
            callWalkThrough()
        }, onError = {
            binding.pbProgress.gone()
            binding.btRetry.visible()
            showErrorMessage(this)
        })


    /**
     * Call Walkthrough
     * */
    private fun callWalkThrough() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 12)
        calendar.set(Calendar.MONTH, 5)
        if (System.currentTimeMillis() < calendar.timeInMillis){
            SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
                ?.let {
                    if ((it.login?.registrationStepCompleted?.isVehicleInfoCompleted == true) && (it.accessToken?.isNotEmpty() == true)) {
                        changeIntent(HomeActivity::class.java)
                    } else {
                        changeIntent(WalkThrough::class.java)
                    }
                } ?: run {
                changeIntent(WalkThrough::class.java)
            }
        }


    }


    private fun changeIntent(className: Class<*>) {
        startActivity(Intent(this, className))
        finish()
    }
}