package com.salonedriver.view.ui.home_drawer

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.mukesh.photopicker.utils.checkPermissions
import com.salonedriver.R
import com.salonedriver.customClasses.LocationResultHandler
import com.salonedriver.customClasses.SingleFusedLocation
import com.salonedriver.databinding.ActivityHomeBinding
import com.salonedriver.model.api.observeData
import com.salonedriver.model.dataclassses.userData.UserDataDC
import com.salonedriver.util.DriverDocumentStatusForApp
import com.salonedriver.util.SharedPreferencesManager
import com.salonedriver.util.commonToast
import com.salonedriver.util.logoutAlert
import com.salonedriver.view.base.BaseActivity
import com.salonedriver.view.ui.SignUpInActivity
import com.salonedriver.viewmodel.UserAccountVM
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding
    private lateinit var drawerLayout: DrawerLayout
    private val navController by lazy { findNavController(R.id.nav_host_fragment_content_home) }
    private val userVM by viewModels<UserAccountVM>()

    var onlineDriver = false
    override fun getLayoutId(): Int {
        return R.layout.activity_home
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
        observeVehicleData()
        addDrawerListener()
    }


    private fun addDrawerListener(){
//        binding.drawerLayout.setScrimColor(Color.TRANSPARENT)
//        binding.drawerLayout.setDrawerShadow(ColorDrawable(Color.TRANSPARENT), GravityCompat.END)
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    private fun drawerLayoutCallback(){
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener{
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                binding.content.x = binding.navView.width * slideOffset
                val lp: LayoutParams = binding.content.layoutParams as LayoutParams
                if (slideOffset == 0f){
                    lp.height = LayoutParams.MATCH_PARENT
                    lp.topMargin =0
                } else {
                    lp.height = binding.drawerLayout.height - (binding.drawerLayout.height * slideOffset * 0.3f).toInt()
                    lp.topMargin = (binding.drawerLayout.height - lp.height) / 2
                }
                binding.content.layoutParams = lp
            }

            override fun onDrawerOpened(p0: View) {
                binding.holder.radius = 25f
                if (SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)?.login?.driverDocumentStatus?.requiredDocStatus.orEmpty() == DriverDocumentStatusForApp.REJECTED.type){
                    binding.tvDocuments.setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.red_text_color))
                    binding.tvDocuments.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_new_info,0)
                }else{
                    binding.tvDocuments.setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.black))
                    binding.tvDocuments.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0)
                }
            }

            override fun onDrawerClosed(p0: View) {
                binding.holder.radius = 0f
            }

            override fun onDrawerStateChanged(p0: Int) {
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermissions(Manifest.permission.POST_NOTIFICATIONS){}
        }
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
        binding.llLogOut.setOnClickListener {
            drawerLayout.close()
            logoutAlert {
                userVM.logout()
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
        getLocationResult()
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
        if (this?.docStatus.orEmpty() == "APPROVED") navController.navigate(R.id.nav_vehicle_listing, bundleOf("vehicleData" to this))
        else commonToast(message = getString(R.string.your_documents_are_not_approved_from_admin))
    })
}