package com.ujeff_driver.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.ujeff_driver.R
import com.ujeff_driver.SaloneDriver
import com.ujeff_driver.databinding.ActivityDocumentsBinding
import com.ujeff_driver.model.api.observeData
import com.ujeff_driver.view.adapter.DocumentsAdapter
import com.ujeff_driver.view.base.BaseFragment
import com.ujeff_driver.view.ui.UploadDriverLicense
import com.ujeff_driver.view.ui.home_drawer.HomeActivity
import com.ujeff_driver.viewmodel.OnBoardingVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DocumentsFragment : BaseFragment<ActivityDocumentsBinding>() {

    lateinit var binding: ActivityDocumentsBinding
    private val viewModel by viewModels<OnBoardingVM>()
    private val isFromView = true
    private val documentAdapter by lazy {
        DocumentsAdapter {
            startActivity(Intent(SaloneDriver.appContext, UploadDriverLicense::class.java).also {
                it.putExtra("documents", this)
            })
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_documents
    }

    override fun initialiseFragmentBaseViewModel() {

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        setRecycler()
        binding.ivMenuBurg.setOnClickListener {
            (activity as HomeActivity).openDrawer()
        }
        observeFetchDocument()
    }


    override fun onResume() {
        super.onResume()
        viewModel.fetchRequiredDocument()
    }


    private fun setRecycler() {
        binding.ivMenuBurg.isVisible = isFromView
        binding.rvDocuments.adapter = documentAdapter
    }


    private fun observeFetchDocument() =
        viewModel.fetchRequiredDocumentDC.observeData(this, onLoading = {
            showProgressDialog()
        }, onSuccess = {
            hideProgressDialog()
            documentAdapter.submitList(this ?: emptyList())
        }, onError = {
            hideProgressDialog()
            showToastShort(this)
        })

}