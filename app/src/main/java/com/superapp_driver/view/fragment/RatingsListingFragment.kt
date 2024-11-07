package com.superapp_driver.view.fragment

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toolbar.LayoutParams
import com.superapp_driver.R
import com.superapp_driver.databinding.FragmentRatingsListingBinding
import com.superapp_driver.view.adapter.RatingsAdapter
import com.superapp_driver.view.base.BaseFragment
import com.superapp_driver.view.ui.home_drawer.HomeActivity


class RatingsListingFragment : BaseFragment<FragmentRatingsListingBinding>() {

    lateinit var binding: FragmentRatingsListingBinding
    lateinit var adapter: RatingsAdapter
    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_ratings_listing
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        adapter = RatingsAdapter()
        binding.rvRatings.adapter = adapter
    }

    override fun onResume() {
        super.onResume()

        binding.ivMenuBurg.setOnClickListener {
            (activity as HomeActivity).openDrawer()
        }
        binding.btnFilter.setOnClickListener {
            filterDialog(it.context)
        }
    }


    private lateinit var alertDialog: AlertDialog

    private fun filterDialog(context: Context) {
        alertDialog = AlertDialog.Builder(context, R.style.SheetDialog).create()
        val view = layoutInflater.inflate(R.layout.dialog_filter, null)
        view.layoutParams =
            ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        view.setBackgroundColor(Color.TRANSPARENT)
        alertDialog.setView(view)
        val cbAll: CheckBox = view.findViewById(R.id.cbAll)!!
        val cbAccept: CheckBox = view.findViewById(R.id.cbAccept)!!
        val cbCancel: CheckBox = view.findViewById(R.id.cbCancel)!!
        val cbLatest: CheckBox = view.findViewById(R.id.cbLatest)!!

        cbAll.setOnCheckedChangeListener { p0, p1 ->
            if (p1) {
                cbAccept.isChecked = false
                cbCancel.isChecked = false
                cbLatest.isChecked = false
            }
        }
        cbCancel.setOnCheckedChangeListener { p0, p1 ->
            if (p1) {
                cbAll.isChecked = false
            }
        }
        cbAccept.setOnCheckedChangeListener { p0, p1 ->
            if (p1) {
                cbAll.isChecked = false
            }
        }
        cbLatest.setOnCheckedChangeListener { p0, p1 ->
            if (p1) {
                cbAll.isChecked = false
            }
        }
        alertDialog.show()
    }
}