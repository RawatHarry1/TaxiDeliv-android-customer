package com.venus_customer.view.fragment.feedback

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.venus_customer.R
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.FragmentFeedBaclBinding
import com.venus_customer.model.api.observeData
import com.venus_customer.util.showSnackBar
import com.venus_customer.view.base.BaseFragment
import com.venus_customer.viewmodel.rideVM.RideVM
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
            findNavController().popBackStack()
        }
        binding.tvSubmit.setOnSingleClickListener {
            viewModel.rateDriver(
                engagementId = navArgs.engagementId,
                feedback = binding.etFeedback.text.toString(),
                rating = binding.ratingBar.rating.toInt().toString()
            )
        }

        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            val text = when(rating.roundToInt()){
                1 -> "Worst"
                2 -> "Bad"
                3 -> "Good"
                4 -> "Better"
                5 -> "Best"
                else -> ""
            }
            if (rating.roundToInt() == 0){
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
            findNavController().popBackStack()
        }
    )

}