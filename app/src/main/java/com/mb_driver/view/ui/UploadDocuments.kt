package com.mb_driver.view.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.mb_driver.R
import com.mb_driver.SaloneDriver
import com.mb_driver.customClasses.singleClick.setOnSingleClickListener
import com.mb_driver.databinding.ActivityUploadDocumentsBinding
import com.mb_driver.model.api.observeData
import com.mb_driver.view.adapter.DocumentsAdapter
import com.mb_driver.view.base.BaseActivity
import com.mb_driver.viewmodel.OnBoardingVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UploadDocuments : BaseActivity<ActivityUploadDocumentsBinding>() {

    lateinit var binding: ActivityUploadDocumentsBinding
    private val viewModel by viewModels<OnBoardingVM>()
    private val isFromView by lazy { intent.getBooleanExtra("isFromView", false) }
    private val documentAdapter by lazy {
        DocumentsAdapter {
            startActivity(Intent(SaloneDriver.appContext, UploadDriverLicense::class.java).also {
                it.putExtra("documents", this)
            })
        }
    }


    override fun getLayoutId(): Int {
        return R.layout.activity_upload_documents

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        setRecycler()
        observeFetchDocument()
    }


    override fun onResume() {
        super.onResume()
        viewModel.fetchRequiredDocument()
    }


    private fun setRecycler() {
        binding.ivBackUpload.isVisible = isFromView
        binding.tvNext.isVisible = !isFromView
        binding.rvUploadDocument.adapter = documentAdapter
        binding.tvNext.setOnSingleClickListener {
            val documentRequiredList = documentAdapter.getItems().filter { it.docRequirement == 1 }
            if (documentRequiredList.all {
                    ((it.docUrl?.size ?: 0) > 0) && ((it.docUrl?.size
                        ?: 0) >= (it.imagePosition?.size ?: 0))
                }) {
                startActivity(Intent(this@UploadDocuments, PayoutInformation::class.java))
                finish()
            } else {
                showErrorMessage(getString(R.string.please_upload_all_documents))
            }
        }
    }


    private fun observeFetchDocument() =
        viewModel.fetchRequiredDocumentDC.observeData(this, onLoading = {
            showProgressDialog()
        }, onSuccess = {
            hideProgressDialog()
            documentAdapter.submitList(this ?: emptyList())
        }, onError = {
            hideProgressDialog()
            showErrorMessage(this)
        })
}