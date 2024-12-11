package com.ujeff_driver.view.ui.cards


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.ujeff_driver.R
import com.ujeff_driver.databinding.ItemCardBinding
import com.ujeff_driver.model.dataclassses.CardData
import com.ujeff_driver.view.ui.PaymentActivity


class CardAdapter(
    private val context: Context,
    private val cardArrayList: ArrayList<CardData>,
    private val listener: OnCardClickListener
) :
    RecyclerView.Adapter<CardAdapter.CardViewHolder>() {
    var selectedCarId = ""
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding, listener)
    }

    override fun getItemCount(): Int {
        return cardArrayList.size
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(cardArrayList[position])
    }

    inner class CardViewHolder(
        private val binding: ItemCardBinding,
        private val listener: OnCardClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(card: CardData) {

            binding.ivDeleteCard.isVisible = !PaymentActivity.whileRide
            binding.ivPayByCard.isVisible = PaymentActivity.whileRide
            binding.tvCardNumber.text = "**** **** **** ${card.last_4}"
            binding.tvCardBrand.text = card.brand ?: ""
            if (PaymentActivity.cardId == card.card_id) {
                binding.ivPayByCard.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_tick_circle
                    )
                )
            } else {
                binding.ivPayByCard.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_circular_stroke_black
                    )
                )
            }
            binding.root.setOnClickListener {
                if (PaymentActivity.whileRide) {
                    PaymentActivity.cardId = card.card_id
                    notifyDataSetChanged()
                    listener.onCardClick(card, false)
                }else
                {
                    listener.onCardClick(card,true)
                }
            }
        }
    }

    interface OnCardClickListener {
        fun onCardClick(card: CardData,isDelete:Boolean)
    }
}
