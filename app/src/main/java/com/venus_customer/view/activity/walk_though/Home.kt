package com.venus_customer.view.activity.walk_though

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.mukesh.photopicker.utils.checkPermissions
import com.venus_customer.R
import com.venus_customer.customClasses.FloatingIconService
import com.venus_customer.customClasses.FloatingOverlayService
import com.venus_customer.databinding.ActivityHomeBinding
import com.venus_customer.dialogs.DialogUtils
import com.venus_customer.model.api.observeData
import com.venus_customer.util.SharedPreferencesManager
import com.venus_customer.util.gone
import com.venus_customer.util.safeCall
import com.venus_customer.util.visible
import com.venus_customer.view.base.BaseActivity
import com.venus_customer.viewmodel.HomeVM
import com.venus_customer.viewmodel.rideVM.RideVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Home : BaseActivity<ActivityHomeBinding>() {

    lateinit var binding: ActivityHomeBinding
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
        handleIntent(intent)


//        overlayPermissionLauncher = registerForActivityResult(
//            ActivityResultContracts.StartActivityForResult()
//        ) {
//            if (Settings.canDrawOverlays(this)) {
//                startFloatingIconService()
//            } else {
//                // Permission is not granted. Show a message to the user.
//                Toast.makeText(this, "Overlay permission is required", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        // Check and request permission
//        if (!Settings.canDrawOverlays(this)) {
//            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
//            overlayPermissionLauncher.launch(intent)
//        } else {
//            startFloatingIconService()
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                offerAppliedBroadCast,
                IntentFilter("offer"), Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(
                offerAppliedBroadCast,
                IntentFilter("offer")
            )
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(offerAppliedBroadCast)
    }

    private fun startFloatingIconService() {
        Log.d("OverlayService", "Service called")
        val intent = Intent(this, FloatingIconService::class.java)
        startService(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private val listener =
        NavController.OnDestinationChangedListener { controller, destination, arguments ->
            safeCall {
                if (destination.id == R.id.navigation_home || destination.id == R.id.navigation_account || destination.id == R.id.navigation_trips || destination.id == R.id.navigation_notifications) {
                    binding.navView.visible()
                } else
                    binding.navView.gone()
//                rideVM.updateUiState(RideAlertUiState.HomeScreen)
            }
        }


    override fun onResume() {
        super.onResume()

        Log.i("ONRESUME", "HOME ${Gson().toJson(intent.extras)}")
        safeCall {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkPermissions(Manifest.permission.POST_NOTIFICATIONS) {}
            }
            viewModel.loginViaToken()
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (navController.handleDeepLink(intent)) {
            // The deep link was handled by the NavController
            intent?.extras?.let { bundle ->
                // Check if the deepLinkExtras exist and retrieve the bundle
                val deepLinkExtras =
                    bundle.getBundle("android-support-nav:controller:deepLinkExtras")
                deepLinkExtras?.let {
                    val notificationType = it.getString("notification_type")
                    // Handle the notification type here
                    Log.d("NotificationRedirection", "Notification Type: $notificationType")
                    if (notificationType == "600")
                        isFromMsgNotification = true
                }
            }
        } else {
            // The deep link was not handled
            intent?.extras?.let { bundle ->
                // Check if the deepLinkExtras exist and retrieve the bundle
                val deepLinkExtras =
                    bundle.getBundle("android-support-nav:controller:deepLinkExtras")
                deepLinkExtras?.let {
                    val notificationType = it.getString("notification_type")
                    // Handle the notification type here
                    Log.d("NotificationRedirection", "Notification Type: $notificationType")
                    if (notificationType == "600")
                        isFromMsgNotification = true
                }
            }
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
        if (this?.login?.popup != null) {
            if (this.login.popup.is_force != null
                && this.login.popup.popup_text != null
                && this.login.popup.download_link != null
            ) {
                DialogUtils.getVersionUpdateDialog(
                    this@Home,
                    this.login.popup.is_force,
                    this.login.popup.popup_text,
                    this.login.popup.download_link,
                    ::onDialogClick
                )
            }
        }
    }, onError = {
        hideProgressDialog()
        showToastShort(this)
    })

    private fun onDialogClick(promoCode: String) {

    }

    private fun observeUiState() = rideVM.hideHomeNavigation.observe(this) {
        Log.i("RIDESTATE", "$it")
        if (it) {
            hideNavView = true
            binding.navView.gone()
        } else {
            hideNavView = false
            binding.navView.visible()
        }
    }

    private val offerAppliedBroadCast = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            navigateToHome()
        }
    }

    fun navigateToHome() {
        binding.navView.selectedItemId = R.id.navigation_home
        navController.navigate(R.id.navigation_home)
    }
}