package com.marsapp_driver.view.ui.cards

import com.marsapp_driver.model.dataclassses.CardData

interface CardClickListener {
    fun onCardClicked(card: CardData)
}