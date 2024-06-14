package com.venus_customer.util

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.BindingAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator
import com.venus_customer.VenusApp
import com.venus_customer.model.dataClass.base.BasePaginationModel
import com.venus_customer.view.activity.walk_though.WalkAdapter
import com.venus_customer.view.activity.walk_though.WalkThrough


inline fun <reified T> Any?.getResponse(): T? {
    return try {
        val json = Gson().toJson(this)
        Gson().fromJson(json, T::class.java)
    } catch (e: Exception) {
        Log.e("GSON ERROR", e.message.toString())
        null
    }
}

inline fun <reified T> Any?.getResponseInArrayList(): ArrayList<T?>? {
    return try {
        val token:TypeToken<ArrayList<T>> = object : TypeToken<ArrayList<T>>() {}
        val json = Gson().toJson(this)
        Gson().fromJson(json, token.type)
    } catch (e: Exception) {
        Log.e("GSON ERROR ", "" + e.message)
        null
    }
}

inline fun <reified T> Any?.getPaginationResponse(): BasePaginationModel<T>? {
    return try {
        val json = Gson().toJson(this)
        val baseModel = Gson().fromJson(json, BasePaginationModel::class.java)
        val data = baseModel.data?.getResponseInArrayList<T>()

        val finalModel = BasePaginationModel<T>(
            total = baseModel.total,
            pageNo = baseModel.pageNo,
            totalPage = baseModel.totalPage,
            nextPage = baseModel.nextPage,
            limit = baseModel.nextPage,
            data = data,
        )

        finalModel

    } catch (e: Exception) {
        Log.e("GSON ERROR", e.message.toString())
        null
    }

}

@RequiresApi(Build.VERSION_CODES.M)
@BindingAdapter(value = ["setBottomHomeSelectedView"])
fun setBottomHomeSelectedView(view: TextView, isSelected: Boolean?) {
    if (isSelected == true) {
        view.alpha = 1f
    } else {
        view.alpha = 0.3f
    }
}

@RequiresApi(Build.VERSION_CODES.M)
@BindingAdapter(value = ["setBottomHomeImageView"])
fun setBottomHomeImageView(view: TextView, image: Int) {
        view.setCompoundDrawablesWithIntrinsicBounds(0,image,0,0)
}

@BindingAdapter(value = ["attachToViewPager"])
fun attachToViewPager(dots: SpringDotsIndicator, viewPager2: ViewPager2) {
    dots.attachTo(viewPager2)
}


fun showSnackBar(message: String, view: View? = null){
    Log.e("dfsdfsdf", "sdfsdf  ${message}")
    VenusApp.appContext.let { context ->
        val snackbar = Snackbar.make(
            view ?: (context as Activity).findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_SHORT
        )
        snackbar.show()
    }
}

fun String?.convertDouble() = (this?.toDoubleOrNull() ?: 0.0)

fun String?.splitSpace() = (this?.split(" ")?.firstOrNull().orEmpty())

fun String?.formatString(digits: Int = 2) = "%.${digits}f".format(this.convertDouble())
