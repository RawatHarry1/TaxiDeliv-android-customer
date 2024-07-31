package com.venus_customer.view.fragment.cards


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.venus_customer.databinding.ItemCardBinding


class CardAdapter(private val listener: OnCardClickListener) :
    ListAdapter<Card, CardAdapter.CardViewHolder>(CardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = getItem(position)
        holder.bind(card)
    }

    class CardViewHolder(
        private val binding: ItemCardBinding,
        private val listener: OnCardClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(card: Card) {
            binding.tvCardNumber.text = card.number
            binding.root.setOnClickListener {
                listener.onCardClick(card)
            }
        }
    }

    interface OnCardClickListener {
        fun onCardClick(card: Card)
    }

    class CardDiffCallback : DiffUtil.ItemCallback<Card>() {
        override fun areItemsTheSame(
            oldItem: Card,
            newItem: Card
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Card,
            newItem: Card
        ): Boolean {
            return oldItem == newItem
        }
    }
}
