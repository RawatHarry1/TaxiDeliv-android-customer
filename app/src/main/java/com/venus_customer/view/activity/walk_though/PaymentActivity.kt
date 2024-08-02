package com.venus_customer.view.activity.walk_though

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.model.SetupIntent
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.venus_customer.R
import com.venus_customer.customClasses.singleClick.setOnSingleClickListener
import com.venus_customer.databinding.DialogFragementShowAndAddCardsBinding
import com.venus_customer.model.api.observeData
import com.venus_customer.model.dataClass.CardData
import com.venus_customer.model.dataClass.userData.UserDataDC
import com.venus_customer.util.SharedPreferencesManager
import com.venus_customer.util.showSnackBar
import com.venus_customer.view.base.BaseActivity
import com.venus_customer.view.fragment.cards.CardAdapter
import com.venus_customer.view.fragment.cards.CardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentActivity : BaseActivity<DialogFragementShowAndAddCardsBinding>(),
    CardAdapter.OnCardClickListener {
    override fun getLayoutId(): Int {
        return R.layout.dialog_fragement_show_and_add_cards
    }

    lateinit var binding: DialogFragementShowAndAddCardsBinding
    private lateinit var stripe: Stripe
    private lateinit var paymentSheet: PaymentSheet
    private lateinit var setupIntentClientSecret: String
    private lateinit var setupIntentId: String
    private val cardVM by viewModels<CardViewModel>()
    private lateinit var cardAdapter: CardAdapter
    private var cardArrayList = ArrayList<CardData>()
    private var whileRide = false

    companion object {
        var cardId = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        whileRide = intent.getBooleanExtra("whileRide", false)
        cardId = intent.getStringExtra("cardId") ?: ""
        SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
            ?.let {
                if (!it.login?.stripeCredentials?.publishableKey.isNullOrEmpty())
                    stripe = Stripe(
                        this,
                        it.login?.stripeCredentials?.publishableKey ?: ""
                    )
                PaymentConfiguration.init(
                    this,
                    it.login?.stripeCredentials?.publishableKey ?: ""
                )
            }

        paymentSheet = PaymentSheet(activity = this, callback = ::onPaymentSheetResult)
        binding.tvAddNewCard.setOnSingleClickListener {
            cardVM.addCard("gJJi0mlw2iVU5Syu58ImglbNYm0tJKyB")
        }
        binding.ivBack.setOnSingleClickListener { finish() }
        cardAdapter = CardAdapter(this, cardArrayList, this)
        binding.rvViewCards.adapter = cardAdapter
        observeCard()
        observeGetCards()
        observeConfirmCard()
        cardVM.getCardsData(1)
    }

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                showSnackBar("CANCELED", binding.tvAddNewCard)
            }

            is PaymentSheetResult.Failed -> {
                showSnackBar("FAILED", binding.tvAddNewCard)
            }

            is PaymentSheetResult.Completed -> {
                // Display for example, an order confirmation screen
                showSnackBar("Confirming your card please wait...", binding.tvAddNewCard)
                cardVM.confirmCard("gJJi0mlw2iVU5Syu58ImglbNYm0tJKyB", setupIntentId)
//                lifecycleScope.launch {
//                    delay(1000)
//                    Log.i("PaymentSheetResult", "setupIntentClientSecret $setupIntentClientSecret")
//                    retrieveSetupIntent()
//                }
            }
        }
    }

    private fun presentPaymentSheet() {
        Log.i("PaymentSheetResult", "setupIntentClientSecret $setupIntentClientSecret")
        paymentSheet.presentWithSetupIntent(
            setupIntentClientSecret = setupIntentClientSecret
        )
    }


    private fun retrieveSetupIntent() {
        stripe.retrieveSetupIntent(
            clientSecret = setupIntentClientSecret,
            callback = object : ApiResultCallback<SetupIntent> {
                override fun onError(e: Exception) {
                    Log.i("PaymentSheetResult", "retrieveSetupIntent ERROR: ${e.localizedMessage}")
                }

                override fun onSuccess(result: SetupIntent) {
                    Log.i("PaymentSheetResult", "retrieveSetupIntent: $result")
                    Log.i("PaymentSheetResult", "STATUS: ${result.status}")
                    Log.i(
                        "PaymentSheetResult",
                        "PAYMENT METHOD TYPES: ${result.paymentMethodTypes}"
                    )
                    result.paymentMethod?.let { paymentMethod ->
                        val card = paymentMethod.card
                        Log.i(
                            "PaymentSheetResult",
                            "Card Brand: ${card?.brand}, Last4: ${card?.last4}"
                        )
                    } ?: run {
                        Log.i("PaymentSheetResult", "No payment method attached")
                    }
                }
            }
        )
    }

    private fun observeCard() =
        cardVM.addCardData.observeData(this, onLoading = {
            showProgressDialog()
        }, onSuccess = {
            hideProgressDialog()
            setupIntentClientSecret = this?.client_secret ?: ""
            setupIntentId = this?.setupIntent?.id ?: ""
            presentPaymentSheet()
        }, onError = {
            hideProgressDialog()
            showSnackBar(this, binding.tvAddNewCard)
        })

    private fun observeConfirmCard() =
        cardVM.confirmCardData.observeData(this, onLoading = {
            showProgressDialog()
        }, onSuccess = {
            hideProgressDialog()
            cardVM.getCardsData(1)
        }, onError = {
            hideProgressDialog()
            showSnackBar(this, binding.tvAddNewCard)
        })

    private fun observeGetCards() =
        cardVM.getCardsData.observeData(this, onLoading = {
//            showProgressDialog()
            if (applicationContext != null) {
                binding.shimmerLayout.shimmerLayout.isVisible = true
                binding.rvViewCards.isVisible = false
            }
        }, onSuccess = {
//            hideProgressDialog()
            if (applicationContext != null) {
                binding.shimmerLayout.shimmerLayout.isVisible = false
                binding.rvViewCards.isVisible = true
                cardArrayList.clear()
                cardArrayList.addAll(this as ArrayList)
                cardAdapter.notifyDataSetChanged()
                binding.tvNoCardsFound.isVisible = cardAdapter.itemCount == 0
            }
        }, onError = {
//            hideProgressDialog()
            if (applicationContext != null) {
                binding.shimmerLayout.shimmerLayout.isVisible = false
                binding.rvViewCards.isVisible = true
            }
            showSnackBar(this, binding.tvAddNewCard)
        })

    override fun onCardClick(card: CardData) {
        if (whileRide) {
            val resultIntent = Intent()
            resultIntent.putExtra("cardId", card.card_id ?: "")
            resultIntent.putExtra("last4", card.last_4 ?: "")
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}