package com.salonedriver.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.salonedriver.R
import com.salonedriver.SaloneDriver
import com.salonedriver.databinding.ActivityDocumentsBinding
import com.salonedriver.databinding.ActivityUploadDocumentsBinding
import com.salonedriver.model.api.observeData
import com.salonedriver.view.adapter.DocumentsAdapter
import com.salonedriver.view.adapter.UploadedDocsAdapter
import com.salonedriver.view.base.BaseFragment
import com.salonedriver.view.ui.UploadDriverLicense
import com.salonedriver.view.ui.VehicleInfo
import com.salonedriver.view.ui.home_drawer.HomeActivity
import com.salonedriver.viewmodel.OnBoardingVM
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