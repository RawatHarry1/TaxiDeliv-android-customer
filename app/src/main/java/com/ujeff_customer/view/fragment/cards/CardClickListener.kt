package com.ujeff_customer.view.fragment.cards

import com.ujeff_customer.model.dataClass.CardData

interface CardClickListener {
    fun onCardClicked(card: CardData)
}