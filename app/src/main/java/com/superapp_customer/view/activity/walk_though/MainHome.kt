package com.superapp_customer.view.activity.walk_though

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.superapp_customer.R
import com.superapp_customer.databinding.ActivityMainHomeBinding
import com.superapp_customer.dialogs.DialogUtils
import com.superapp_customer.util.gone
import com.superapp_customer.util.safeCall
import com.superapp_customer.util.visible
import com.superapp_customer.view.base.BaseActivity
import com.superapp_customer.viewmodel.HomeVM
import com.superapp_customer.viewmodel.rideVM.RideVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainHome : BaseActivity<ActivityMainHomeBinding>() {
    lateinit var binding: ActivityMainHomeBinding
    private lateinit var navGraph: NavGraph
    private lateinit var navController: NavController
    private val viewModel by viewModels<HomeVM>()
    private val rideVM by viewModels<RideVM>()
    private var hideNavView = false
    private lateinit var overlayPermissionLauncher: ActivityResultLauncher<Intent>

    companion object {
        var isFromMsgNotification = false
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_main_home
    }

    private val listener =
        NavController.OnDestinationChangedListener { controller, destination, arguments ->
            safeCall {
                if (destination.id == R.id.navigation_home || destination.id == R.id.navigation_account || destination.id == R.id.navigation_trips || destination.id == R.id.navigation_notifications || destination.id == R.id.navigate_services) {
                    binding.navView.visible()
                } else
                    binding.navView.gone()
            }
//            if (destination.id == R.id.navigation_ride)
//                SharedPreferencesManager.put(
//                    SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID,
//                    1
//                )
//            else if (destination.id == R.id.navigation_delivery)
//                SharedPreferencesManager.put(
//                    SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID,
//                    2
//                )

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        val navView: BottomNavigationView = binding.navView
        val navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_home_main) as NavHostFragment)

        val graphInflater = navHostFragment.navController.navInflater
        navGraph = graphInflater.inflate(R.navigation.main_mobile_navigation)
        navController = navHostFragment.navController
        navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener(listener)
//        binding.navView.menu.findItem(R.id.navigation_ride).isVisible = false

        // Register back press callback
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController.currentDestination?.id == R.id.navigation_home ) {
                    DialogUtils.getNegativeDialog(
                        this@MainHome,
                        "Yes",
                        getString(R.string.are_you_sure_you_want_to_exist_the_app),
                        ::onDialogClick
                    )
                }
                else
                {
                    // Behave as normal back press
                    isEnabled = false // Disable this callback to let the back press continue
                    onBackPressedDispatcher.onBackPressed() // Trigger normal back press behavior
                    isEnabled = true // Re-enable the callback
                }
            }
        })

    }

    private fun onDialogClick(position: Int) {
        finish()
    }

}