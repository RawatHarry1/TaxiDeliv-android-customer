package com.salonedriver.view.adapter


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.salonedriver.R
import com.salonedriver.databinding.ItemBookingsBinding
import com.salonedriver.model.dataclassses.bookingHistory.BookingHistoryDC
import com.salonedriver.util.SharedPreferencesManager
import com.salonedriver.util.formatAmount
import com.salonedriver.util.getTime

class BookingsAdapter(val onClickBookingLambda: String.() -> Unit) :
    RecyclerView.Adapter<BookingsAdapter.BookingsHolder>() {

    private val bookingHistoryList by lazy { mutableListOf<BookingHistoryDC>() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingsHolder {
        val itemBinding =
            ItemBookingsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingsHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return bookingHistoryList.size
    }

    override fun onBindViewHolder(holder: BookingsHolder, position: Int) {
        holder.bind(bookingHistoryList[position])
    }

    inner class BookingsHolder(private val binding: ItemBookingsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(dataClass: BookingHistoryDC) {
            binding.tvCustomerName.text = dataClass.customerName.orEmpty()
            binding.tvDateTime.text = dataClass.createdAt.getTime(input = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", output = "MMM dd, yyyy, hh:mm a", applyTimeZone = true)
            binding.tvAmount.text = "${SharedPreferencesManager.getCurrencySymbol()} ${dataClass.totalFare.orEmpty().formatAmount()}"

            Glide.with(binding.ivMap).load(dataClass.trackingImage).into(binding.ivMap)
            binding.root.setOnClickListener {
                onClickBookingLambda(dataClass.tripId.orEmpty())
            }
        }
    }


    fun getItems() = bookingHistoryList


    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<BookingHistoryDC>){
        bookingHistoryList.clear()
        bookingHistoryList.addAll(list)
        notifyDataSetChanged()
    }

}
