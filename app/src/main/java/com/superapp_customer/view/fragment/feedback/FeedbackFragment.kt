package com.superapp_customer.view.fragment.feedback

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.superapp_customer.R
import com.superapp_customer.customClasses.singleClick.setOnSingleClickListener
import com.superapp_customer.databinding.FragmentFeedBaclBinding
import com.superapp_customer.model.api.observeData
import com.superapp_customer.util.showSnackBar
import com.superapp_customer.view.base.BaseFragment
import com.superapp_customer.viewmodel.rideVM.RideVM
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class FeedbackFragment : BaseFragment<FragmentFeedBaclBinding>() {

    lateinit var binding: FragmentFeedBaclBinding
    private val viewModel by viewModels<RideVM>()
    private val navArgs by navArgs<FeedbackFragmentArgs>()

    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_feed_bacl
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        binding.tvDriverName.text = navArgs.driverName
    }

    override fun onResume() {
        super.onResume()
        setClicks()
        observeFeedbackData()
    }

    private fun setClicks() {
        binding.ivClose.setOnSingleClickListener {
            findNavController().popBackStack()
        }
        binding.tvSkip.setOnSingleClickListener {

            if (navArgs.tripId.isEmpty())
                findNavController().popBackStack()
            else
                requireView().findNavController().navigate(
                    R.id.navigation_ride_details, bundleOf(
                        "tripId" to navArgs.tripId,
                        "driverId" to navArgs.driverId
                    )
                )
        }
        binding.tvSubmit.setOnSingleClickListener {
            viewModel.rateDriver(
                engagementId = navArgs.engagementId,
                feedback = binding.etFeedback.text.toString().trim(),
                rating = binding.ratingBar.rating.toInt().toString()
            )
        }

        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            val text = when (rating.roundToInt()) {
                1 -> "Worst"
                2 -> "Bad"
                3 -> "Good"
                4 -> "Better"
                5 -> "Best"
                else -> ""
            }
            if (rating.roundToInt() == 0) {
                binding.ratingBar.rating = 1f
                return@setOnRatingBarChangeListener
            }
            binding.tvRating.text = text
        }
    }


    private fun observeFeedbackData() = viewModel.rateDriverData.observeData(
        lifecycle = viewLifecycleOwner,
        onLoading = {
            showProgressDialog()
        }, onError = {
            hideProgressDialog()
            showSnackBar(this)
        }, onSuccess = {
            hideProgressDialog()
            if (navArgs.tripId.isEmpty())
                findNavController().popBackStack()
            else
                requireView().findNavController().navigate(
                    R.id.navigation_ride_details, bundleOf(
                        "tripId" to navArgs.tripId,
                        "driverId" to navArgs.driverId
                    )
                )
        }
    )

}