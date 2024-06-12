package com.salonedriver.model.dataclassses.fetchRequiredDocument


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FetchRequiredDocumentDC(
    @SerializedName("doc_count")
    val docCount: Int? = null,
    @SerializedName("doc_requirement")
    val docRequirement: Int? = null,
    @SerializedName("doc_status")
    val docStatus: String? = null,
    @SerializedName("doc_type_num")
    val docTypeNum: Int? = null,
    @SerializedName("doc_type_text")
    val docTypeText: String? = null,
    @SerializedName("doc_url")
    val docUrl: List<String?>? = null,
    @SerializedName("image_position")
    val imagePosition: List<ImagePosition>? = null,
    @SerializedName("reason")
    val reason: String? = null
) : Parcelable