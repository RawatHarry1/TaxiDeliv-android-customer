package com.mcabs_driver.view.ui.cards

import com.mcabs_driver.model.dataclassses.CardData

interface CardClickListener {
    fun onCardClicked(card: CardData)
}