package com.vyba_dri.view.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.vyba_dri.R
import com.vyba_dri.customClasses.singleClick.setOnSingleClickListener
import com.vyba_dri.databinding.ActivityPayoutInformationBinding
import com.vyba_dri.model.api.observeData
import com.vyba_dri.model.dataclassses.clientConfig.ClientConfigDC
import com.vyba_dri.model.dataclassses.userData.UserDataDC
import com.vyba_dri.util.SharedPreferencesManager
import com.vyba_dri.util.getValue
import com.vyba_dri.view.base.BaseActivity
import com.vyba_dri.view.ui.home_drawer.HomeActivity
import com.vyba_dri.viewmodel.OnBoardingVM
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject

@AndroidEntryPoint
class PayoutInformation : BaseActivity<ActivityPayoutInformationBinding>() {


    lateinit var binding: ActivityPayoutInformationBinding
    private val viewModel by viewModels<OnBoardingVM>()
    override fun getLayoutId(): Int {
        return R.layout.activity_payout_information
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        clickHandler()
        observePaymentInfo()
    }


    private fun clickHandler() {
        binding.tvSubmitPayout.setOnClickListener {
//            if (binding.etBankName.getValue().isEmpty() && binding.etAccountNo.getValue()
//                    .isEmpty() && binding.etAccountName.getValue().isEmpty()
//            ) {
//
//            } else {
            if (validation()) {
                viewModel.payoutInfo(jsonObject = JSONObject().apply {
                    put("name", binding.etBankName.getValue())
                    put("iban", binding.etAccountNo.getValue())
                    put("bank_name", binding.etBankName.getValue())
                    put("mobile_wallet", binding.etMobile.getValue())
                })
//                }
            }
        }
        binding.tvSkip.setOnSingleClickListener {
            startActivity(Intent(this@PayoutInformation, HomeActivity::class.java))
            finishAffinity()
        }
        binding.ivBackElectric.setOnClickListener { finish() }
        SharedPreferencesManager.getModel<ClientConfigDC>(SharedPreferencesManager.Keys.CLIENT_CONFIG)
            ?.let {
                binding.tvSkip.isVisible =
                    it.mandatoryRegistrationSteps?.is_bank_details_mandatory != true
            }
    }


    private fun validation(): Boolean = when {

        binding.etBankName.getValue().isEmpty() -> {
            showErrorMessage(getString(R.string.please_enter_bank_name))
            false
        }

        binding.etAccountNo.getValue().isEmpty() -> {
            showErrorMessage(getString(R.string.please_enter_bank_account_number))
            false
        }

        binding.etAccountName.getValue().isEmpty() -> {
            showErrorMessage(getString(R.string.please_enter_account_holder_name))
            false
        }

        binding.etMobile.getValue().isEmpty() -> {
            showErrorMessage(getString(R.string.please_enter_mobile_wallet))
            false
        }

        else -> {
            true
        }
    }


    private fun observePaymentInfo() = viewModel.payoutInfo.observeData(this, onLoading = {
        showProgressDialog()
    }, onSuccess = {
        hideProgressDialog()
        SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
            ?.let {
                it.login?.registrationStepCompleted?.isBankDetailsCompleted = true
                SharedPreferencesManager.putModel(SharedPreferencesManager.Keys.USER_DATA, it)
            }
        startActivity(Intent(this@PayoutInformation, HomeActivity::class.java))
        finishAffinity()
    }, onError = {
        hideProgressDialog()
        showErrorMessage(this)
    })
}