package com.superapp_customer.model.dataClass

data class AddPackage(
    var id: Int,
    var packageSize :String,
    var packageType: String,
    var itemDescription: String,
    var quantity: String,
    var length: String,
    var width: String,
    var height: String,
    var weight: String,
    var image: String
)