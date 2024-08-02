package com.venus_customer.view.fragment.cards


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.venus_customer.R
import com.venus_customer.databinding.ItemCardBinding
import com.venus_customer.model.dataClass.CardData
import com.venus_customer.view.activity.walk_though.PaymentActivity


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
                PaymentActivity.cardId = card.card_id
                notifyDataSetChanged()
                listener.onCardClick(card)
            }
        }
    }

    interface OnCardClickListener {
        fun onCardClick(card: CardData)
    }
}
