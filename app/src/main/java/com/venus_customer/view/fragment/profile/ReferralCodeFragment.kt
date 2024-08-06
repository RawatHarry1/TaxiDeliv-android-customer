package com.venus_customer.view.fragment.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.venus_customer.R
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.FragmentReferralCodeBinding
import com.venus_customer.model.dataClass.userData.UserDataDC
import com.venus_customer.util.SharedPreferencesManager
import com.venus_customer.view.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReferralCodeFragment : BaseFragment<FragmentReferralCodeBinding>() {
    lateinit var binding: FragmentReferralCodeBinding
    private var referralCode = ""
    private var referralLink = ""

    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_referral_code
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
            ?.let {
                Glide.with(view.context).load(it.login?.referralData?.referral_image_d2c)
                    .error(R.drawable.referral_image).into(binding.ivReferral)
                referralCode = it.login?.referralCode ?: ""
                referralLink = it.login?.referralLink ?: ""
                binding.tvReferralCode.text = referralCode
                binding.tvReferralMsg.text = it.login?.referralData?.referral_message ?: ""
            }
        binding.ivBack.setOnSingleClickListener {
            findNavController().popBackStack()
        }
        binding.tvReferral.setOnSingleClickListener {
            shareInvite()
        }
        binding.ivCopyReferral.setOnClickListener { copyTextToClipboard(referralCode) }
        binding.tvReferralCode.setOnClickListener { copyTextToClipboard(referralCode) }
    }

    fun shareInvite() {
        val inviteMessage = """
        Hey, join ${resources.getString(R.string.app_name)} app using this referral code: $referralCode.
        Click here to download the app: $referralLink
    """.trimIndent()

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, inviteMessage)
            type = "text/plain"
        }

        startActivity(Intent.createChooser(shareIntent, "Share invitation via"))
    }

    fun copyTextToClipboard(text: String) {
        val clipboard = requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Referral code: ", text)
        clipboard.setPrimaryClip(clip)
        showToastShort("Referral code copied")
    }
}