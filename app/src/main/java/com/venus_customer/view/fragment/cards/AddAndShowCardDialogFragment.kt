package com.venus_customer.view.fragment.cards

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.stripe.android.ApiResultCallback
import com.stripe.android.Stripe
import com.stripe.android.model.CardParams
import com.stripe.android.model.Token
import com.venus_customer.databinding.DialogFragementShowAndAddCardsBinding
import com.venus_customer.model.api.observeData
import com.venus_customer.model.dataClass.userData.UserDataDC
import com.venus_customer.util.SharedPreferencesManager
import com.venus_customer.util.showSnackBar
import com.venus_customer.view.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddAndShowCardDialogFragment : DialogFragment(), CardAdapter.OnCardClickListener {
    private var _binding: DialogFragementShowAndAddCardsBinding? = null
    private val binding get() = _binding!!
    private var cardClickListener: CardClickListener? = null
    private lateinit var stripe: Stripe
    private lateinit var cardAdapter: CardAdapter
    private val cardVM by viewModels<CardViewModel>()

    // Lambda function to pass card data back to parent fragment
    var onCardSelected: ((Card) -> Unit)? = null
    override fun onCardClick(card: Card) {
        onCardSelected?.invoke(card)
        dismiss() // Dismiss the dialog after clicking a card
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as Dialog
        val layoutParams = dialog.window!!.attributes
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog.window!!.attributes = layoutParams
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.let {
            it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            it.setWindowAnimations(android.R.style.Animation_Dialog)
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragementShowAndAddCardsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Apply the custom style for full-screen dialog
//        setStyle(STYLE_NO_TITLE, R.style.FullScreenDialogTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeCard()
        SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)
            ?.let {
                if (!it.login?.stripeCredentials?.publishableKey.isNullOrEmpty())
                    stripe = Stripe(
                        requireActivity(),
                        it.login?.stripeCredentials?.publishableKey ?: ""
                    )
            }

        binding.btnAddCard.setOnClickListener {
            cardVM.addCard("gJJi0mlw2iVU5Syu58ImglbNYm0tJKyB")
//            addCard()
        }
    }

    private fun addCard() {
        val cardParams = CardParams(
            number = "4242424242424242",
            expMonth = 12,
            expYear = 2024,
            cvc = "123"
        )
        if (::stripe.isInitialized)
            stripe.createCardToken(
                cardParams,
                callback = object : ApiResultCallback<Token> {
                    override fun onSuccess(result: Token) {
                        showSnackBar("SUCCESS::::  ${result.id}", binding.btnAddCard)
                    }

                    override fun onError(e: Exception) {
                        showSnackBar("ERROR::::  ${e.localizedMessage}", binding.btnAddCard)
                    }
                }
            )

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeCard() =
        cardVM.addCardData.observeData(viewLifecycleOwner, onLoading = {
            showProgressDialog()
        }, onSuccess = {
            hideProgressDialog()
        }, onError = {
            hideProgressDialog()
            showSnackBar(this, binding.btnAddCard)
        })

    fun showProgressDialog() {
        (activity as BaseActivity<*>?)?.showProgressDialog()
    }

    fun hideProgressDialog() {
        (activity as BaseActivity<*>?)?.hideProgressDialog()
    }
}

data class Card(
    val id: String,
    val number: String,
    val expMonth: Int,
    val expYear: Int,
    val cvc: String
)