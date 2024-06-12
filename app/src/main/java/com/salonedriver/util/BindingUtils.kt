package com.salonedriver.util

import android.os.Build
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.BindingAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.salonedriver.model.dataclassses.base.BasePaginationModel
import com.salonedriver.view.ui.WalkAdapter
import com.salonedriver.view.ui.WalkThrough
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator


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
        val token: TypeToken<ArrayList<T>> = object : TypeToken<ArrayList<T>>() {}
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
    view.setCompoundDrawablesWithIntrinsicBounds(0, image, 0, 0)
}


@BindingAdapter(value = ["setAdapter"])
fun setAdapter(vp: ViewPager2, list: MutableList<WalkThrough.WalkData>) {
    vp.adapter = WalkAdapter().apply {
        submitList(list)
    }
}

@BindingAdapter(value = ["attachToViewPager"])
fun attachToViewPager(dots: SpringDotsIndicator, viewPager2: ViewPager2) {
    dots.attachTo(viewPager2)
}

