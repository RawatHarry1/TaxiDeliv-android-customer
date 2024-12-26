package com.vyba_dri.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.vyba_dri.R
import com.vyba_dri.customClasses.singleClick.setOnSingleClickListener
import com.vyba_dri.databinding.FragmentRaisedTicketListBinding
import com.vyba_dri.databinding.ItemRaisedTicketBinding
import com.vyba_dri.model.api.observeData
import com.vyba_dri.model.dataclassses.Ticket
import com.vyba_dri.util.AppUtils
import com.vyba_dri.util.GenericAdapter
import com.vyba_dri.util.showSnackBar
import com.vyba_dri.view.base.BaseFragment
import com.vyba_dri.viewmodel.RideViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RaisedTicketsListFragment : BaseFragment<FragmentRaisedTicketListBinding>() {
    private val rideVM by viewModels<RideViewModel>()
    private lateinit var ticketAdapter: GenericAdapter<Ticket>
    private val ticketArrayList = ArrayList<Ticket>()
    private lateinit var binding: FragmentRaisedTicketListBinding
    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_raised_ticket_list
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        setAdapter()
        observeTickets()
        binding.ivBack.setOnSingleClickListener { findNavController().popBackStack() }
        rideVM.getTicketList()
    }

    private fun setAdapter() {
        ticketAdapter = object : GenericAdapter<Ticket>(R.layout.item_raised_ticket) {
            @SuppressLint("SetTextI18n")
            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val bindingM = ItemRaisedTicketBinding.bind(holder.itemView)
                val data = getItem(position)
                bindingM.tvTicketIdValue.text = data.id.toString()
                bindingM.tvTicketReasonValue.text = data.subject.toString()
                bindingM.tvDate.text = "${AppUtils.convertUtcToLocal(data.created_at)} | ${
                    AppUtils.convertUtcToLocalDate(data.created_at)
                }"
//                TICKET_SENT: 0,
//                IN_QUEUE: 1,
//                RESOLVED: 2

                when (data.status) {
                    0 -> {
                        bindingM.tvStatusValue.setTextColor(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.black_text_color
                            )
                        )

                        bindingM.tvStatusValue.text = "Sent"
                    }

                    1 -> {
                        bindingM.tvStatusValue.setTextColor(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.black_text_color
                            )
                        )

                        bindingM.tvStatusValue.text = "In Queue"
                    }

                    2 -> {
                        bindingM.tvStatusValue.setTextColor(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.green_text_color
                            )
                        )
                        bindingM.tvStatusValue.text = "Resolved"
                    }
                }

            }
        }
        binding.rvRaisedTickets.adapter = ticketAdapter
    }

    private fun observeTickets() {
        rideVM.getTicketList.observeData(lifecycle = viewLifecycleOwner,
            onLoading = {
                binding.shimmerLayout.shimmerLayout.isVisible = true
                binding.rvRaisedTickets.isVisible = false
            }, onError = {
                binding.shimmerLayout.shimmerLayout.isVisible = false
                binding.rvRaisedTickets.isVisible = true
                showSnackBar(this)
            }, onSuccess = {
                binding.shimmerLayout.shimmerLayout.isVisible = false
                binding.rvRaisedTickets.isVisible = true
                ticketArrayList.clear()
                this?.let { ticketArrayList.addAll(it) }
                ticketAdapter.submitList(ticketArrayList)
                ticketAdapter.refreshAdapter()
                binding.tvNoTicketFound.isVisible = ticketAdapter.itemCount == 0
            })
    }
}