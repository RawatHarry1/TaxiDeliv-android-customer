package com.mcabs_driver.view.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import com.mukesh.photopicker.utils.pickerDialog
import com.mcabs_driver.R
import com.mcabs_driver.databinding.ActivityUploadDriverLicenseBinding
import com.mcabs_driver.model.api.getJsonRequestBody
import com.mcabs_driver.model.api.getPartMap
import com.mcabs_driver.model.dataclassses.fetchRequiredDocument.FetchRequiredDocumentDC
import com.mcabs_driver.model.dataclassses.fetchRequiredDocument.ImagePosition
import com.mcabs_driver.view.adapter.UploadDocImageAdapter
import com.mcabs_driver.view.base.BaseActivity
import com.mcabs_driver.viewmodel.OnBoardingVM
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class UploadDriverLicense : BaseActivity<ActivityUploadDriverLicenseBinding>(),
    UploadDocImageAdapter.AdapterClick {

    lateinit var binding: ActivityUploadDriverLicenseBinding
    private val viewModel by viewModels<OnBoardingVM>()
    private val uploadDocAdapter by lazy {
        UploadDocImageAdapter(this)
    }
    private val documentsData by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("documents", FetchRequiredDocumentDC::class.java)
                ?: FetchRequiredDocumentDC()
        } else {
            intent.getParcelableExtra<FetchRequiredDocumentDC>("documents")
                ?: FetchRequiredDocumentDC()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_upload_driver_license

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        binding.ivBackDriverUpload.setOnClickListener {
            finish()
        }
        binding.tvNextLicense.setOnClickListener {
            if (uploadDocAdapter.getItems().any { it.imageUrl.isNullOrEmpty() }){
                showErrorMessage(getString(R.string.please_upload_all_documents))
            }else {
                finish()
            }
            //startActivity(Intent(this@UploadDriverLicense, VehicleInfo::class.java))
        }
        setAdapter()
    }


    private fun setAdapter() {
        binding.tvUploadDriver.text = documentsData.docTypeText.orEmpty()
        binding.rvDocImage.adapter = uploadDocAdapter.apply {
            submitList(documentsData.imagePosition ?: emptyList())
        }
    }


    override fun onClick(position: Int, dataClass: ImagePosition) {
        uploadImage(position, dataClass)
    }


    private fun uploadImage(position: Int, dataClass: ImagePosition) = try {
        pickerDialog().setPickerCloseListener { _, uris ->
            try {
                viewModel.uploadDocument(hashMap = hashMapOf(
                    "docTypeNum" to documentsData.docTypeNum?.toString()?.getJsonRequestBody(),
                    "imgPosition" to dataClass.imgPosition?.toString()?.getJsonRequestBody()
                ),
                    part = File(uris).getPartMap("image"),
                    isLoading = {
                        showProgressDialog()
                    },
                    isError = {
                        hideProgressDialog()
                        showErrorMessage(this)
                    },
                    isSuccess = {
                        hideProgressDialog()
                        dataClass.imageUrl = uris
                        uploadDocAdapter.changeItem(dataClass, position)
                    })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}