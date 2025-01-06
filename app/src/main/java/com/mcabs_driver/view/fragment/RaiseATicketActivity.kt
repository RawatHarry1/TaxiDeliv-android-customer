package com.mcabs_driver.view.fragment

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.mukesh.photopicker.utils.pickerDialog
import com.mcabs_driver.R
import com.mcabs_driver.customClasses.singleClick.setOnSingleClickListener
import com.mcabs_driver.databinding.FragmentRaiseATicketBinding
import com.mcabs_driver.model.api.getJsonRequestBody
import com.mcabs_driver.model.api.getPartMap
import com.mcabs_driver.model.api.observeData
import com.mcabs_driver.model.dataclassses.userData.UserDataDC
import com.mcabs_driver.util.SharedPreferencesManager
import com.mcabs_driver.util.arrayAdapter
import com.mcabs_driver.view.base.BaseActivity
import com.mcabs_driver.viewmodel.RideViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File

@AndroidEntryPoint
class RaiseATicketActivity : BaseActivity<FragmentRaiseATicketBinding>() {
    private lateinit var binding: FragmentRaiseATicketBinding
    private val rideVM by viewModels<RideViewModel>()
    private var tripId = ""
    private var uploadedUrl = ""


    override fun getLayoutId(): Int {
        return R.layout.fragment_raise_a_ticket
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewDataBinding()
        tripId = intent?.getStringExtra("tripId") ?: ""
        observeFileUpload()
        observeRaiseTicket()
        binding.ivBack.setOnClickListener { finish() }
        binding.ivUploadImage.setOnClickListener {
            pickerDialog().setPickerCloseListener { _, uris ->
                showSnackBar("Please wait uploading image...")
                binding.ivDelete.isVisible = true
                binding.ivUploadImage.isVisible = false
                Glide.with(this).load(uris).into(binding.ivTicketImage)
                rideVM.uploadTicketFile(
                    part = File(uris).getPartMap("image"),
                    HashMap<String, RequestBody?>().apply {
                        put("trip_id", tripId.getJsonRequestBody())
                    })
            }.show()
        }
        binding.ivDelete.setOnSingleClickListener {
            binding.ivUploadImage.isVisible = true
            binding.ivTicketImage.isVisible = false
            binding.ivDelete.isVisible = false
        }
        val reason =
            SharedPreferencesManager.getModel<UserDataDC>(SharedPreferencesManager.Keys.USER_DATA)?.login?.supportTicketReasons.orEmpty()
        binding.etReason.setOnSingleClickListener {
            arrayAdapter(autoCompleteTextView = binding.etReason, reason) {

            }
        }
        binding.tvSubmit.setOnClickListener {
            if (binding.etReason.text.toString().trim().isEmpty())
                showSnackBar("Please select reason")
            else if (binding.etEnterDescription.text.toString().trim().isEmpty())
                showSnackBar("Please enter description")
            else {
                rideVM.raiseATicket(jsonObject = JSONObject().apply {
                    put("ride_id", tripId)
                    put("ticket_image", uploadedUrl)
                    put("subject", binding.etReason.text.toString())
                    put("description", binding.etEnterDescription.text.toString())
                })
            }
        }
    }


    private fun observeFileUpload() {
        rideVM.uploadTicketFile.observeData(lifecycle = this,
            onLoading = {
                showProgressDialog()
            }, onError = {
                hideProgressDialog()
                showSnackBar(this)
            }, onSuccess = {
                hideProgressDialog()
                uploadedUrl = this?.file_path ?: ""
            })
    }

    private fun observeRaiseTicket() {
        rideVM.raiseATicket.observeData(lifecycle = this,
            onLoading = {
                showProgressDialog()
            }, onError = {
                hideProgressDialog()
                showSnackBar(this)
            }, onSuccess = {
                hideProgressDialog()
                showSnackBar("Ticket has been raised successfully")
                finish()
            })
    }
}