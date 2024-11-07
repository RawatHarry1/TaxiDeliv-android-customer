package com.superapp_driver.model.dataclassses

data class UploadPackageResponse(
    val file_path: String,
    val flag: Int,
    val message: String
)

data class PackageStatus(
    val can_end: Int,
    val can_start: Int
)