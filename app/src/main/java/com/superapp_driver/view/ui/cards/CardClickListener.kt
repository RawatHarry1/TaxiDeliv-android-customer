package com.superapp_driver.view.ui.cards

import com.superapp_driver.model.dataclassses.CardData

interface CardClickListener {
    fun onCardClicked(card: CardData)
}