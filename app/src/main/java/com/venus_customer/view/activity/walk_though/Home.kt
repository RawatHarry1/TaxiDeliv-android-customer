package com.venus_customer.view.activity.walk_though

import android.Manifest
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mukesh.photopicker.utils.checkPermissions
import com.venus_customer.R
import com.venus_customer.databinding.ActivityHomeBinding
import com.venus_customer.model.api.observeData
import com.venus_customer.util.SharedPreferencesManager
import com.venus_customer.util.gone
import com.venus_customer.util.safeCall
import com.venus_customer.util.visible
import com.venus_customer.view.base.BaseActivity
import com.venus_customer.viewmodel.HomeVM
import com.venus_customer.viewmodel.rideVM.RideVM
import com.venus_customer.viewmodel.rideVM.RideVM.RideAlertUiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Home : BaseActivity<ActivityHomeBinding>() {

    lateinit var binding: ActivityHomeBinding
    private lateinit var navGraph: NavGraph
    private lateinit var navController: NavController
    private val viewModel by viewModels<HomeVM>()
    private val rideVM by viewModels<RideVM>()

    override fun getLayoutId(): Int {
        return R.layout.activity_home
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = getViewDataBinding()

        val navView: BottomNavigationView = binding.navView
        val navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_home) as NavHostFragment)

        val graphInflater = navHostFragment.navController.navInflater
        navGraph = graphInflater.inflate(R.navigation.mobile_navigation)
        navController = navHostFragment.navController

//        val destination: Int = R.id.navigation_home
//        navGraph.setStartDestination(destination)
//        navController.setGraph(navGraph, null)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_trips,
                R.id.navigation_notifications,
                R.id.navigation_account
            )
        )
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener(listener)

        observeData()
        observeUiState()
    }

    private val listener =
        NavController.OnDestinationChangedListener { controller, destination, arguments ->
            safeCall {
                if (destination.id == R.id.navigation_home || destination.id == R.id.navigation_account || destination.id == R.id.navigation_trips || destination.id == R.id.navigation_notifications)
                    binding.navView.visible()
                else
                    binding.navView.gone()
//                rideVM.updateUiState(RideAlertUiState.HomeScreen)
            }
        }


    override fun onResume() {
        super.onResume()
        safeCall {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkPermissions(Manifest.permission.POST_NOTIFICATIONS) {}
            }
            viewModel.loginViaToken()
        }
    }


    fun removeTint(source: Bitmap): Bitmap {
        val copy = source.copy(source.getConfig(), true)
        copy.setHasAlpha(true)
        return copy
    }


    private fun observeData() = viewModel.loginViaToken.observeData(this, onLoading = {
//        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        SharedPreferencesManager.putModel(SharedPreferencesManager.Keys.USER_DATA, this)
    }, onError = {
        hideProgressDialog()
        showToastShort(this)
    })

    private fun observeUiState() = rideVM.hideHomeNavigation.observe(this) {
        Log.i("RIDESTATE", "$it")
        if (it)
            binding.navView.gone()
        else
            binding.navView.visible()
    }
}