package com.ujeff_customer.util.constants

import android.Manifest
import android.os.Build

object PermissionConstants {
    const val REQUEST_CODE_IN_SETTING = 200
    const val  REQ_CAMERA = 201
    const val  REQ_GALLERY = 202
    var permissionCamera = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
    var permissionCamera13 = arrayOf(
        Manifest.permission.CAMERA
//        Manifest.permission.READ_MEDIA_IMAGES
    )
    var permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE

    )
    var permissionLocation = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
//        Manifest.permission.BLUETOOTH_CONNECT,
//        Manifest.permission.BLUETOOTH_SCAN
    )
    var permissionBluetooth = arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN
    )
}