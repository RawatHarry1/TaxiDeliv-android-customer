package com.ujeff_customer.view.activity.walk_though

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.ujeff_customer.R
import com.ujeff_customer.customClasses.singleClick.setOnSingleClickListener
import com.ujeff_customer.databinding.ActivityWalkthorughBinding
import com.ujeff_customer.util.SharedPreferencesManager
import com.ujeff_customer.view.activity.SignUpInActivity

class WalkThrough : AppCompatActivity() {

    private lateinit var binding: ActivityWalkthorughBinding
    private val adapter by lazy { WalkAdapter() }

    private val pageChangeListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            binding.tvSalonNext.text = if (position == (adapter.listData.size - 1)) {
                getString(R.string.get_started)
            } else {
                getString(R.string.txt_next)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val walkViewModel: WalkThroughViewModel by viewModels()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_walkthorugh)
        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_color)

        // Optional: Adjust the status bar text color to ensure readability
        // Use dark or light content based on the background color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        binding.lifecycleOwner = this
        binding.walkViewModel = walkViewModel
        setWalkAdapter()
        binding.tvNext.setOnClickListener { nextClick() }
        binding.tvSalonNext.setOnClickListener { nextClick() }
        binding.tvSkip.setOnSingleClickListener { skipClick() }
        binding.tvSalonSkip.setOnSingleClickListener { skipClick() }
        SharedPreferencesManager.put(SharedPreferencesManager.Keys.WALKTHROUGH, true)
    }

    private fun setWalkAdapter() {
        binding.vpWalk.adapter = adapter
        val listData = mutableListOf<WalkData>()
//        if (BuildConfig.APPLICATION_ID == VENUS_PACKAGE_NAME) {
            binding.tvNext.isVisible = false
            binding.dotsIndicator.isVisible = false
            binding.tvSkip.isVisible = false
            binding.clBottom.isVisible = true
            binding.tvSalonSkip.isVisible = true
            listData.add(
                WalkData(
                    getString(R.string.empty_string),
                    getString(R.string.empty_string),
                    ContextCompat.getDrawable(this, R.drawable.venus_intro_1)
                )
            )
            listData.add(
                WalkData(
                    getString(R.string.empty_string),
                    getString(R.string.empty_string),
                    ContextCompat.getDrawable(this, R.drawable.venus_intro_2)
                )
            )
            listData.add(
                WalkData(
                    getString(R.string.empty_string),
                    getString(R.string.empty_string),
                    ContextCompat.getDrawable(this, R.drawable.venus_intro_3)
                )
            )
//            listData.add(WalkData(getString(R.string.empty_string),getString(R.string.empty_string),ContextCompat.getDrawable(this,R.drawable.venus_intro_4)))
//        } else {
//            binding.tvNext.isVisible = true
//            binding.dotsIndicator.isVisible = true
//            binding.tvSkip.isVisible = true
//            binding.clBottom.isVisible = false
//            binding.tvSalonSkip.isVisible = false
//            listData.add(
//                WalkData(
//                    getString(R.string.txt_join_the_green),
//                    getString(R.string.txt_volt_exclusively_for_drivers),
//                    ContextCompat.getDrawable(this, R.drawable.ic_hand_phone)
//                )
//            )
//            listData.add(
//                WalkData(
//                    getString(R.string.txt_available_everywhere),
//                    getString(R.string.txt_set_your_own_hours_area),
//                    ContextCompat.getDrawable(this, R.drawable.ic_location)
//                )
//            )
//            listData.add(
//                WalkData(
//                    getString(R.string.txt_highest_earnings),
//                    getString(R.string.txt_earn_100_of_fare),
//                    ContextCompat.getDrawable(this, R.drawable.ic_cash)
//                )
//            )
//        }
        adapter.submitList(listData)
    }

    private fun skipClick() {
        startActivity(Intent(this, SignUpInActivity::class.java))
    }

    private fun nextClick() {
        if (binding.vpWalk.currentItem < (adapter.listData.size - 1)) {
            binding.vpWalk.setCurrentItem(binding.vpWalk.currentItem + 1, true)
        } else {
            skipClick()
        }
    }


    override fun onResume() {
        super.onResume()
        binding.vpWalk.registerOnPageChangeCallback(pageChangeListener)
    }


    override fun onPause() {
        super.onPause()
        binding.vpWalk.unregisterOnPageChangeCallback(pageChangeListener)
    }


    data class WalkData(var title: String = "", var subTitle: String = "", var drawable: Drawable?)

}