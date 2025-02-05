package com.mb_driver.model.dataclassses.base

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class BasePaginationModel<T>(
    @SerializedName("total")
    @Expose
    var total: Int? = null,
    @SerializedName("pageNo")
    @Expose
    var pageNo: Int? = null,
    @SerializedName("totalPage")
    @Expose
    var totalPage: Int? = null,
    @SerializedName("nextPage")
    @Expose
    var nextPage: Int? = null,
    @SerializedName("limit")
    @Expose
    var limit: Int? = null,
    @SerializedName("data")
    @Expose
    var data: ArrayList<T?>? = null,

    @SerializedName("nextHit")
    @Expose
    var nextHit: Int? = null,
)
