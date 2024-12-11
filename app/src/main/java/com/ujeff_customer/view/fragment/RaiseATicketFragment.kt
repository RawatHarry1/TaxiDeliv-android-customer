package com.ujeff_customer.view.fragment

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.mukesh.photopicker.utils.pickerDialog
import com.ujeff_customer.R
import com.ujeff_customer.customClasses.singleClick.setOnSingleClickListener
import com.ujeff_customer.databinding.FragmentRaiseATicketBinding
import com.ujeff_customer.model.api.getJsonRequestBody
import com.ujeff_customer.model.api.getPartMap
import com.ujeff_customer.model.api.observeData
import com.ujeff_customer.model.dataClass.userData.UserDataDC
import com.ujeff_customer.util.SharedPreferencesManager
import com.ujeff_customer.util.arrayAdapter
import com.ujeff_customer.util.showSnackBar
import com.ujeff_customer.view.base.BaseFragment
import com.ujeff_customer.viewmodel.rideVM.RideVM
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File

@AndroidEntryPoint
class RaiseATicketFragment : BaseFragment<FragmentRaiseATicketBinding>() {
    private lateinit var binding: FragmentRaiseATicketBinding
    private val rideVM by viewModels<RideVM>()
    private var tripId = ""
    private var uploadedUrl = ""
    override fun initialiseFragmentBaseViewModel() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_raise_a_ticket
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        tripId = arguments?.getString("tripId") ?: ""
        observeFileUpload()
        observeRaiseTicket()
        binding.ivBack.setOnClickListener { findNavController().popBackStack() }
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
            requireActivity().arrayAdapter(autoCompleteTextView = binding.etReason, reason) {

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
        rideVM.uploadTicketFile.observeData(lifecycle = viewLifecycleOwner,
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
        rideVM.raiseATicket.observeData(lifecycle = viewLifecycleOwner,
            onLoading = {
                showProgressDialog()
            }, onError = {
                hideProgressDialog()
                showSnackBar(this)
            }, onSuccess = {
                hideProgressDialog()
                showSnackBar("Ticket has been raised successfully")
                findNavController().popBackStack()
            })
    }
}