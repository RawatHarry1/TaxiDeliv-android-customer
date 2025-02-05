package com.mb_driver.view.ui.home_drawer

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout.LayoutParams
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.mb_driver.R
import com.mb_driver.SaloneDriver
import com.mb_driver.customClasses.LocationResultHandler
import com.mb_driver.customClasses.SingleFusedLocation
import com.mb_driver.databinding.ActivityHomeBinding
import com.mb_driver.dialogs.DialogUtils
import com.mb_driver.firebaseSetup.SoundService
import com.mb_driver.model.api.observeData
import com.mb_driver.model.dataclassses.clientConfig.ClientConfigDC
import com.mb_driver.model.dataclassses.userData.UserDataDC
import com.mb_driver.util.DriverDocumentStatusForApp
import com.mb_driver.util.SharedPreferencesManager
import com.mb_driver.util.commonToast
import com.mb_driver.util.deleteAlert
import com.mb_driver.util.logoutAlert
import com.mb_driver.view.base.BaseActivity
import com.mb_driver.view.ui.PaymentActivity
import com.mb_driver.view.ui.SignUpInActivity
import com.mb_driver.viewmodel.UserAccountVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding
    private lateinit var drawerLayout: DrawerLayout
    private val navController by lazy { findNavController(R.id.nav_host_fragment_content_home) }
    private val userVM by viewModels<UserAccountVM>()
    var onlineDriver = false

    companion object {
        var isMsgNotification = false
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_home
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
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
                        isMsgNotification = true
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
                        isMsgNotification = true
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        setSupportActionBar(binding.appBarHome.toolbar)
        drawerLayout = binding.drawerLayout
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        drawerLayoutCallback()
        clickHandler()
        observeLogout()
        observeDeleteAccount()
        observeVehicleData()
        addDrawerListener()
        startRepeatingJob()
        handleIntent(intent)

        // Register back press callback
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController.currentDestination?.id == R.id.nav_home) {
                    DialogUtils.getNegativeDialog(
                        this@HomeActivity,
                        "Yes",
                        getString(R.string.are_you_sure_you_want_to_exist_the_app),
                        ::onDialogClick
                    )
                } else {
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

    private fun startRepeatingJob() {
        lifecycleScope.launch {
            while (true) {
                withContext(Dispatchers.IO) {
                    if (SaloneDriver.latLng != null && SaloneDriver.latLng?.latitude != 0.0) {
                        userVM.updateDriverLocation()
                    }
                }
                try {
                    val clientConfig =
                        SharedPreferencesManager.getModel<ClientConfigDC>(SharedPreferencesManager.Keys.CLIENT_CONFIG)
                    val timer = clientConfig?.updateLocationTimer ?: 0f
                    if (timer != 0f)
                        delay((timer * 1000).toLong())
                    else
                        delay(15000)
                } catch (e: Exception) {
                    delay(15000) // 15 seconds
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.coroutineContext.cancelChildren() // Stop the job when the activity is destroyed
    }

    private fun addDrawerListener() {
//        binding.drawerLayout.setScrimColor(Color.TRANSPARENT)
//        binding.drawerLayout.setDrawerShadow(ColorDrawable(Color.TRANSPARENT), GravityCompat.END)
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    private fun drawerLayoutCallback() {
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                binding.content.x = binding.navView.width * slideOffset
                val lp: LayoutParams = binding.content.layoutParams as LayoutParams
                if (slideOffset == 0f) {
                    lp.height = LayoutParams.MATCH_PARENT
                    lp.topMargin = 0
                } else {
                    lp.height =
                        binding.drawerLayout.height - (binding.drawerLayout.height * slideOffset * 0.3f).toInt()
                    lp.topMargin = (binding.drawerLayout.height - lp.height) / 2
                }
                binding.content.layoutParams = lp
            }

            override fun onDrawerOpened(p0: View) {
                binding.holder.radius = 25f
                if (SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)?.login?.driverDocumentStatus?.requiredDocStatus.orEmpty() == DriverDocumentStatusForApp.REJECTED.type) {
                    binding.tvDocuments.setTextColor(
                        ContextCompat.getColor(
                            this@HomeActivity,
                            R.color.red_text_color
                        )
                    )
                    binding.tvDocuments.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_new_info,
                        0
                    )
                } else {
                    binding.tvDocuments.setTextColor(
                        ContextCompat.getColor(
                            this@HomeActivity,
                            R.color.black
                        )
                    )
                    binding.tvDocuments.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                }
            }

            override fun onDrawerClosed(p0: View) {
                binding.holder.radius = 0f
            }

            override fun onDrawerStateChanged(p0: Int) {
            }
        })
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            checkPermissions(Manifest.permission.POST_NOTIFICATIONS) {}
//        }
    }


    private fun clickHandler() {
        binding.llHome.setOnClickListener {
            drawerLayout.close()
            navController.navigate(R.id.nav_home)
        }
        binding.llAccount.setOnClickListener {
            drawerLayout.close()
            navController.navigate(R.id.nav_account)

        }
        binding.llEarnings.setOnClickListener {
            drawerLayout.close()
            navController.navigate(R.id.nav_earnings, bundleOf("isBookingTab" to false))
        }
        binding.llCards.setOnClickListener {
            drawerLayout.close()
            startActivity(Intent(this, PaymentActivity::class.java))
        }
        binding.llBooking.setOnClickListener {
            drawerLayout.close()
            navController.navigate(R.id.nav_earnings, bundleOf("isBookingTab" to true))

        }
        binding.llDocuments.setOnClickListener {
            navController.navigate(R.id.nav_documents)
            drawerLayout.close()
        }
        binding.llRatings.setOnClickListener {
            navController.navigate(R.id.nav_ratings)
            drawerLayout.close()
        }
        binding.llNotifications.setOnClickListener {
            navController.navigate(R.id.nav_notifications)
            drawerLayout.close()
        }

        binding.llVehicleList.setOnClickListener {
            userVM.vehicleListData()
        }
        binding.llAboutApp.setOnClickListener {
            navController.navigate(R.id.nav_about_app)
            drawerLayout.close()
        }
        binding.llWallet.setOnClickListener {
            navController.navigate(R.id.wallet)
            drawerLayout.close()
        }
        binding.llTicketList.setOnClickListener {
            navController.navigate(R.id.raisedTicketsListFragment)
            drawerLayout.close()
        }
        binding.llLogOut.setOnClickListener {
            drawerLayout.close()
            logoutAlert {
                stopService(Intent(this, SoundService::class.java))
                userVM.logout()
            }
        }
        binding.llDeleteAccount.setOnClickListener {
            drawerLayout.close()
            deleteAlert {
                stopService(Intent(this, SoundService::class.java))
                userVM.deleteAccount()
            }
        }
    }


    fun openDrawer() {
        drawerLayout.open()

    }

    fun closeDrawer() {
        drawerLayout.close()

    }


    override fun onResume() {
        super.onResume()
//        getLocationResult()
    }


    /**
     * Observe Logout
     * */
    private fun observeLogout() = userVM.logout.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        SharedPreferencesManager.clearKeyData(SharedPreferencesManager.Keys.USER_DATA)
        startActivity(Intent(this@HomeActivity, SignUpInActivity::class.java))
        finishAffinity()
    }, onError = {
        hideProgressDialog()
        showToastShort(this)
    })

    private fun observeDeleteAccount() = userVM.deleteAccount.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        SharedPreferencesManager.clearKeyData(SharedPreferencesManager.Keys.USER_DATA)
        startActivity(Intent(this@HomeActivity, SignUpInActivity::class.java))
        finishAffinity()
    }, onError = {
        hideProgressDialog()
        showToastShort(this)
    })


    private fun getLocationResult() {
        SingleFusedLocation.initialize(this, object : LocationResultHandler {
            override fun updatedLocation(location: Location) {

            }
        })
    }

    private fun observeVehicleData() = userVM.vehicleListData.observeData(this, onLoading = {
        showProgressDialog()
    }, onError = {
        hideProgressDialog()
        showToastLong(this)
        drawerLayout.close()
    }, onSuccess = {
        hideProgressDialog()
        drawerLayout.close()
        if (this?.docStatus.orEmpty() == "APPROVED") navController.navigate(
            R.id.nav_vehicle_listing,
            bundleOf("vehicleData" to this)
        )
        else commonToast(message = getString(R.string.your_documents_are_not_approved_from_admin))
    })
}