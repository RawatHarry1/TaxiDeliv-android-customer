package com.mukesh.photopicker.picker

import android.Manifest
import android.content.ContentValues
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mukesh.photopicker.R
import com.mukesh.photopicker.contracts.PhotoGalleryContract
import com.mukesh.photopicker.databinding.DialogPickerBinding
import com.mukesh.photopicker.utils.ItemType
import com.mukesh.photopicker.utils.checkPermissions
import com.mukesh.photopicker.utils.getMediaFilePathFor


/**
 * Picker Dialog
 * */
class PickerDialog internal constructor() : BottomSheetDialogFragment(), View.OnClickListener {

    private var currentFragmentManager: FragmentManager? = null
    private val uris = mutableListOf<Uri>()
    private var onPickerCloseListener: OnPickerCloseListener? = null
    private var isCameraSelect: Boolean = false
    private val binding by lazy { DialogPickerBinding.inflate(layoutInflater) }


    /**
     * Take Picture Result Contract
     * */
    private val takePictureResultContract =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) {
                uris.firstOrNull()?.let { uri ->
                    requireContext().getMediaFilePathFor(uri).let { path ->
                        onPickerCloseListener?.onPickerClosed(ItemType.ITEM_CAMERA, path)
                        dismissAllowingStateLoss()
                    }
                }
            }
        }


    /**
     * Photo Gallery Result Contract
     * */
    private val photoGalleryResultContract =
        registerForActivityResult(PhotoGalleryContract()) { uris ->
            if (uris.isNotEmpty()) {
                uris.firstOrNull()?.let { uri ->
                    requireContext().getMediaFilePathFor(uri).let { path ->
                        onPickerCloseListener?.onPickerClosed(ItemType.ITEM_GALLERY, path)
                        dismissAllowingStateLoss()
                    }
                }
            }
        }


    /**
     * Media Images Picker
     * */
    private val mediaImagePicker =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                requireContext().getMediaFilePathFor(it).let {
                    onPickerCloseListener?.onPickerClosed(ItemType.ITEM_GALLERY, it.orEmpty())
                    dismissAllowingStateLoss()
                }
            }
        }


    /**
     * Static Methods
     * */
    companion object {
        private fun newInstance(
            fragmentManager: FragmentManager,
        ): PickerDialog {
            val dialog = PickerDialog()
            dialog.currentFragmentManager = fragmentManager

            return dialog
        }
    }


    /**
     * Builder Class
     * */
    class Builder {
        private val currentFragmentManager: FragmentManager

        internal constructor(activity: FragmentActivity) {
            currentFragmentManager = activity.supportFragmentManager
        }

        internal constructor(fragment: Fragment) {
            currentFragmentManager = fragment.childFragmentManager
        }

        internal fun create(): PickerDialog = newInstance(
            currentFragmentManager
        )
    }


    /**
     * On Create View
     * */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root


    /**
     * Get Theme
     * */
    override fun getTheme(): Int {
        return R.style.TransaparentSheet
    }


    /**
     * On View Created
     * */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickHandler()
    }


    /**
     * Open Camera
     * */
    private fun openCamera() {
        val ctx = context ?: return
        val fileName = "${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, fileName)
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TENET KYC")
        ctx.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?.let {
                uris.add(it)
                takePictureResultContract.launch(it)
            } ?: openCamera()
    }


    /**
     * Open Photo Gallery
     * */
    private fun openPhotoGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().checkPermissions(
                Manifest.permission.READ_MEDIA_IMAGES
            ) {
                mediaImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        } else {
            requireContext().checkPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) {
                photoGalleryResultContract.launch(Unit)
            }
        }
    }


    /**
     * Set Picker Close Listener
     * */
    fun setPickerCloseListener(listener: OnPickerCloseListener): PickerDialog {
        this.onPickerCloseListener = listener
        return this
    }


    /**
     * Show Dialog
     * */
    fun show() {
        uris.clear()
        val fragmentManager = currentFragmentManager
            ?: throw IllegalStateException("Fragment manager is not initialized")
        show(fragmentManager, javaClass.name)
    }


    /**
     * Click Handler
     * */
    private fun clickHandler() {
        binding.ivCross.setOnClickListener(this)
        binding.tvGallery.setOnClickListener(this)
        binding.tvCamera.setOnClickListener(this)
        binding.btSubmit.setOnClickListener(this)
    }


    /**
     * Click Listener
     * */
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ivCross -> dismissAllowingStateLoss()

            R.id.tvGallery -> {
                isCameraSelect = false
                setUpUI()
                openPhotoGallery()
            }

            R.id.tvCamera -> {
                isCameraSelect = true
                setUpUI()
                requireContext().checkPermissions(
                    Manifest.permission.CAMERA
                ) {
                    openCamera()
                }
            }

            R.id.btSubmit -> if (isCameraSelect) {
                requireContext().checkPermissions(
                    Manifest.permission.CAMERA
                ) {
                    openCamera()
                }
            } else {
                openPhotoGallery()
            }
        }
    }


    /**
     * Set Up UI
     * */
    private fun setUpUI() {
        binding.tvGallery.changeTint(!isCameraSelect)
        binding.tvCamera.changeTint(isCameraSelect)
    }


    /**
     * Change Tint
     * */
    private fun TextView.changeTint(isSelected: Boolean) {
        for (drawable in compoundDrawablesRelative) {
            if (drawable != null) {
                drawable.colorFilter =
                    PorterDuffColorFilter(
                        ContextCompat.getColor(
                            context,
                            if (isSelected) R.color._ffffff else R.color._52A8A0
                        ),
                        PorterDuff.Mode.SRC_IN
                    )
            }
        }
        this.background.setTint(
            ContextCompat.getColor(
                requireContext(),
                if (isSelected) R.color._52A8A0 else R.color._f6fafa
            )
        )
        this.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (isSelected) R.color._ffffff else R.color._52A8A0
            )
        )
    }

}