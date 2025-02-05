package com.mb_driver.view.ui.cards

import com.mb_driver.model.dataclassses.CardData

interface CardClickListener {
    fun onCardClicked(card: CardData)
}