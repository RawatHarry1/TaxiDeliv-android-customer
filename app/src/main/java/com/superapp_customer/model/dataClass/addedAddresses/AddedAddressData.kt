package com.superapp_customer.model.dataClass.addedAddresses

import android.os.Parcelable
import com.google.errorprone.annotations.Keep
import kotlinx.parcelize.Parcelize

@Keep
data class AddedAddressData(
    val saved_addresses: List<SavedAddresse>
)

data class AutosAddresse(
    val addr: String,
    val engagement_id: Int,
    val freq: Int,
    val last_used_on: String,
    val lat: Double,
    val lng: Double
)
@Parcelize
data class SavedAddresse(
    val addr: String,
    val freq: Int,
    val google_place_id: String,
    val id: Int,
    val instr: String,
    val is_confirmed: Int,
    val lat: Double,
    val lng: Double,
    val nick_name: String,
    val type: String
): Parcelable