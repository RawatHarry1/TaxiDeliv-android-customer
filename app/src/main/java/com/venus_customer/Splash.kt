package com.venus_customer

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.google.android.gms.maps.model.LatLng
import com.venus_customer.customClasses.LocationResultHandler
import com.venus_customer.customClasses.SingleFusedLocation
import com.venus_customer.databinding.ActivitySplashBinding
import com.venus_customer.model.api.observeData
import com.venus_customer.model.dataClass.userData.UserDataDC
import com.venus_customer.util.SharedPreferencesManager
import com.venus_customer.view.activity.SignUpInActivity
import com.venus_customer.view.activity.walk_though.Home
import com.venus_customer.view.activity.walk_though.WalkThrough
import com.venus_customer.view.base.BaseActivity
import com.venus_customer.viewmodel.base.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class Splash : BaseActivity<ActivitySplashBinding>() {

    private val mViewModel by viewModels<SplashViewModel>()
    lateinit var binding: ActivitySplashBinding
    override fun getLayoutId(): Int {
        return R.layout.activity_splash
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        setObservers()
        SingleFusedLocation.initialize(VenusApp.appContext, object :
            LocationResultHandler {
            override fun updatedLocation(location: Location) {
                Log.i("CurrentLocation", "OnVenusApp")
                VenusApp.latLng = LatLng(location.latitude, location.longitude)
//                Handler(Looper.getMainLooper()).postDelayed({
                callGetOperatorToken()
//                }, 2000)
            }
        })
    }

    private fun callGetOperatorToken() {
        mViewModel.fetchOperatorToken()
    }


    private fun setObservers() {
        mViewModel.fetchTokenResponseLiveData.observeData(this,
            onLoading = {
                showProgressDialog()
            }, onSuccess = {
                hideProgressDialog()
                SharedPreferencesManager.putModel(SharedPreferencesManager.Keys.CLIENT_CONFIG, this)
                callWalkThrough()
            }, onError = {
                hideProgressDialog()
                showToastLong(this)
            })
    }


    private fun callWalkThrough() {
        val userData =
            SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
        if (userData?.login?.isCustomerProfileComplete == 1) {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        } else {
            val walkThroughShown =
                SharedPreferencesManager.getBoolean(SharedPreferencesManager.Keys.WALKTHROUGH)
            val intent = if (walkThroughShown)
                Intent(this, SignUpInActivity::class.java)
            else
                Intent(this, WalkThrough::class.java)
            startActivity(intent)
        }
        finishAffinity()
    }
}