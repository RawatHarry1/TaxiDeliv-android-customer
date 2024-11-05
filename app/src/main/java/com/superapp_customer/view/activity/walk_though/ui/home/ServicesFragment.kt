package com.superapp_customer.view.activity.walk_though.ui.home

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.superapp_customer.R
import com.superapp_customer.VenusApp
import com.superapp_customer.customClasses.singleClick.setOnSingleClickListener
import com.superapp_customer.databinding.FragmentServicesBinding
import com.superapp_customer.databinding.ItemBannersBinding
import com.superapp_customer.util.SharedPreferencesManager
import com.superapp_customer.view.activity.walk_though.Home
import com.superapp_customer.view.base.BaseFragment

class ServicesFragment : BaseFragment<FragmentServicesBinding>() {
    private lateinit var binding: FragmentServicesBinding
    private var currentPage = 0
    private val handler = Handler(Looper.getMainLooper())
    private val bannerArrayList = ArrayList<Drawable>()
    private lateinit var bannerAdapter: BannerAdapter
    private val slideRunnable = object : Runnable {
        override fun run() {
            currentPage = (currentPage + 1) % bannerArrayList.size
            binding.bannerViewPager.setCurrentItem(currentPage, true)
            handler.postDelayed(this, 3000) // 3 seconds
        }
    }

    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_services
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(slideRunnable)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        bannerArrayList.clear()
        ContextCompat.getDrawable(requireActivity(), R.drawable.service_banner_one)
            ?.let { bannerArrayList.add(it) }
        ContextCompat.getDrawable(requireActivity(), R.drawable.service_banner_two)
            ?.let { bannerArrayList.add(it) }
        bannerAdapter = BannerAdapter()
        binding.bannerViewPager.adapter = bannerAdapter
        binding.tabLayoutDots.setSelectedTabIndicatorColor(Color.WHITE)
        TabLayoutMediator(
            binding.tabLayoutDots, binding.bannerViewPager
        ) { tab, position ->
            val unselectedIcon =
                ContextCompat.getDrawable(requireContext(), R.drawable.banner_indicator_unselected)
            unselectedIcon?.setTintMode(PorterDuff.Mode.SRC_IN)
            tab.setIcon(unselectedIcon)
            tab.icon?.setTint(ContextCompat.getColor(requireContext(), R.color.white))
        }.attach()
        // Customize dot appearance based on selection
        binding.bannerViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                for (i in 0 until binding.tabLayoutDots.tabCount) {
                    val unselectedIcon = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.banner_indicator_unselected
                    )
                    unselectedIcon?.setTintMode(PorterDuff.Mode.SRC_IN)
                    val selectedIcon = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.banner_indicator_selected
                    )
                    selectedIcon?.setTintMode(PorterDuff.Mode.SRC_IN)
                    binding.tabLayoutDots.getTabAt(i)?.setIcon(
                        if (i == position) selectedIcon else unselectedIcon
                    )
                    binding.tabLayoutDots.getTabAt(i)?.icon?.setTint(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                }
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                for (i in 0 until binding.tabLayoutDots.tabCount) {
                    val unselectedIcon = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.banner_indicator_unselected
                    )
                    unselectedIcon?.setTintMode(PorterDuff.Mode.SRC_IN)
                    val selectedIcon = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.banner_indicator_selected
                    )
                    selectedIcon?.setTintMode(PorterDuff.Mode.SRC_IN)
                    binding.tabLayoutDots.getTabAt(i)?.setIcon(
                        if (i == position) selectedIcon else unselectedIcon
                    )
                    binding.tabLayoutDots.getTabAt(i)?.icon?.setTint(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                }
            }
        })
        handler.post(slideRunnable)


        binding.rlRide.setOnSingleClickListener {
            SharedPreferencesManager.put(
                SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID,
                1
            )

            VenusApp.isServiceTypeDefault = false
            startActivity(Intent(requireActivity(), Home::class.java))
        }
        binding.rlRideSchedule.setOnSingleClickListener {
            SharedPreferencesManager.put(
                SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID,
                1
            )

            VenusApp.isServiceTypeDefault = false
            startActivity(Intent(requireActivity(), Home::class.java))
        }
        binding.rlDelivery.setOnSingleClickListener {
            SharedPreferencesManager.put(
                SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID,
                2
            )

            VenusApp.isServiceTypeDefault = false
            startActivity(Intent(requireActivity(), Home::class.java))
        }
        binding.rlDeliverySchedule.setOnSingleClickListener {
            SharedPreferencesManager.put(
                SharedPreferencesManager.Keys.SELECTED_OPERATOR_ID,
                2
            )

            VenusApp.isServiceTypeDefault = false
            startActivity(Intent(requireActivity(), Home::class.java))
        }

    }

    inner class BannerAdapter : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

        inner class BannerViewHolder(private val binding: ItemBannersBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(banners: Drawable) {
                Glide.with(requireActivity()).load(banners)
                    .error(R.drawable.ic_banner_image_new).into(binding.ivBannerImage)

//                binding.ivBannerImage.setOnClickListener {
//                    if (!banners.actionUrl.isNullOrEmpty())
//                        safeCall {
//                            CustomTabsIntent.Builder().build()
//                                .launchUrl(requireContext(), Uri.parse(banners.actionUrl))
//                        }
//                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
            return BannerViewHolder(ItemBannersBinding.inflate(layoutInflater, parent, false))
        }

        override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
            holder.bind(bannerArrayList[position])
        }

        override fun getItemCount(): Int = bannerArrayList.size
    }
}