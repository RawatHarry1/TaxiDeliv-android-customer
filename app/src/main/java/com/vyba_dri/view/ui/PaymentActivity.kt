package com.vyba_dri.view.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.vyba_dri.R
import com.vyba_dri.customClasses.singleClick.setOnSingleClickListener
import com.vyba_dri.databinding.DialogFragementShowAndAddCardsBinding
import com.vyba_dri.dialogs.CustomProgressDialog
import com.vyba_dri.dialogs.DialogUtils
import com.vyba_dri.model.api.observeData
import com.vyba_dri.model.dataclassses.CardData
import com.vyba_dri.model.dataclassses.userData.UserDataDC
import com.vyba_dri.util.SharedPreferencesManager
import com.vyba_dri.view.base.BaseActivity
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.model.SetupIntent
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.vyba_dri.view.ui.cards.CardAdapter
import com.vyba_dri.view.ui.cards.CardViewModel
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
    private var clientSecret = ""
    private val cardVM by viewModels<CardViewModel>()
    private lateinit var cardAdapter: CardAdapter
    private var cardArrayList = ArrayList<CardData>()
    private val progressBar by lazy { CustomProgressDialog() }

    companion object {
        var cardId = ""
        var whileRide = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        whileRide = intent.getBooleanExtra("whileRide", false)
        cardId = intent.getStringExtra("cardId") ?: ""
        SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
            ?.let {
                if (!it.login?.stripeCredentials?.publishableKey.isNullOrEmpty()) {
                    stripe = Stripe(
                        this,
                        it.login?.stripeCredentials?.publishableKey ?: ""
                    )
                    PaymentConfiguration.init(
                        this,
                        it.login?.stripeCredentials?.publishableKey ?: ""
                    )
                }
                if (!it.login?.stripeCredentials?.clientSecret.isNullOrEmpty())
                    clientSecret = it.login?.stripeCredentials?.clientSecret ?: ""
            }

        paymentSheet = PaymentSheet(activity = this, callback = ::onPaymentSheetResult)
        binding.tvAddNewCard.setOnSingleClickListener {
            binding.tvAddNewCard.isEnabled = false
            cardVM.addCard(clientSecret)
        }
        binding.ivBack.setOnSingleClickListener { finish() }
        cardAdapter = CardAdapter(this, cardArrayList, this)
        binding.rvViewCards.adapter = cardAdapter
        observeCard()
        observeGetCards()
        observeConfirmCard()
        observeDeleteCard()
        cardVM.getCardsData(1)
    }

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                binding.tvAddNewCard.isEnabled = true
            }

            is PaymentSheetResult.Failed -> {
                binding.tvAddNewCard.isEnabled = true
                Log.i("StripePayment", "FAILED::: ${paymentSheetResult.error.localizedMessage}")
                showErrorMessage("Something went wrong please try again later.")
            }

            is PaymentSheetResult.Completed -> {
                // Display for example, an order confirmation screen
                showErrorMessage("Confirming your card please wait...")
                cardVM.confirmCard(clientSecret, setupIntentId)
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
            progressBar.show(this)
        }, onSuccess = {
            progressBar.dismiss()
            setupIntentClientSecret = this?.client_secret ?: ""
            setupIntentId = this?.setupIntent?.id ?: ""
            presentPaymentSheet()
        }, onError = {
            binding.tvAddNewCard.isEnabled = true
            progressBar.dismiss()
            showSnackBar(this, binding.tvAddNewCard)
        })

    private fun observeConfirmCard() =
        cardVM.confirmCardData.observeData(this, onLoading = {
            progressBar.show(this)
        }, onSuccess = {
            progressBar.dismiss()
            binding.tvAddNewCard.isEnabled = true
            cardVM.getCardsData(1)
        }, onError = {
            progressBar.dismiss()
            showSnackBar(this, binding.tvAddNewCard)
        })


    private fun observeDeleteCard() =
        cardVM.deleteCardsData.observeData(this, onLoading = {
            progressBar.show(this)
        }, onSuccess = {
            progressBar.dismiss()
            cardVM.getCardsData(1)
        }, onError = {
            progressBar.dismiss()
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

    override fun onCardClick(card: CardData, isDelete: Boolean) {
        if (isDelete) {
            DialogUtils.getNegativeDialog(
                this, "Remove Card",
                "Are you sure you want to remove *** ${card.last_4} card?"
            ) {
                cardVM.deleteCardsData(card.card_id)
            }
        } else
            if (whileRide) {
                val resultIntent = Intent()
                resultIntent.putExtra("cardId", card.card_id ?: "")
                resultIntent.putExtra("last4", card.last_4 ?: "")
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
    }
}