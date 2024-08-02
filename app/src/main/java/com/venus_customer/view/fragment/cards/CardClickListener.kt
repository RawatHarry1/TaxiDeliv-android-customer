package com.venus_customer.view.fragment.cards

import com.venus_customer.model.dataClass.CardData

interface CardClickListener {
    fun onCardClicked(card: CardData)
}