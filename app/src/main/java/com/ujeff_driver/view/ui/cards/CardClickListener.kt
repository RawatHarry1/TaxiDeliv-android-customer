package com.ujeff_driver.view.ui.cards

import com.ujeff_driver.model.dataclassses.CardData

interface CardClickListener {
    fun onCardClicked(card: CardData)
}