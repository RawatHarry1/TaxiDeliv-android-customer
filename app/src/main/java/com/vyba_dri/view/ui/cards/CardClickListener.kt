package com.vyba_dri.view.ui.cards

import com.vyba_dri.model.dataclassses.CardData

interface CardClickListener {
    fun onCardClicked(card: CardData)
}