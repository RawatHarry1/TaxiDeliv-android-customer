package com.salonedriver.view.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.salonedriver.R
import com.salonedriver.SaloneDriver
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
            SaloneDriver.googleMapKey = this?.googleMapKey.orEmpty()
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
        val walkThroughShown =
            SharedPreferencesManager.getBoolean(SharedPreferencesManager.Keys.WALKTHROUGH)
        SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
            ?.let {
                if ((it.login?.registrationStepCompleted?.isVehicleInfoCompleted == true) && (it.accessToken?.isNotEmpty() == true)) {
                    changeIntent(HomeActivity::class.java)
                } else {
                    if (walkThroughShown)
                        changeIntent(SignUpInActivity::class.java)
                    else
                        changeIntent(WalkThrough::class.java)
                }
            } ?: run {
            if (walkThroughShown)
                changeIntent(SignUpInActivity::class.java)
            else
                changeIntent(WalkThrough::class.java)
        }
    }


    private fun changeIntent(className: Class<*>) {
        startActivity(Intent(this, className))
        finish()
    }
}