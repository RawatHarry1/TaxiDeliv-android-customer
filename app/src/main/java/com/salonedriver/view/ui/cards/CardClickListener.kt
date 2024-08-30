package com.salonedriver.view.ui.cards

import com.salonedriver.model.dataclassses.CardData

interface CardClickListener {
    fun onCardClicked(card: CardData)
}