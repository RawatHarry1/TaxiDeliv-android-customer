package com.superapp_customer.view.fragment.cards

import com.superapp_customer.model.dataClass.CardData

interface CardClickListener {
    fun onCardClicked(card: CardData)
}